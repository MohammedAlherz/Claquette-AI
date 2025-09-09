package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.EpisodeDTOOUT;
import com.example.claquetteai.DTO.SceneDTOOUT;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.CharacterRepository;
import com.example.claquetteai.Repository.EpisodeRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final CharacterRepository characterRepository;

    /** Generate a single episode (with robust JSON handling + graph normalization). */
    public Episode generateEpisodeWithScenes(Project project, int episodeNumber, String characterNames) throws Exception {
        System.out.println("=== GENERATING EPISODE " + episodeNumber + " ===");
        System.out.println("Project: " + project.getTitle());
        System.out.println("Available Characters: " + characterNames);

        // 1) Build strict JSON prompt around your base episode prompt
        String basePrompt = promptBuilderService.episodePrompt(project.getDescription(), episodeNumber, characterNames);
        String strictPrompt = addStrictJsonRules(basePrompt, episodeNumber);

        System.out.println("=== EPISODE PROMPT PREVIEW ===");
        System.out.println("Character Names Injected: " + characterNames);
        System.out.println("Prompt Length: " + strictPrompt.length() + " characters");

        // 2) Call model (attempt #1)
        System.out.println("Calling AI service for episode generation...");
        String aiText = aiClientService.askModel(strictPrompt);

        System.out.println("=== AI RESPONSE RECEIVED ===");
        System.out.println("Response Length: " + aiText.length() + " characters");
        System.out.println("Response Preview: " + aiText.substring(0, Math.min(200, aiText.length())) + "...");

        Episode episode;
        try {
            episode = jsonExtractor.extractEpisodeWithScenes(aiText, project, episodeNumber);
        } catch (Exception first) {
            // 3) Corrective retry: ask model to convert its own output into strict JSON
            String corrective = buildCorrectiveJsonPrompt(aiText, episodeNumber);
            String retryText = aiClientService.askModel(corrective);

            System.out.println("=== AI CORRECTIVE RETRY ===");
            System.out.println("Retry Response Preview: " + retryText.substring(0, Math.min(200, retryText.length())) + "...");

            try {
                episode = jsonExtractor.extractEpisodeWithScenes(retryText, project, episodeNumber);
            } catch (Exception second) {
                String head = aiText.substring(0, Math.min(aiText.length(), 500));
                throw new ApiException("AI JSON parse error for episode " + episodeNumber + " after retry. First 500 chars of initial response:\n" + head);
            }
        }

        // 4) Normalize graph: back-refs + attach scene characters to existing project chars by exact name
        normalizeEpisodeGraph(episode, project);

        // 5) Logging + light stats
        System.out.println("=== EPISODE EXTRACTION COMPLETE ===");
        System.out.println("Episode Title: " + episode.getTitle());
        System.out.println("Scene Count: " + (episode.getScenes() != null ? episode.getScenes().size() : 0));
        if (episode.getScenes() != null) {
            int associations = episode.getScenes().stream()
                    .mapToInt(s -> s.getCharacters() == null ? 0 : s.getCharacters().size()).sum();
            System.out.println("Total character-scene associations: " + associations);
        }

        // 6) Persist
        Episode saved = episodeRepository.save(episode);
        System.out.println("Episode saved with ID: " + saved.getId());
        System.out.println("=== EPISODE GENERATION COMPLETE ===");
        return saved;
    }

    /** Generate all episodes sequentially using the same character list. */
    public void generateEpisodes(Integer userId, Integer projectId) throws Exception {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("user not found");
        if (!user.getCompany().getIsSubscribed()) throw new ApiException("you must subscribe to generate one by one");

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("project not found");
        if (!project.getCompany().getUser().equals(user)) throw new ApiException("not authorized");

        if ("FILM".equals(project.getProjectType())) {
            project.setEpisodeCount(1);
        }

        List<FilmCharacters> chars = characterRepository.findFilmCharactersByProject(project);
        if (chars.isEmpty()) throw new ApiException("No characters found for this project. Please generate characters first.");

        String characterNames = chars.stream()
                .map(FilmCharacters::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        for (int ep = 1; ep <= project.getEpisodeCount(); ep++) {
            generateEpisodeWithScenes(project, ep, characterNames);
        }
    }

    // ===================== Reads / DTOs (unchanged structure) =====================

    public List<Episode> getMyEpisodes(Integer userId, Integer projectId) {
        return episodeRepository.findByProjectId(projectId);
    }

    public EpisodeDTOOUT getProjectEpisode(Integer userId, Integer projectId, Integer episodeId) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("Project not found");
        if (!project.getCompany().getUser().equals(user)) throw new ApiException("Not authorized");

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException("Episode not found"));

        if (!episode.getProject().equals(project)) {
            throw new ApiException("Episode does not belong to this project");
        }
        return convertToEpisodeDTO(episode);
    }

    public Set<SceneDTOOUT> getEpisodeScenes(Integer userId, Integer projectId, Integer episodeId) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("Project not found");
        if (!project.getCompany().getUser().equals(user)) throw new ApiException("Not authorized");

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException("Episode not found"));

        if (!episode.getProject().equals(project)) {
            throw new ApiException("Episode does not belong to this project");
        }
        return convertToSceneDTOs(episode.getScenes());
    }

    public List<EpisodeDTOOUT> getProjectEpisodes(Integer userId, Integer projectId) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("Project not found");
        if (!project.getCompany().getUser().equals(user)) throw new ApiException("Not authorized");

        List<Episode> episodes = episodeRepository.findByProjectId(projectId);
        return episodes.stream().map(this::convertToEpisodeDTO).collect(Collectors.toList());
    }

    // ===================== Converters (unchanged) =====================

    private EpisodeDTOOUT convertToEpisodeDTO(Episode episode) {
        EpisodeDTOOUT dto = new EpisodeDTOOUT();
        dto.setEpisodeNumber(episode.getEpisodeNumber());
        dto.setTitle(episode.getTitle());
        dto.setSummary(episode.getSummary());
        return dto;
    }

    private Set<SceneDTOOUT> convertToSceneDTOs(Set<Scene> scenes) {
        return scenes.stream().map(this::convertToSceneDTO).collect(Collectors.toSet());
    }

    private SceneDTOOUT convertToSceneDTO(Scene scene) {
        String dialogue = scene.getDialogue();
        Integer episodeNumber = null;
        String episodeTitle = null;

        if (scene.getEpisode() != null) {
            episodeNumber = scene.getEpisode().getEpisodeNumber();
            episodeTitle = scene.getEpisode().getTitle();
        }
        return new SceneDTOOUT(dialogue, episodeNumber, episodeTitle);
    }

    // ===================== JSON Prompting & Normalization =====================

    /** Hard “strict JSON” rules layered on top of your base episode prompt. */
    private String addStrictJsonRules(String basePrompt, int episodeNumber) {
        return """
               Return ONLY STRICT MINIFIED JSON that matches:
               {
                 "episodeNumber": %d,
                 "title": "string",
                 "summary": "string",
                 "scenes": [
                   {
                     "sceneNumber": 1,
                     "setting": "string",
                     "description": "string",
                     "actions": "string",
                     "dialogue": "string",
                     "departmentNotes": "string",
                     "characters": ["Name1","Name2"]
                   }
                 ]
               }
               Rules:
               - No prose, no markdown, no code fences.
               - Double quotes for all keys/strings; no trailing commas.
               - episodeNumber = %d exactly.
               - sceneNumber starts at 1 and increments by 1.
               - characters[] must use ONLY names from the provided list exactly.
               
               Content to base on:
               """.formatted(episodeNumber, episodeNumber) + basePrompt;
    }

    /** If the first parse fails, ask the model to convert its own output to strict JSON. */
    private String buildCorrectiveJsonPrompt(String aiText, int episodeNumber) {
        return """
               Convert the following content to STRICT MINIFIED JSON with keys:
               episodeNumber (=%d), title, summary, scenes[].sceneNumber, scenes[].setting, scenes[].description, scenes[].actions, scenes[].dialogue, scenes[].departmentNotes, scenes[].characters (array of strings).
               
               Output ONLY JSON (no prose, no markdown, no comments).
               Use double quotes for keys/strings. No trailing commas.
               
               Content:
               """.formatted(episodeNumber) + aiText;
    }

    /**
     * Normalize object graph before saving:
     * - set episode.project
     * - set scene.episode (ensure sceneNumber positive)
     * - attach scene.characters to EXISTING FilmCharacters of this project by exact name
     */
    private void normalizeEpisodeGraph(Episode episode, Project project) {
        if (episode == null) throw new ApiException("Episode is null after extraction");

        episode.setProject(project);
        if (episode.getEpisodeNumber() == null || episode.getEpisodeNumber() <= 0) {
            episode.setEpisodeNumber(1);
        }

        // Build lookup: name -> FilmCharacters (managed for this project)
        List<FilmCharacters> projectChars = characterRepository.findFilmCharactersByProject(project);
        Map<String, FilmCharacters> byName = new HashMap<>();
        for (FilmCharacters fc : projectChars) {
            if (fc.getName() != null) byName.put(fc.getName(), fc);
        }

        if (episode.getScenes() != null) {
            for (Scene s : episode.getScenes()) {
                s.setEpisode(episode);
                if (s.getSceneNumber() == null || s.getSceneNumber() <= 0) {
                    s.setSceneNumber(1);
                }

                if (s.getCharacters() != null && !s.getCharacters().isEmpty()) {
                    Set<FilmCharacters> remapped = new HashSet<>();
                    for (FilmCharacters c : s.getCharacters()) {
                        FilmCharacters attached = (c != null && c.getName() != null) ? byName.get(c.getName()) : null;
                        if (attached != null) remapped.add(attached);
                    }
                    s.setCharacters(remapped);
                }
            }
        }
    }

    // ===================== Consistency checker (unchanged behavior) =====================

    public void validateEpisodeCharacterConsistency(Episode episode, String expectedCharacterNames) {
        if (episode.getScenes() == null || expectedCharacterNames == null) return;

        String[] expectedNames = Arrays.stream(expectedCharacterNames.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

        System.out.println("=== VALIDATING CHARACTER CONSISTENCY ===");
        System.out.println("Expected Characters: " + String.join(", ", expectedNames));

        for (var scene : episode.getScenes()) {
            if (scene.getCharacters() != null) {
                for (var character : scene.getCharacters()) {
                    boolean found = false;
                    for (String expectedName : expectedNames) {
                        if (expectedName.equals(character.getName())) { found = true; break; }
                    }
                    if (!found) {
                        System.out.println("WARNING: Scene " + scene.getSceneNumber() +
                                " contains unexpected character: " + character.getName());
                    }
                }
            }
        }
        System.out.println("=== VALIDATION COMPLETE ===");
    }
}
