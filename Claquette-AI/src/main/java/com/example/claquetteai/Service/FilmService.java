package com.example.claquetteai.Service;

import com.example.claquetteai.Model.Film;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;

    // UPDATED METHOD: AI Generation method with character consistency
    public Film generateFilmWithScenes(Project project, String characterNames) throws Exception {
        System.out.println("=== GENERATING FILM ===");
        System.out.println("Project: " + project.getTitle());
        System.out.println("Available Characters: " + characterNames);

        // Build prompt for film generation using consistent character names
        String prompt = promptBuilderService.filmPrompt(project.getDescription(), characterNames);

        System.out.println("=== FILM PROMPT PREVIEW ===");
        System.out.println("Character Names Injected: " + characterNames);
        System.out.println("Prompt Length: " + prompt.length() + " characters");

        // Get AI response and extract film with scenes
        System.out.println("Calling AI service for film generation...");
        String json = aiClientService.askModel(prompt);

        System.out.println("=== AI RESPONSE RECEIVED ===");
        System.out.println("Response Length: " + json.length() + " characters");
        System.out.println("Response Preview: " + json.substring(0, Math.min(200, json.length())) + "...");

        // Extract film with scenes using the updated JSON extractor
        Film film = jsonExtractor.extractFilmWithScenes(json, project);

        System.out.println("=== FILM EXTRACTION COMPLETE ===");
        System.out.println("Film Title: " + film.getTitle());
        System.out.println("Scene Count: " + (film.getScenes() != null ? film.getScenes().size() : 0));
        System.out.println("Duration: " + film.getDurationMinutes() + " minutes");

        // Validate scene-character consistency
        if (film.getScenes() != null) {
            int totalCharacterAssociations = 0;
            for (var scene : film.getScenes()) {
                if (scene.getCharacters() != null) {
                    totalCharacterAssociations += scene.getCharacters().size();
                    System.out.println("Scene " + scene.getSceneNumber() + " has " +
                            scene.getCharacters().size() + " character associations");
                }
            }
            System.out.println("Total character-scene associations: " + totalCharacterAssociations);
        }

        // Save and return the film
        Film savedFilm = filmRepository.save(film);
        System.out.println("Film saved with ID: " + savedFilm.getId());
        System.out.println("=== FILM GENERATION COMPLETE ===");

        return savedFilm;
    }

    // UTILITY METHOD: Validate film character consistency
    public void validateFilmCharacterConsistency(Film film, String expectedCharacterNames) {
        if (film.getScenes() == null || expectedCharacterNames == null) {
            return;
        }

        String[] expectedNames = expectedCharacterNames.split(",");
        for (int i = 0; i < expectedNames.length; i++) {
            expectedNames[i] = expectedNames[i].trim();
        }

        System.out.println("=== VALIDATING FILM CHARACTER CONSISTENCY ===");
        System.out.println("Expected Characters: " + String.join(", ", expectedNames));

        for (var scene : film.getScenes()) {
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

}