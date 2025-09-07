package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Main method to generate complete screenplay with character consistency
     */
    @Transactional
    public Project generateFullScreenplay(Integer projectId, Integer userId) throws Exception {
        // Step 1: Get existing project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        if (!user.getCompany().getIsSubscribed() && user.getUseAI() <= 0) {
            throw new ApiException("you cannot generate project using AI subscribe");
        }

        // Step 2: Generate characters for the project using CharacterService
        Set<FilmCharacters> characters = characterService.generateCharacters(project, project.getDescription());
        project.setCharacters(characters);

        // CRITICAL: Save the project with characters first to establish the relationship
        project = projectRepository.save(project);

        // LOGS: Extract character names for consistency
        String characterNames = extractCharacterNames(characters);
        System.out.println("=== CHARACTER CONSISTENCY CHECK ===");
        System.out.println("Generated Characters: " + characterNames);
        System.out.println("Character Count: " + characters.size());
        System.out.println("===================================");

        // Step 3: Generate Film OR Episodes based on project type
        if ("FILM".equals(project.getProjectType())) {
            // Generate film using FilmService with character names for consistency
            Film film = filmService.generateFilmWithScenes(project, characterNames);
            project.setFilms(film);

            // Explicitly save each scene to persist many-to-many relationships
            if (film.getScenes() != null) {
                for (Scene scene : film.getScenes()) {
                    // Make sure the scene has proper character associations
                    if (scene.getCharacters() != null && !scene.getCharacters().isEmpty()) {
                        // Save the scene - this will trigger the cascade to persist relationships
                        sceneRepository.save(scene);
                        System.out.println("Saved film scene " + scene.getSceneNumber() + " with " +
                                scene.getCharacters().size() + " characters");
                    }
                }
            }

            // Validate film character consistency
            filmService.validateFilmCharacterConsistency(film, characterNames);

        } else {
            // For series, determine episode count and generate episodes using EpisodeService
            int episodeCount = project.getEpisodeCount();
            Set<Episode> episodes = new HashSet<>();

            for (int i = 1; i <= episodeCount; i++) {
                // Pass character names to episode generation for consistency
                Episode episode = episodeService.generateEpisodeWithScenes(project, i, characterNames);
                episodes.add(episode);

                // Explicitly save each scene to persist many-to-many relationships
                if (episode.getScenes() != null) {
                    for (Scene scene : episode.getScenes()) {
                        // Make sure the scene has proper character associations
                        if (scene.getCharacters() != null && !scene.getCharacters().isEmpty()) {
                            // Save the scene - this will trigger the cascade to persist relationships
                            sceneRepository.save(scene);
                            System.out.println("Saved episode " + i + " scene " + scene.getSceneNumber() +
                                    " with " + scene.getCharacters().size() + " characters");
                        }
                    }
                }

                // Validate episode character consistency
                episodeService.validateEpisodeCharacterConsistency(episode, characterNames);
            }

            project.setEpisodes(episodes);
        }

        // Step 4: Generate casting recommendations using CastingService
        Set<CastingRecommendation> casting = castingService.generateCasting(project);
        project.setCastingRecommendations(casting);

        // Update user AI usage
        user.setUseAI(user.getUseAI() - 1);
        userRepository.save(user);

        // Save final project with all relationships
        Project finalProject = projectRepository.save(project);

        // Run final consistency analysis
        System.out.println("=== FINAL PROJECT ANALYSIS ===");
        debugCharacterSceneRelationships(finalProject);

        return finalProject;
    }

    /**
     * Helper method to extract character names as comma-separated string for AI consistency
     */
    private String extractCharacterNames(Set<FilmCharacters> characters) {
        if (characters == null || characters.isEmpty()) {
            System.out.println("WARNING: No characters provided for consistency check");
            return "";
        }

        String characterNames = characters.stream()
                .map(FilmCharacters::getName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .collect(Collectors.joining(", "));

        System.out.println("Extracted character names for AI: " + characterNames);
        return characterNames;
    }

    /**
     * Debug method to analyze character-scene relationships
     */
    public void debugCharacterSceneRelationships(Project project) {
        System.out.println("=== CHARACTER-SCENE RELATIONSHIP ANALYSIS ===");

        if (project.getCharacters() != null) {
            System.out.println("Project Characters (" + project.getCharacters().size() + "):");
            for (FilmCharacters character : project.getCharacters()) {
                System.out.println("  - " + character.getName() + " (ID: " + character.getId() + ")");
            }
        }

        // Analyze film scenes if it's a film
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

        // Analyze episode scenes if it's a series
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