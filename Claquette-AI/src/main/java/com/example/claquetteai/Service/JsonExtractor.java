package com.example.claquetteai.Service;

import com.example.claquetteai.Model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

@Service
public class JsonExtractor {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Extracts basic project information from AI JSON response
     * Creates a Project entity with essential fields populated
     */
    public Project extractProject(String json) throws Exception {
        // Fix assumptions formatting issues before parsing
        json = fixAssumptionsFormat(json);

        // Parse the cleaned JSON
        JsonNode root = mapper.readTree(json);
        JsonNode projectNode = root.path("project");

        // Create new project entity
        Project project = new Project();

        // Set basic project information
        project.setTitle(projectNode.path("title").asText());
        project.setDescription(projectNode.path("story_description").asText());

        // Determine project type based on episode count
        int episodesCount = projectNode.path("episodes_count").asInt(1);
        project.setProjectType(episodesCount == 1 ? "FILM" : "SERIES");

        // Extract optional fields from AI response
        if (projectNode.has("genre")) {
            project.setGenre(projectNode.path("genre").asText());
        }

        if (projectNode.has("target_audience")) {
            project.setTargetAudience(projectNode.path("target_audience").asText());
        }

        // Handle assumptions - append to description if present
        if (projectNode.has("assumptions")) {
            List<String> assumptions = extractAssumptions(projectNode.get("assumptions"));
            if (!assumptions.isEmpty()) {
                project.setDescription(project.getDescription() + "\n\nAssumptions: " + String.join("; ", assumptions));
            }
        }

        return project;
    }

    /**
     * Extracts Film with scenes from AI JSON response
     * Creates Film entity with scenes and character associations
     */
    public Film extractFilmWithScenes(String json, Project project) throws Exception {
        JsonNode root = mapper.readTree(json);

        // Create Film entity
        Film film = new Film();
        film.setProject(project);
        film.setTitle(project.getTitle());
        film.setCreatedAt(LocalDateTime.now());
        film.setUpdatedAt(LocalDateTime.now());

        // Extract film details
        JsonNode filmNode = root.path("film");
        if (filmNode.has("summary")) {
            film.setSummary(filmNode.path("summary").asText());
        } else {
            film.setSummary(project.getDescription());
        }

        if (filmNode.has("duration_minutes")) {
            film.setDurationMinutes(filmNode.path("duration_minutes").asInt());
        }

        // Create character map for linking scenes to characters
        Map<String, FilmCharacters> characterMap = createCharacterMapFromProject(project);

        // Extract scenes for this film with character associations
        Set<Scene> scenes = extractScenesForFilm(filmNode, film, characterMap);
        film.setScenes(scenes);

        return film;
    }

    /**
     * ENHANCED: Extracts scenes for film with improved dialogue structure
     */
    private Set<Scene> extractScenesForFilm(JsonNode filmNode, Film film, Map<String, FilmCharacters> characterMap) {
        Set<Scene> scenes = new HashSet<>();
        int sceneCounter = 1;

        for (JsonNode sceneNode : filmNode.path("scenes")) {
            Scene scene = new Scene();

            scene.setSceneNumber(sceneCounter++);
            scene.setSetting(sceneNode.path("slug").asText());
            scene.setActions(sceneNode.path("action").asText());
            scene.setFilm(film);

            // ENHANCED: Extract dialogue with proper character separation
            Set<FilmCharacters> sceneCharacters = new HashSet<>();
            String formattedDialogue = extractAndFormatDialogue(sceneNode, characterMap, sceneCharacters);

            scene.setDialogue(formattedDialogue);
            scene.setCharacters(sceneCharacters);

            // Technical notes
            StringBuilder notes = new StringBuilder();
            if (sceneNode.has("sound")) {
                notes.append("Sound: ").append(sceneNode.path("sound").asText()).append(" | ");
            }
            if (sceneNode.has("mood_light")) {
                notes.append("Mood: ").append(sceneNode.path("mood_light").asText()).append(" | ");
            }
            if (sceneNode.has("purpose")) {
                notes.append("Purpose: ").append(sceneNode.path("purpose").asText()).append(" | ");
            }
            scene.setDepartmentNotes(notes.toString().trim());

            scene.setCreatedAt(LocalDateTime.now());
            scene.setUpdatedAt(LocalDateTime.now());

            scenes.add(scene);
        }

        return scenes;
    }

