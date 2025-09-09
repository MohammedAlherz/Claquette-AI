package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.FilmDTOOUT;
import com.example.claquetteai.DTO.FilmSceneDTOOUT;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
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

    // === Public entrypoint used elsewhere (kept as you had it) ===
    public void generateFilm(Integer userId, Integer projectId) throws Exception {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");
        if (!user.getCompany().getIsSubscribed()) {
            throw new ApiException("You must subscribe to generate films");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("Project not found");
        if (!project.getCompany().getUser().equals(user)) throw new ApiException("Not authorized");
        if ("SERIES".equals(project.getProjectType())) throw new ApiException("Project is not Film");

        List<FilmCharacters> characters = characterRepository.findFilmCharactersByProject(project);
        if (characters.isEmpty()) {
            throw new ApiException("No characters found for this project. Please generate characters first.");
        }

        String characterNames = characters.stream()
                .map(FilmCharacters::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        generateFilmWithScenes(project, characterNames);
    }

    // === Core film generation with robust JSON handling + graph normalization ===
    public Film generateFilmWithScenes(Project project, String characterNames) throws Exception {
        System.out.println("=== GENERATING FILM ===");
        System.out.println("Project: " + project.getTitle());
        System.out.println("Available Characters: " + characterNames);

        // 1) Build strict JSON prompt (wrap your existing prompt with hard rules)
        String basePrompt = promptBuilderService.filmPrompt(project.getDescription(), characterNames);
        String strictPrompt = addStrictJsonRules(basePrompt);

        System.out.println("=== FILM PROMPT PREVIEW ===");
        System.out.println("Character Names Injected: " + characterNames);
        System.out.println("Prompt Length: " + strictPrompt.length() + " characters");

        // 2) Call model (attempt #1)
        System.out.println("Calling AI service for film generation...");
        String aiText = aiClientService.askModel(strictPrompt);

        System.out.println("=== AI RESPONSE RECEIVED ===");
        System.out.println("Response Length: " + aiText.length() + " characters");
        System.out.println("Response Preview: " + aiText.substring(0, Math.min(200, aiText.length())) + "...");

        Film film;
        try {
            film = jsonExtractor.extractFilmWithScenes(aiText, project);
        } catch (Exception first) {
            // 3) Corrective retry: ask model to convert its own output to strict JSON
            String corrective = buildCorrectiveJsonPrompt(aiText);
            String retryText = aiClientService.askModel(corrective);

            // Helpful logging for diagnostics
            System.out.println("=== AI CORRECTIVE RETRY ===");
            System.out.println("Retry Response Preview: " + retryText.substring(0, Math.min(200, retryText.length())) + "...");

            try {
                film = jsonExtractor.extractFilmWithScenes(retryText, project);
            } catch (Exception second) {
                String head = aiText.substring(0, Math.min(aiText.length(), 500));
                throw new ApiException("AI JSON parse error after retry. First 500 chars of initial response:\n" + head);
            }
        }

        // 4) Normalize graph: set back-refs and attach scene characters to existing project characters
        normalizeFilmGraph(film, project);

        // 5) Optional consistency validation you already have
        if (film.getScenes() != null) {
            int totalCharacterAssociations = film.getScenes().stream()
                    .mapToInt(s -> s.getCharacters() == null ? 0 : s.getCharacters().size())
                    .sum();
            System.out.println("Total character-scene associations: " + totalCharacterAssociations);
        }

        // 6) Persist
        Film savedFilm = filmRepository.save(film);
        System.out.println("Film saved with ID: " + savedFilm.getId());
        System.out.println("=== FILM GENERATION COMPLETE ===");
        return savedFilm;
    }

    // === DTO reads (unchanged) ===
    public FilmDTOOUT getProjectFilm(Integer userId, Integer projectId) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("Project not found");
        if (!project.getCompany().getUser().equals(user)) throw new ApiException("Not authorized");

        Film film = filmRepository.findFilmByProject(project);
        if (film == null) throw new ApiException("No film found for this project");

        return convertToFilmDTO(film);
    }

    public Set<FilmSceneDTOOUT> getFilmScenes(Integer userId, Integer projectId) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("Project not found");
        if (!project.getCompany().getUser().equals(user)) throw new ApiException("Not authorized");

        Film film = filmRepository.findFilmByProject(project);
        if (film == null) throw new ApiException("No film found for this project");

        return convertToFilmSceneDTOs(film.getScenes());
    }

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

    // === Consistency validator you already have (unchanged) ===
    public void validateFilmCharacterConsistency(Film film, String expectedCharacterNames) {
        if (film.getScenes() == null || expectedCharacterNames == null) return;

        String[] expectedNames = Arrays.stream(expectedCharacterNames.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

        System.out.println("=== VALIDATING FILM CHARACTER CONSISTENCY ===");
        System.out.println("Expected Characters: " + String.join(", ", expectedNames));

        for (var scene : film.getScenes()) {
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

    public List<FilmDTOOUT> getUserFilms(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("User not found");

        List<Project> projects = projectRepository.findProjectsByCompany_User_Id(userId);
        List<Film> films = filmRepository.findFilmsByProjectIn(projects);

        return films.stream().map(this::convertToFilmDTO).collect(Collectors.toList());
    }

    // ========================= Helpers =========================

    /** Add hard JSON rules in front of your base prompt so the model returns valid JSON. */
    private String addStrictJsonRules(String basePrompt) {
        return """
               Return ONLY STRICT MINIFIED JSON that matches:
               {
                 "title": "string",
                 "summary": "string",
                 "durationMinutes": 120,
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
               - Double quotes for all keys/strings.
               - No trailing commas.
               - sceneNumber starts at 1 and increments by 1.
               - characters: use ONLY names from the provided list exactly.
               
               Content to base on:
               """ + basePrompt;
    }

    /** If first parse fails, ask the model to convert its own text to strict JSON. */
    private String buildCorrectiveJsonPrompt(String aiText) {
        return """
               Convert the following content to STRICT MINIFIED JSON with keys:
               title, summary, durationMinutes, scenes[].sceneNumber, scenes[].setting, scenes[].description, scenes[].actions, scenes[].dialogue, scenes[].departmentNotes, scenes[].characters (array of strings).
               
               Output ONLY JSON (no prose, no markdown, no comments).
               Use double quotes for keys/strings. No trailing commas.
               
               Content:
               """ + aiText;
    }

    /**
     * Normalize object graph before saving:
     * - set film.project
     * - set scene.film (and ensure sceneNumber positive)
     * - attach scene.characters to EXISTING FilmCharacters of this project by exact name match
     */
    private void normalizeFilmGraph(Film film, Project project) {
        if (film == null) throw new ApiException("Film is null after extraction");

        film.setProject(project);

        // Build lookup: name -> FilmCharacters (managed for this project)
        List<FilmCharacters> projectChars = characterRepository.findFilmCharactersByProject(project);
        Map<String, FilmCharacters> byName = new HashMap<>();
        for (FilmCharacters fc : projectChars) {
            if (fc.getName() != null) byName.put(fc.getName(), fc);
        }

        if (film.getScenes() != null) {
            for (Scene s : film.getScenes()) {
                s.setFilm(film);

                if (s.getSceneNumber() == null || s.getSceneNumber() <= 0) {
                    s.setSceneNumber(1);
                }

                // Replace transient characters with attached ones by exact name
                if (s.getCharacters() != null && !s.getCharacters().isEmpty()) {
                    Set<FilmCharacters> remapped = new HashSet<>();
                    for (FilmCharacters c : s.getCharacters()) {
                        FilmCharacters attached = (c != null && c.getName() != null) ? byName.get(c.getName()) : null;
                        if (attached != null) {
                            remapped.add(attached);
                        }
                    }
                    s.setCharacters(remapped);
                }
            }
        }
    }
}
