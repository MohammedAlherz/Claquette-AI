package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.FilmDTOOUT;
import com.example.claquetteai.DTO.FilmSceneDTOOUT;
import com.example.claquetteai.DTO.SceneDTOOUT;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;

    // Main film generation method (similar to episode generation pattern)
    public void generateFilm(Integer userId, Integer projectId) throws Exception {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (!user.getCompany().getIsSubscribed()) {
            throw new ApiException("You must subscribe to generate films");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("Project not found");
        }
        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("Not authorized");
        }

        if(project.getProjectType().equals("SERIES")){
            throw new ApiException("Project is not Film");
        }

        // Get characters for the project
        List<FilmCharacters> characters = characterRepository.findFilmCharactersByProject(project);
        if (characters.isEmpty()) {
            throw new ApiException("No characters found for this project. Please generate characters first.");
        }

        // Build character names string
        StringBuilder characterNames = new StringBuilder();
        for (int i = 0; i < characters.size(); i++) {
            characterNames.append(characters.get(i).getName());
            if (i < characters.size() - 1) {
                characterNames.append(", ");
            }
        }

        // Generate film with scenes using character consistency
        generateFilmWithScenes(project, characterNames.toString());
    }

    // Core film generation method with character consistency
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

    // Get project film with authorization (returning DTO)
    public FilmDTOOUT getProjectFilm(Integer userId, Integer projectId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("Project not found");
        }
        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("Not authorized");
        }

        Film film = filmRepository.findFilmByProject(project);
        if (film == null) {
            throw new ApiException("No film found for this project");
        }

        return convertToFilmDTO(film);
    }

    // Get film scenes with authorization (returning DTO)
    public Set<FilmSceneDTOOUT> getFilmScenes(Integer userId, Integer projectId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("Project not found");
        }
        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("Not authorized");
        }

        Film film = filmRepository.findFilmByProject(project);
        if (film == null) {
            throw new ApiException("No film found for this project");
        }

        return convertToFilmSceneDTOs(film.getScenes());
    }

    // Converter methods for FILM scenes
    private FilmDTOOUT convertToFilmDTO(Film film) {
        FilmDTOOUT dto = new FilmDTOOUT();
        dto.setTitle(film.getTitle());
        dto.setSummary(film.getSummary());
        return dto;
    }

    private Set<FilmSceneDTOOUT> convertToFilmSceneDTOs(Set<Scene> scenes) {
        return scenes.stream()
                .map(this::convertToFilmSceneDTO)
                .collect(Collectors.toSet());
    }

    private FilmSceneDTOOUT convertToFilmSceneDTO(Scene scene) {
        FilmSceneDTOOUT dto = new FilmSceneDTOOUT();
        dto.setSceneNumber(scene.getSceneNumber());
        dto.setSetting(scene.getSetting());
        dto.setActions(scene.getActions());
        dto.setDialogue(scene.getDialogue());
        dto.setDepartmentNotes(scene.getDepartmentNotes());
        return dto;
    }
    // Utility method to validate film character consistency
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

    // Get all films for a user (for dashboard/analytics)
    public List<FilmDTOOUT> getUserFilms(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        List<Project> projects = projectRepository.findProjectsByCompany_User_Id(userId);
        List<Film> films = filmRepository.findFilmsByProjectIn(projects);

        return films.stream()
                .map(this::convertToFilmDTO)
                .collect(Collectors.toList());
    }


}