    /**
     * Extracts single episode with scenes from AI JSON response
     */
    public Episode extractEpisodeWithScenes(String json, Project project, int episodeNumber) throws Exception {
        JsonNode root = mapper.readTree(json);

        Episode episode = new Episode();
        episode.setProject(project);
        episode.setEpisodeNumber(episodeNumber);
        episode.setCreatedAt(LocalDateTime.now());
        episode.setUpdatedAt(LocalDateTime.now());

        // Look for episode data in the JSON
        JsonNode episodeNode = null;

        // Try different possible structures
        if (root.has("episode")) {
            episodeNode = root.path("episode");
        } else if (root.has("episodes") && root.path("episodes").isArray() && root.path("episodes").size() > 0) {
            episodeNode = root.path("episodes").get(0);
        } else {
            // Fallback - use project data
            episode.setTitle("Episode " + episodeNumber);
            episode.setSummary(project.getDescription());
            episode.setScenes(new HashSet<>());
            return episode;
        }

        // Use user-provided data or fallback to project description
        episode.setTitle(episodeNode.path("title").asText("Episode " + episodeNumber));
        episode.setSummary(episodeNode.path("summary").asText(project.getDescription()));

        // Only set duration if provided by AI
        if (episodeNode.has("duration_minutes")) {
            episode.setDurationMinutes(episodeNode.path("duration_minutes").asInt());
        }

        // Create character map for linking scenes to characters
        Map<String, FilmCharacters> characterMap = createCharacterMapFromProject(project);

        // Extract scenes for this episode with character associations
        Set<Scene> scenes = extractScenesFromNode(episodeNode, episode, characterMap);
        episode.setScenes(scenes);

        return episode;
    }

    /**
     * Extracts episodes and their scenes from AI JSON response (legacy method)
     */
    public Set<Episode> extractEpisodes(String json, Project project) throws Exception {
        JsonNode root = mapper.readTree(json);
        Set<Episode> episodes = new HashSet<>();

        // Create character map for linking scenes to characters
        Map<String, FilmCharacters> characterMap = createCharacterMapFromProject(project);

        // Process each episode in the JSON
        for (JsonNode epNode : root.path("episodes")) {
            Episode episode = new Episode();

            // Set basic episode information
            episode.setProject(project);
            episode.setEpisodeNumber(epNode.path("episode").asInt());
            episode.setTitle(epNode.path("title").asText());

            // Build episode summary (combine summary and dramatic goal)
            String summary = epNode.path("summary").asText();
            if (epNode.has("dramatic_goal")) {
                summary += " | Goal: " + epNode.path("dramatic_goal").asText();
            }
            episode.setSummary(summary);

            // Only set duration if provided by AI
            if (epNode.has("duration_minutes")) {
                episode.setDurationMinutes(epNode.path("duration_minutes").asInt());
            }

            // Extract scenes for this episode with character associations
            Set<Scene> scenes = extractScenesFromNode(epNode, episode, characterMap);
            episode.setScenes(scenes);

            episodes.add(episode);
        }

        return episodes;
    }

    /**
     * ENHANCED: Extracts scenes from a JSON node with improved dialogue structure
     */
    private Set<Scene> extractScenesFromNode(JsonNode parentNode, Episode episode, Map<String, FilmCharacters> characterMap) {
        Set<Scene> scenes = new HashSet<>();
        int sceneCounter = 1;

        for (JsonNode sceneNode : parentNode.path("scenes")) {
            Scene scene = new Scene();

            // Set basic scene information
            scene.setSceneNumber(sceneCounter++);
            scene.setSetting(sceneNode.path("slug").asText());
            scene.setActions(sceneNode.path("action").asText());
            scene.setEpisode(episode);

            // ENHANCED: Extract dialogue with proper character separation
            Set<FilmCharacters> sceneCharacters = new HashSet<>();
            String formattedDialogue = extractAndFormatDialogue(sceneNode, characterMap, sceneCharacters);

            scene.setDialogue(formattedDialogue);
            scene.setCharacters(sceneCharacters);

            // Combine technical notes into department notes
            StringBuilder notes = new StringBuilder();
            if (sceneNode.has("sound")) {
                notes.append("Sound: ").append(sceneNode.path("sound").asText()).append(" | ");
            }
            if (sceneNode.has("mood_light")) {
                notes.append("Mood: ").append(sceneNode.path("mood_light").asText()).append(" | ");
            }
            if (sceneNode.has("purpose")) {
                notes.append("Purpose: ").append(sceneNode.path("purpose").asText()).append(" | ");
            }
            if (sceneNode.has("turning_point")) {
                notes.append("Turning: ").append(sceneNode.path("turning_point").asText());
            }
            scene.setDepartmentNotes(notes.toString().trim());

            // Set timestamps
            scene.setCreatedAt(LocalDateTime.now());
            scene.setUpdatedAt(LocalDateTime.now());

            scenes.add(scene);
        }

        return scenes;
    }

