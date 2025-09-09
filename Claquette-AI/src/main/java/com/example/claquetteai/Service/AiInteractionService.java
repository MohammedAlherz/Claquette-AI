package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiInteractionService {

    private final ProjectRepository projectRepository;
    private final FilmService filmService;
    private final CharacterService characterService;
    private final EpisodeService episodeService;
    private final CastingService castingService;
    private final UserRepository userRepository;
    private final SceneRepository sceneRepository;

    private final ProjectGenerationJobRepository jobRepository;
    private final ThreadPoolTaskExecutor generationExecutor;

    private final PlatformTransactionManager txManager;

    /** Controller calls this. Returns immediately with jobId; work runs async. */
    public String startScreenplayGeneration(Integer projectId, Integer userId) {
        ProjectGenerationJob job = new ProjectGenerationJob();
        job.setProjectId(projectId);
        job.setUserId(userId);
        job.setStatus(JobStatus.PENDING);
        job.setCurrentStep(JobStep.NONE);
        job = jobRepository.save(job);

        final String jobId = job.getId();
        generationExecutor.submit(() -> runJob(jobId));
        return jobId;
    }

    /** Progress lookup (no heavy logic). */
    public ProjectGenerationJob getJobStatus(String jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> new ApiException("job not found"));
    }

    /* ===================== Orchestration (async) ===================== */

    private void runJob(String jobId) {
        ProjectGenerationJob job = jobRepository.findById(jobId).orElseThrow(() -> new ApiException("job not found"));
        try {
            mark(job, JobStatus.RUNNING, JobStep.CHARACTERS, 0, "Starting…");

            int charCount = txRequiresNew(() -> generateCharacters(job.getProjectId(), job.getUserId()));
            mark(job, JobStatus.RUNNING, JobStep.CHARACTERS, 20, "Characters: " + charCount);

            mark(job, JobStatus.RUNNING, JobStep.FILM_OR_EPISODES, 25, "Generating story…");
            String storyMsg = txRequiresNew(() -> generateFilmOrEpisodes(job.getProjectId()));
            mark(job, JobStatus.RUNNING, JobStep.FILM_OR_EPISODES, 80, storyMsg);

            mark(job, JobStatus.RUNNING, JobStep.CASTING, 85, "Casting…");
            int castCount = txRequiresNew(() -> generateCasting(job.getProjectId()));
            mark(job, JobStatus.RUNNING, JobStep.CASTING, 90, "Casting: " + castCount);

            txRequiresNew(() -> { finalizeAndDecrementCredits(job.getProjectId(), job.getUserId()); return null; });
            mark(job, JobStatus.SUCCEEDED, JobStep.FINALIZE, 100, "Complete");
        } catch (Exception e) {
            fail(job, e);
        }
    }

    /* ===================== Chunked steps (each in its own TX) ===================== */

    /** STEP 1: Characters */
    private int generateCharacters(Integer projectId, Integer userId) throws Exception {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found with id: " + projectId));
        User user = userRepository.findUserById(userId);
        assertUserCanGenerate(user);

        // IMPORTANT: initialize existing collection in-session to avoid lazy init later (clear & replace safely)
        Hibernate.initialize(project.getCharacters());
        if (project.getCharacters() != null) {
            project.getCharacters().clear();
        }

        Set<FilmCharacters> characters = characterService.generateCharacters(project, project.getDescription());

        // ensure back-reference is set to avoid flush-time lazy touching
        for (FilmCharacters fc : characters) {
            fc.setProject(project);
        }

        project.setCharacters(characters);
        projectRepository.save(project);

        // DO NOT return entities; just a count
        return characters.size();
    }

    /** STEP 2: Film or Episodes (with scenes) */
    private String generateFilmOrEpisodes(Integer projectId) throws Exception {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found"));

        // Force-load characters within TX before using them
        Hibernate.initialize(project.getCharacters());
        String characterNames = extractCharacterNames(project.getCharacters());

        if ("FILM".equals(project.getProjectType())) {
            Film film = filmService.generateFilmWithScenes(project, characterNames);
            project.setFilms(film);
            projectRepository.save(project);

            if (film.getScenes() != null) {
                for (Scene s : film.getScenes()) {
                    if (s.getCharacters() != null && !s.getCharacters().isEmpty()) {
                        // ensure backrefs if needed
                        s.setFilm(film);
                        sceneRepository.save(s);
                    }
                }
            }
            filmService.validateFilmCharacterConsistency(film, characterNames);
            return "Film done";
        } else {
            int n = project.getEpisodeCount();
            Set<Episode> episodes = new HashSet<>();
            for (int i = 1; i <= n; i++) {
                Episode ep = episodeService.generateEpisodeWithScenes(project, i, characterNames);
                ep.setProject(project);
                episodes.add(ep);

                if (ep.getScenes() != null) {
                    for (Scene s : ep.getScenes()) {
                        if (s.getCharacters() != null && !s.getCharacters().isEmpty()) {
                            s.setEpisode(ep);
                            sceneRepository.save(s);
                        }
                    }
                }
                episodeService.validateEpisodeCharacterConsistency(ep, characterNames);
            }
            project.setEpisodes(episodes);
            projectRepository.save(project);
            return "Episodes done";
        }
    }

    /** STEP 3: Casting */
    private int generateCasting(Integer projectId) throws Exception {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found"));
        // initialize required bits if casting service navigates relations
        Hibernate.initialize(project.getCharacters());

        Set<CastingRecommendation> casting = castingService.generateCasting(project);
        for (CastingRecommendation cr : casting) {
            cr.setProject(project);
        }
        project.setCastingRecommendations(casting);
        projectRepository.save(project);
        return casting.size();
    }

    /** STEP 4: Finalize + decrement credits (reads full graph safely inside TX) */
    private void finalizeAndDecrementCredits(Integer projectId, Integer userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found"));

        // Initialize deep graph only for safe traversal/logging
        Hibernate.initialize(project.getCharacters());
        if (project.getFilms() != null) {
            Hibernate.initialize(project.getFilms().getScenes());
            if (project.getFilms().getScenes() != null) {
                project.getFilms().getScenes().forEach(sc -> Hibernate.initialize(sc.getCharacters()));
            }
        }
        if (project.getEpisodes() != null) {
            Hibernate.initialize(project.getEpisodes());
            project.getEpisodes().forEach(ep -> {
                Hibernate.initialize(ep.getScenes());
                if (ep.getScenes() != null) ep.getScenes().forEach(sc -> Hibernate.initialize(sc.getCharacters()));
            });
        }

        User user = userRepository.findUserById(userId);
        if (user != null) {
            user.setUseAI(user.getUseAI() - 1);
            userRepository.save(user);
        }

        // stay within session while traversing for debug
        debugCharacterSceneRelationships(project);
    }

    /* ===================== Helpers ===================== */

    private void assertUserCanGenerate(User user) {
        if (user == null) throw new ApiException("user not found");
        if (!user.getCompany().getIsSubscribed() && user.getUseAI() <= 0) {
            throw new ApiException("you cannot generate project using AI subscribe");
        }
    }

    private String extractCharacterNames(Set<FilmCharacters> characters) {
        if (characters == null || characters.isEmpty()) return "";
        return characters.stream()
                .map(FilmCharacters::getName)
                .filter(n -> n != null && !n.trim().isEmpty())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private void mark(ProjectGenerationJob job, JobStatus status, JobStep step, int progress, String info) {
        job.setStatus(status);
        job.setCurrentStep(step);
        job.setProgress(progress);
        job.setInfo(info);
        jobRepository.save(job);
    }

    private void fail(ProjectGenerationJob job, Exception e) {
        job.setStatus(JobStatus.FAILED);
        job.setErrorMessage(e.getMessage());
        jobRepository.save(job);
        e.printStackTrace();
    }

    /** Utility to run a block in REQUIRES_NEW (no proxy/self-invocation issues). */
    private <T> T txRequiresNew(TxCallback<T> work) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        var status = txManager.getTransaction(def);
        try {
            T result = work.doInTx();
            txManager.commit(status);
            return result;
        } catch (RuntimeException | Error e) {
            txManager.rollback(status);
            throw e;
        } catch (Exception e) {
            txManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface TxCallback<T> {
        T doInTx() throws Exception;
    }

    public void debugCharacterSceneRelationships(Project project) {
        System.out.println("=== CHARACTER-SCENE RELATIONSHIP ANALYSIS ===");
        if (project.getCharacters() != null) {
            System.out.println("Project Characters (" + project.getCharacters().size() + "):");
            for (FilmCharacters character : project.getCharacters()) {
                System.out.println("  - " + character.getName() + " (ID: " + character.getId() + ")");
            }
        }
        if (project.getFilms() != null && project.getFilms().getScenes() != null) {
            System.out.println("Film Scenes Analysis:");
            for (Scene scene : project.getFilms().getScenes()) {
                System.out.println("  Scene " + scene.getSceneNumber() + ":");
                if (scene.getCharacters() != null) {
                    for (FilmCharacters character : scene.getCharacters()) {
                        System.out.println("    - " + character.getName() + " (ID: " + character.getId() + ")");
                    }
                } else {
                    System.out.println("    - No characters associated");
                }
            }
        }
        if (project.getEpisodes() != null) {
            System.out.println("Episode Scenes Analysis:");
            for (Episode episode : project.getEpisodes()) {
                System.out.println("  Episode " + episode.getEpisodeNumber() + ":");
                if (episode.getScenes() != null) {
                    for (Scene scene : episode.getScenes()) {
                        System.out.println("    Scene " + scene.getSceneNumber() + ":");
                        if (scene.getCharacters() != null) {
                            for (FilmCharacters character : scene.getCharacters()) {
                                System.out.println("      - " + character.getName() + " (ID: " + character.getId() + ")");
                            }
                        } else {
                            System.out.println("      - No characters associated");
                        }
                    }
                }
            }
        }
        System.out.println("=== END ANALYSIS ===");
    }
}
