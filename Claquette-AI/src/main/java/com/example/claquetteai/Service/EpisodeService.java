package com.example.claquetteai.Service;

import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;

    // UPDATED METHOD: AI Generation method with character consistency
    public Episode generateEpisodeWithScenes(Project project, int episodeNumber, String characterNames) throws Exception {
        System.out.println("=== GENERATING EPISODE " + episodeNumber + " ===");
        System.out.println("Project: " + project.getTitle());
        System.out.println("Available Characters: " + characterNames);

        // Build prompt for specific episode with scenes using consistent character names
        String prompt = promptBuilderService.episodePrompt(project.getDescription(), episodeNumber, characterNames);

        System.out.println("=== EPISODE PROMPT PREVIEW ===");
        System.out.println("Character Names Injected: " + characterNames);
        System.out.println("Prompt Length: " + prompt.length() + " characters");

        // Get AI response and extract episode with scenes
        System.out.println("Calling AI service for episode generation...");
        String json = aiClientService.askModel(prompt);

        System.out.println("=== AI RESPONSE RECEIVED ===");
        System.out.println("Response Length: " + json.length() + " characters");
        System.out.println("Response Preview: " + json.substring(0, Math.min(200, json.length())) + "...");

        // Extract episode with scenes using the updated JSON extractor
        Episode episode = jsonExtractor.extractEpisodeWithScenes(json, project, episodeNumber);

        System.out.println("=== EPISODE EXTRACTION COMPLETE ===");
        System.out.println("Episode Title: " + episode.getTitle());
        System.out.println("Scene Count: " + (episode.getScenes() != null ? episode.getScenes().size() : 0));

        // Validate scene-character consistency
        if (episode.getScenes() != null) {
            int totalCharacterAssociations = 0;
            for (var scene : episode.getScenes()) {
                if (scene.getCharacters() != null) {
                    totalCharacterAssociations += scene.getCharacters().size();
                    System.out.println("Scene " + scene.getSceneNumber() + " has " +
                            scene.getCharacters().size() + " character associations");
                }
            }
            System.out.println("Total character-scene associations: " + totalCharacterAssociations);
        }

        // Save and return the episode
        Episode savedEpisode = episodeRepository.save(episode);
        System.out.println("Episode saved with ID: " + savedEpisode.getId());
        System.out.println("=== EPISODE GENERATION COMPLETE ===");

        return savedEpisode;
    }

    // UTILITY METHOD: Validate episode character consistency
    public void validateEpisodeCharacterConsistency(Episode episode, String expectedCharacterNames) {
        if (episode.getScenes() == null || expectedCharacterNames == null) {
            return;
        }

        String[] expectedNames = expectedCharacterNames.split(",");
        for (int i = 0; i < expectedNames.length; i++) {
            expectedNames[i] = expectedNames[i].trim();
        }

        System.out.println("=== VALIDATING CHARACTER CONSISTENCY ===");
        System.out.println("Expected Characters: " + String.join(", ", expectedNames));

        for (var scene : episode.getScenes()) {
            if (scene.getCharacters() != null) {
                for (var character : scene.getCharacters()) {
                    boolean found = false;
                    for (String expectedName : expectedNames) {
                        if (expectedName.equals(character.getName())) {
                            found = true;
                            break;
                        }
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

    // CRUD METHOD: Get episodes for a specific user and project
    public List<Episode> getMyEpisodes(Integer userId, Integer projectId) {
        // You can add user validation here if needed
        return episodeRepository.findByProjectId(projectId);
    }

    // ALTERNATIVE: Get episodes with user validation
    public List<Episode> getMyEpisodesWithValidation(Integer userId, Integer projectId) {
        // Add validation logic here if you need to check user permissions
        // For example: verify that the user owns the project
        List<Episode> episodes = episodeRepository.findByProjectId(projectId);
        return episodes;
    }

}