    /**
     * NEW METHOD: Extracts and formats dialogue with proper character separation
     * This method creates a clean, structured dialogue format
     */
    private String extractAndFormatDialogue(JsonNode sceneNode, Map<String, FilmCharacters> characterMap, Set<FilmCharacters> sceneCharacters) {
        StringBuilder dialogueBuilder = new StringBuilder();

        for (JsonNode dialogueNode : sceneNode.path("dialogue")) {
            String characterName = dialogueNode.path("character").asText();
            String line = dialogueNode.path("line").asText();

            // Handle stage directions/asides
            String aside = "";
            if (dialogueNode.has("aside") && !dialogueNode.path("aside").asText().trim().isEmpty()) {
                aside = " (" + dialogueNode.path("aside").asText() + ")";
            }

            // Format the dialogue line properly
            dialogueBuilder.append(characterName)
                    .append(": ")
                    .append(line)
                    .append(aside)
                    .append("\n");

            // Associate character with scene if character exists
            FilmCharacters character = findCharacterByName(characterName, characterMap);
            if (character != null) {
                sceneCharacters.add(character);
                // Maintain both sides of the many-to-many relationship
                if (character.getScenes() == null) {
                    character.setScenes(new HashSet<>());
                }
            }
        }
        System.out.println("Dialogue Builder"+dialogueBuilder.toString());

        return dialogueBuilder.toString().trim();
    }

    /**
     * Creates character map from project's already-saved characters
     */
    private Map<String, FilmCharacters> createCharacterMapFromProject(Project project) {
        Map<String, FilmCharacters> characterMap = new HashMap<>();
        if (project.getCharacters() != null) {
            for (FilmCharacters character : project.getCharacters()) {
                if (character.getName() != null) {
                    // Store with exact name and normalized name for flexible matching
                    characterMap.put(character.getName(), character);
                    characterMap.put(character.getName().toLowerCase().trim(), character);
                }
            }
        }
        return characterMap;
    }

