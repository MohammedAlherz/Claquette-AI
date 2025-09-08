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

import java.util.List;
import java.util.Set;
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

    public List<Episode> getMyEpisodes(Integer userId, Integer projectId) {
        // You can add user validation here if needed
        return episodeRepository.findByProjectId(projectId);
    }


    public void generateEpisodes(Integer userId, Integer projectId) throws Exception {
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        if (!user.getCompany().getIsSubscribed()){
            throw new ApiException("you must subscribe to generate one by one");
        }
        Project project = projectRepository.findProjectById(projectId);
        if (project == null){
            throw new ApiException("project not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorized");
        }
        if(project.getProjectType().equals("FILM")){
            throw new ApiException("Project is not Series");
        }

        List<FilmCharacters> characters = characterRepository.findFilmCharactersByProject(project);
        if (characters.isEmpty()) {
            throw new ApiException("No characters found for this project. Please generate characters first.");
        }

        // FIXED: Get all character names together and generate episodes sequentially
        String characterNames = characters.stream()
                .map(FilmCharacters::getName)
                .collect(Collectors.joining(", "));

        // Generate episodes 1, 2, 3... up to the project's episode count
        for (int episodeNumber = 1; episodeNumber <= project.getEpisodeCount(); episodeNumber++) {
            generateEpisodeWithScenes(project, episodeNumber, characterNames);
        }
    }

    // Get project episode with authorization
    public EpisodeDTOOUT getProjectEpisode(Integer userId, Integer projectId, Integer episodeId) {
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

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException("Episode not found"));

        if (!episode.getProject().equals(project)) {
            throw new ApiException("Episode does not belong to this project");
        }

        return convertToEpisodeDTO(episode);
    }

    // Get episode scenes with authorization
    public Set<SceneDTOOUT> getEpisodeScenes(Integer userId, Integer projectId, Integer episodeId) {
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

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException("Episode not found"));

        if (!episode.getProject().equals(project)) {
            throw new ApiException("Episode does not belong to this project");
        }

        return convertToSceneDTOs(episode.getScenes());
    }


    // Converter methods
    private EpisodeDTOOUT convertToEpisodeDTO(Episode episode) {
        EpisodeDTOOUT dto = new EpisodeDTOOUT();
        dto.setEpisodeNumber(episode.getEpisodeNumber());
        dto.setTitle(episode.getTitle());
        dto.setSummary(episode.getSummary());
        return dto;
    }

    private Set<SceneDTOOUT> convertToSceneDTOs(Set<Scene> scenes) {
        return scenes.stream()
                .map(this::convertToSceneDTO)
                .collect(Collectors.toSet());
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
    // Get episodes for a specific project with authorization
    public List<EpisodeDTOOUT> getProjectEpisodes(Integer userId, Integer projectId) {
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

        List<Episode> episodes = episodeRepository.findByProjectId(projectId);

        return episodes.stream()
                .map(this::convertToEpisodeDTO)
                .collect(Collectors.toList());
    }
}