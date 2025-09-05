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

        // Don't set defaults - use only what user provides
        // Status is set in ProjectService.addProject()
        // Budget, dates come from user input

        // Extract optional fields from AI response
        if (projectNode.has("genre")) {
            project.setGenre(projectNode.path("genre").asText());
        }

        if (projectNode.has("target_audience")) {
            project.setTargetAudience(projectNode.path("target_audience").asText());
        }

        // Handle location - use mutable ArrayList
        if (projectNode.has("city")) {
            List<String> locations = new ArrayList<>();
            locations.add(projectNode.path("city").asText());
            project.setLocation(locations);
        } else if (projectNode.has("country")) {
            List<String> locations = new ArrayList<>();
            locations.add(projectNode.path("country").asText());
            project.setLocation(locations);
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
     * Creates Film entity with scenes directly attached
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

        // Extract scenes for this film
        Set<Scene> scenes = extractScenesForFilm(filmNode, film);
        film.setScenes(scenes);

        return film;
    }

    /**
     * Extracts scenes for film (similar to episodes but links to film)
     */
    private Set<Scene> extractScenesForFilm(JsonNode filmNode, Film film) {
        Set<Scene> scenes = new HashSet<>();
        int sceneCounter = 1;

        for (JsonNode sceneNode : filmNode.path("scenes")) {
            Scene scene = new Scene();

            scene.setSceneNumber(sceneCounter++);
            scene.setSetting(sceneNode.path("slug").asText());
            scene.setActions(sceneNode.path("action").asText());
            scene.setFilm(film); // Link to film instead of episode

            // Same dialogue and notes extraction as before...
            StringBuilder dialogueBuilder = new StringBuilder();
            for (JsonNode dialogueNode : sceneNode.path("dialogue")) {
                String character = dialogueNode.path("character").asText();
                String line = dialogueNode.path("line").asText();
                String aside = dialogueNode.has("aside") ?
                        " (" + dialogueNode.path("aside").asText() + ")" : "";

                dialogueBuilder.append(character)
                        .append(": ")
                        .append(line)
                        .append(aside)
                        .append("\n");
            }
            scene.setDialogue(dialogueBuilder.toString().trim());

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
     * Creates Episode entity with associated Scene entities
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

        // Extract scenes for this episode
        Set<Scene> scenes = extractScenesFromNode(episodeNode, episode);
        episode.setScenes(scenes);

        return episode;
    }

    /**
     * Extracts episodes and their scenes from AI JSON response (legacy method)
     * Creates Episode entities with associated Scene entities
     */
    public Set<Episode> extractEpisodes(String json, Project project) throws Exception {
        JsonNode root = mapper.readTree(json);
        Set<Episode> episodes = new HashSet<>();

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

            // Extract scenes for this episode
            Set<Scene> scenes = extractScenesFromNode(epNode, episode);
            episode.setScenes(scenes);

            episodes.add(episode);
        }

        return episodes;
    }

    /**
     * Extracts scenes from a JSON node (episode or film)
     * Creates Scene entities with dialogue and technical notes
     */
    private Set<Scene> extractScenesFromNode(JsonNode parentNode, Episode episode) {
        Set<Scene> scenes = new HashSet<>();
        int sceneCounter = 1;

        for (JsonNode sceneNode : parentNode.path("scenes")) {
            Scene scene = new Scene();

            // Set basic scene information
            scene.setSceneNumber(sceneCounter++);
            scene.setSetting(sceneNode.path("slug").asText());
            scene.setActions(sceneNode.path("action").asText());
            scene.setEpisode(episode); // Link to episode

            // Convert dialogue array to formatted string
            StringBuilder dialogueBuilder = new StringBuilder();
            for (JsonNode dialogueNode : sceneNode.path("dialogue")) {
                String character = dialogueNode.path("character").asText();
                String line = dialogueNode.path("line").asText();
                String aside = dialogueNode.has("aside") ?
                        " (" + dialogueNode.path("aside").asText() + ")" : "";

                dialogueBuilder.append(character)
                        .append(": ")
                        .append(line)
                        .append(aside)
                        .append("\n");
            }
            scene.setDialogue(dialogueBuilder.toString().trim());

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
     * Fixes problematic assumptions formatting in JSON
     * Converts invalid object format to valid array format
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
     * Returns list of assumption strings
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
     * Creates FilmCharacters entities with detailed backgrounds
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
     * Creates CastingRecommendation entities for each character-actor pairing
     */
    public Set<CastingRecommendation> extractCasting(String json, Project project) throws Exception {
        JsonNode root = mapper.readTree(json);
        Set<CastingRecommendation> castingRecommendations = new HashSet<>();

        // Process each character's casting suggestions
        for (JsonNode castNode : root.path("casting")) {
            String characterName = castNode.path("character").asText();

            // Process each actor suggestion for this character
            for (JsonNode suggestionNode : castNode.path("suggestions")) {
                // Debug output to track processing
                System.out.println("Processing casting for character: " + characterName);

                CastingRecommendation recommendation = new CastingRecommendation();

                // Set casting recommendation details
                recommendation.setProject(project);
                recommendation.setCharacterName(characterName);
                recommendation.setRecommendedActorName(suggestionNode.path("actor").asText());
                recommendation.setReasoning(suggestionNode.path("why").asText());

                // Convert percentage to decimal (87% -> 0.87)
                recommendation.setMatchScore(suggestionNode.path("match_percent").asDouble() / 100.0);
                recommendation.setProfile(suggestionNode.path("profile").asText());

                // Set timestamps
                recommendation.setCreatedAt(LocalDateTime.now());
                recommendation.setUpdatedAt(LocalDateTime.now());

                castingRecommendations.add(recommendation);
            }
        }

        return castingRecommendations;
    }
}