    /**
     * Finds a character by name using flexible matching
     */
    private FilmCharacters findCharacterByName(String characterName, Map<String, FilmCharacters> characterMap) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return null;
        }

        // Try exact match first
        FilmCharacters character = characterMap.get(characterName);
        if (character != null) {
            return character;
        }

        // Try normalized match
        character = characterMap.get(characterName.toLowerCase().trim());
        if (character != null) {
            return character;
        }

        // Try partial matching for cases where dialogue uses shortened names
        String normalizedSearch = characterName.toLowerCase().trim();
        for (Map.Entry<String, FilmCharacters> entry : characterMap.entrySet()) {
            String mapKey = entry.getKey().toLowerCase().trim();
            if (mapKey.contains(normalizedSearch) || normalizedSearch.contains(mapKey)) {
                return entry.getValue();
            }
        }

        return null; // Character not found
    }

    /**
     * Fixes problematic assumptions formatting in JSON
     */
    private String fixAssumptionsFormat(String json) {
        // Simple regex replacement for assumptions object
        if (json.contains("\"assumptions\": {")) {
            // Replace the entire assumptions object with empty array
            json = json.replaceAll("\"assumptions\"\\s*:\\s*\\{[^}]*\\}", "\"assumptions\": []");
        }
        return json;
    }

    /**
     * Extracts assumptions from JsonNode (handles array, object, or string)
     */
    private List<String> extractAssumptions(JsonNode assumptionsNode) {
        List<String> assumptions = new ArrayList<>();

        if (assumptionsNode.isArray()) {
            // Normal case - assumptions is an array
            for (JsonNode assumption : assumptionsNode) {
                assumptions.add(assumption.asText());
            }
        } else if (assumptionsNode.isObject()) {
            // AI returned object instead of array - extract all values
            assumptionsNode.fields().forEachRemaining(entry -> {
                assumptions.add(entry.getValue().asText());
            });
        } else if (assumptionsNode.isTextual()) {
            // Single string assumption
            assumptions.add(assumptionsNode.asText());
        }

        return assumptions;
    }

    /**
     * Extracts character information from AI JSON response
     */
    public Set<FilmCharacters> extractCharacters(JsonNode root, Project project) {
        Set<FilmCharacters> characters = new HashSet<>();
        Set<String> seen = new HashSet<>(); // Prevent duplicate characters

        for (JsonNode charNode : root.path("characters")) {
            // Create unique key to avoid duplicates
            String uniqueKey = charNode.path("name").asText() + "-" + charNode.path("age").asInt();
            if (seen.contains(uniqueKey)) continue;
            seen.add(uniqueKey);

            FilmCharacters character = new FilmCharacters();

            // Set basic character information
            character.setProject(project);
            character.setName(charNode.path("name").asText());
            character.setAge(charNode.path("age").asInt());
            character.setRoleInStory(charNode.path("role").asText());

            // Extract and combine personality traits
            if (charNode.has("traits")) {
                List<String> traitsList = new ArrayList<>();
                for (JsonNode trait : charNode.path("traits")) {
                    traitsList.add(trait.asText());
                }
                character.setPersonalityTraits(String.join(" | ", traitsList));
            }

            // Build comprehensive background information
            List<String> backgroundParts = new ArrayList<>();
            if (charNode.has("backstory")) {
                backgroundParts.add(charNode.path("backstory").asText());
            }
            if (charNode.has("relationships")) {
                List<String> relationships = new ArrayList<>();
                for (JsonNode rel : charNode.path("relationships")) {
                    relationships.add(rel.asText());
                }
                backgroundParts.add("العلاقات: " + String.join(" | ", relationships));
            }
            if (charNode.has("goal")) {
                backgroundParts.add("الهدف: " + charNode.path("goal").asText());
            }
            if (charNode.has("obstacle")) {
                backgroundParts.add("العقبة: " + charNode.path("obstacle").asText());
            }
            character.setBackground(String.join(" | ", backgroundParts));

            // Build character arc information
            List<String> arcParts = new ArrayList<>();
            if (charNode.has("arc")) {
                arcParts.add(charNode.path("arc").asText());
            }
            if (charNode.has("voice_notes")) {
                arcParts.add("ملاحظات الصوت: " + charNode.path("voice_notes").asText());
            }
            character.setCharacterArc(String.join(" | ", arcParts));

            // Set timestamps
            character.setCreatedAt(LocalDateTime.now());
            character.setUpdatedAt(LocalDateTime.now());

            characters.add(character);
        }

        return characters;
    }

    /**
     * Extracts casting recommendations from AI JSON response
     */
    public Set<CastingRecommendation> extractCasting(String json, Project project) throws Exception {
        JsonNode root = mapper.readTree(json);
        Set<CastingRecommendation> castingRecommendations = new HashSet<>();

        // Process each character's casting suggestions
        for (JsonNode castNode : root.path("casting")) {
            String characterName = castNode.path("character").asText();

            // Process each actor suggestion for this character
            for (JsonNode suggestionNode : castNode.path("suggestions")) {
                CastingRecommendation recommendation = new CastingRecommendation();

                // Set casting recommendation details
                recommendation.setProject(project);
                recommendation.setCharacterName(characterName);
                recommendation.setRecommendedActorName(suggestionNode.path("actor").asText());
                recommendation.setReasoning(suggestionNode.path("why").asText());

                // Convert percentage to decimal (87% -> 0.87)
                recommendation.setMatchScore(suggestionNode.path("match_percent").asDouble() / 100.0);
                recommendation.setProfile(suggestionNode.path("profile").asText());
                recommendation.setAge(suggestionNode.path("age").asInt());

                // Set timestamps
                recommendation.setCreatedAt(LocalDateTime.now());
                recommendation.setUpdatedAt(LocalDateTime.now());

                castingRecommendations.add(recommendation);
            }
        }

        return castingRecommendations;
    }
}