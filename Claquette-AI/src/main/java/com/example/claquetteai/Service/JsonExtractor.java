package com.example.claquetteai.Service;

import com.example.claquetteai.Model.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.io.JsonEOFException;
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
     * Extracts Film with scenes from AI JSON response
     * Creates Film entity with scenes and character associations
     */
    public Film extractFilmWithScenes(String json, Project project) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            throw new RuntimeException("Empty JSON response from AI service");
        }
        // Clean the JSON response first
        json = cleanJsonResponse(json);

        // Validate JSON structure before parsing
        if (!isValidJsonStructure(json)) {
            System.err.println("=== MALFORMED JSON DETECTED ===");
            System.err.println("Response length: " + json.length());
            System.err.println("Response ends with: " + json.substring(Math.max(0, json.length() - 100)));
            System.err.println("Expected to end with '}' or ']', but ends with: '" + json.charAt(json.length() - 1) + "'");
            throw new RuntimeException("AI service returned incomplete JSON. Response appears to be truncated.");
        }

        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (JsonEOFException e) {
            System.err.println("JSON EOF Exception - response was cut off");
            throw new RuntimeException("AI response was truncated - try reducing prompt size or increasing token limits", e);
        } catch (Exception e) {
            System.err.println("JSON parsing failed: " + e.getMessage());
            throw new RuntimeException("Could not parse AI response as valid JSON", e);
        }
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

    private boolean isValidJsonStructure(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        json = json.trim();

        // Basic structure validation
        if (json.startsWith("{") && !json.endsWith("}")) {
            System.err.println("JSON starts with '{' but doesn't end with '}'");
            return false;
        }

        if (json.startsWith("[") && !json.endsWith("]")) {
            System.err.println("JSON starts with '[' but doesn't end with ']'");
            return false;
        }

        // Try a quick parse test
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            System.err.println("JSON structure validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * COMPLETELY REWRITTEN: Extracts scenes for film with proper character-dialogue separation
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

            // COMPLETELY NEW APPROACH: Process dialogue and characters separately
            Set<FilmCharacters> sceneCharacters = new HashSet<>();
            String formattedDialogue = processSceneDialogueAndCharacters(sceneNode, characterMap, sceneCharacters);

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
        // ADD JSON VALIDATION BEFORE PARSING
        if (json == null || json.trim().isEmpty()) {
            throw new RuntimeException("Empty JSON response from AI service");
        }

        // Clean the JSON response first
        json = cleanJsonResponse(json);

        // Validate JSON structure before parsing
        if (!isValidJsonStructure(json)) {
            System.err.println("=== MALFORMED JSON DETECTED ===");
            System.err.println("Response length: " + json.length());
            System.err.println("Response ends with: " + json.substring(Math.max(0, json.length() - 100)));
            throw new RuntimeException("AI service returned incomplete JSON. Response appears to be truncated.");
        }

        // WRAP THE EXISTING LINE WITH TRY-CATCH
        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (JsonEOFException e) {
            System.err.println("JSON EOF Exception - response was cut off");
            throw new RuntimeException("AI response was truncated - try reducing prompt size or increasing token limits", e);
        } catch (Exception e) {
            System.err.println("JSON parsing failed: " + e.getMessage());
            throw new RuntimeException("Could not parse AI response as valid JSON", e);
        }
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
        Set<Scene> scenes = extractScenesFromEpisodeNode(episodeNode, episode, characterMap);
        episode.setScenes(scenes);

        return episode;
    }


    /**
     * COMPLETELY REWRITTEN: Extracts scenes from episode node with proper character-dialogue separation
     */
    private Set<Scene> extractScenesFromEpisodeNode(JsonNode parentNode, Episode episode, Map<String, FilmCharacters> characterMap) {
        Set<Scene> scenes = new HashSet<>();
        int sceneCounter = 1;

        for (JsonNode sceneNode : parentNode.path("scenes")) {
            Scene scene = new Scene();

            // Set basic scene information
            scene.setSceneNumber(sceneCounter++);
            scene.setSetting(sceneNode.path("slug").asText());
            scene.setActions(sceneNode.path("action").asText());
            scene.setEpisode(episode);

            // COMPLETELY NEW APPROACH: Process dialogue and characters separately
            Set<FilmCharacters> sceneCharacters = new HashSet<>();
            String formattedDialogue = processSceneDialogueAndCharacters(sceneNode, characterMap, sceneCharacters);

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
     * COMPLETELY NEW METHOD: Processes dialogue and characters with proper validation and cleanup
     */
    private String processSceneDialogueAndCharacters(JsonNode sceneNode, Map<String, FilmCharacters> characterMap, Set<FilmCharacters> sceneCharacters) {
        StringBuilder dialogueBuilder = new StringBuilder();
        List<String> dialogueLines = new ArrayList<>();

        System.out.println("=== PROCESSING SCENE DIALOGUE ===");

        // Check if dialogue node exists and is not empty
        JsonNode dialogueNode = sceneNode.path("dialogue");
        if (dialogueNode.isMissingNode() || dialogueNode.isEmpty()) {
            System.out.println("WARNING: No dialogue found in scene");
            return "";
        }

        // Process each dialogue entry
        for (JsonNode dialogueEntry : dialogueNode) {
            String characterName = dialogueEntry.path("character").asText();
            String line = dialogueEntry.path("line").asText();

            // Skip empty dialogue entries
            if (characterName.trim().isEmpty() || line.trim().isEmpty()) {
                System.out.println("WARNING: Skipping empty dialogue entry");
                continue;
            }

            // Handle stage directions/asides
            String aside = "";
            if (dialogueEntry.has("aside") && !dialogueEntry.path("aside").asText().trim().isEmpty()) {
                aside = " (" + dialogueEntry.path("aside").asText() + ")";
            }

            // Format the dialogue line
            String formattedLine = characterName + ": " + line + aside;
            dialogueLines.add(formattedLine);

            System.out.println("Processing dialogue: " + characterName + " -> " + line.substring(0, Math.min(50, line.length())) + "...");

            // Find and associate character with scene
            FilmCharacters character = findCharacterByNameImproved(characterName, characterMap);
            if (character != null) {
                sceneCharacters.add(character);
                System.out.println("✓ Associated character " + character.getName() + " (ID: " + character.getId() + ") with scene");
            } else {
                System.out.println("✗ Character '" + characterName + "' not found in project characters");

                // Try to find closest match
                String closestMatch = findClosestCharacterName(characterName, characterMap);
                if (closestMatch != null) {
                    System.out.println("  Suggestion: Did you mean '" + closestMatch + "'?");
                    FilmCharacters suggestedChar = characterMap.get(closestMatch);
                    if (suggestedChar != null) {
                        sceneCharacters.add(suggestedChar);
                        System.out.println("  ✓ Using suggested character " + suggestedChar.getName() + " (ID: " + suggestedChar.getId() + ")");
                    }
                }
            }
        }

        // Join all dialogue lines
        String finalDialogue = String.join("\n", dialogueLines);

        System.out.println("=== SCENE SUMMARY ===");
        System.out.println("Total dialogue lines: " + dialogueLines.size());
        System.out.println("Total characters in scene: " + sceneCharacters.size());
        System.out.println("Character IDs: " + sceneCharacters.stream().map(c -> c.getId()).toList());
        System.out.println("Final dialogue length: " + finalDialogue.length());
        System.out.println("======================");

        return finalDialogue;
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
                    System.out.println("Added character to map: " + character.getName() + " (ID: " + character.getId() + ")");
                }
            }
        }
        System.out.println("Character map created with " + (characterMap.size()/2) + " unique characters");
        return characterMap;
    }

    /**
     * IMPROVED: Finds a character by name using flexible matching with better debugging
     */
    private FilmCharacters findCharacterByNameImproved(String characterName, Map<String, FilmCharacters> characterMap) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return null;
        }

        String cleanName = characterName.trim();

        // Try exact match first
        FilmCharacters character = characterMap.get(cleanName);
        if (character != null) {
            return character;
        }

        // Try normalized match
        character = characterMap.get(cleanName.toLowerCase().trim());
        if (character != null) {
            return character;
        }

        // Try partial matching
        String normalizedSearch = cleanName.toLowerCase().trim();
        for (Map.Entry<String, FilmCharacters> entry : characterMap.entrySet()) {
            String mapKey = entry.getKey().toLowerCase().trim();
            if (mapKey.contains(normalizedSearch) || normalizedSearch.contains(mapKey)) {
                return entry.getValue();
            }
        }

        return null; // Character not found
    }

    /**
     * NEW METHOD: Finds the closest character name for suggestions
     */
    private String findClosestCharacterName(String targetName, Map<String, FilmCharacters> characterMap) {
        String normalizedTarget = targetName.toLowerCase().trim();
        String closestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (String characterName : characterMap.keySet()) {
            String normalizedChar = characterName.toLowerCase().trim();

            // Skip if this is just a normalized version of another entry
            if (characterMap.get(characterName) == null) continue;

            // Simple distance calculation
            int distance = Math.abs(normalizedTarget.length() - normalizedChar.length());

            // Check for partial matches
            if (normalizedChar.contains(normalizedTarget) || normalizedTarget.contains(normalizedChar)) {
                distance = 0;
            }

            if (distance < minDistance) {
                minDistance = distance;
                closestMatch = characterName;
            }
        }

        return closestMatch;
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
     * FIXED: Extracts casting recommendations from AI JSON response with robust error handling
     */
    public Set<CastingRecommendation> extractCasting(String json, Project project) throws Exception {
        try {
            // Clean and validate JSON before parsing
            json = cleanJsonResponse(json);

            if (json == null || json.trim().isEmpty()) {
                System.out.println("ERROR: Empty or null JSON response for casting");
                return new HashSet<>();
            }

            System.out.println("=== PARSING CASTING JSON ===");
            System.out.println("JSON length: " + json.length());
            System.out.println("First 200 characters: " + json.substring(0, Math.min(200, json.length())));

            JsonNode root = mapper.readTree(json);
            Set<CastingRecommendation> castingRecommendations = new HashSet<>();

            // Create a map of character names to FilmCharacters for easy lookup
            Map<String, FilmCharacters> characterMap = new HashMap<>();

            if (project.getCharacters() != null) {
                for (FilmCharacters character : project.getCharacters()) {
                    // Store with exact name and normalized name for flexible matching
                    characterMap.put(character.getName(), character);
                    characterMap.put(character.getName().toLowerCase().trim(), character);
                    System.out.println("Added character to casting map: " + character.getName() + " (ID: " + character.getId() + ")");
                }
            }

            System.out.println("=== PROCESSING CASTING RECOMMENDATIONS ===");
            System.out.println("Available characters: " + characterMap.size()/2);

            // Check if casting node exists
            JsonNode castingNode = root.path("casting");
            if (castingNode.isMissingNode() || !castingNode.isArray()) {
                System.out.println("WARNING: No 'casting' array found in JSON response");
                return castingRecommendations;
            }

            // Process each character's casting suggestions
            for (JsonNode castNode : castingNode) {
                String characterName = castNode.path("character").asText();
                System.out.println("Processing casting for character: " + characterName);

                // Find the corresponding FilmCharacters entity using flexible matching
                FilmCharacters filmCharacter = findCharacterForCasting(characterName, characterMap);
                if (filmCharacter == null) {
                    System.out.println("Warning: Character '" + characterName + "' not found in project characters");
                    // Try to find closest match
                    String closestMatch = findClosestCharacterName(characterName, characterMap);
                    if (closestMatch != null) {
                        System.out.println("Suggestion: Did you mean '" + closestMatch + "'?");
                        filmCharacter = characterMap.get(closestMatch);
                    }

                    if (filmCharacter == null) {
                        System.out.println("Skipping casting for unknown character: " + characterName);
                        continue; // Skip if character not found
                    }
                }

                // Process ALL suggestions for this character (One-to-Many)
                JsonNode suggestions = castNode.path("suggestions");
                if (suggestions.isArray() && suggestions.size() > 0) {
                    int priority = 1; // Start with highest priority

                    for (JsonNode suggestion : suggestions) {
                        // Take up to 3 suggestions per character to avoid too many recommendations
                        if (priority > 3) break;

                        try {
                            CastingRecommendation recommendation = new CastingRecommendation();
                            recommendation.setProject(project);
                            recommendation.setCharacter(filmCharacter);

                            // Safely extract actor name with validation
                            String actorName = safeGetTextValue(suggestion, "actor", "Unknown Actor");
                            recommendation.setRecommendedActorName(actorName);

                            // Safely extract reasoning
                            String reasoning = safeGetTextValue(suggestion, "why", "No reasoning provided");
                            recommendation.setReasoning(reasoning);

                            // Safely extract and convert match percentage
                            double matchScore = safeGetDoubleValue(suggestion, "match_percent", 50.0) / 100.0;
                            recommendation.setMatchScore(Math.max(0.0, Math.min(1.0, matchScore))); // Clamp between 0-1

                            // Safely extract profile
                            String profile = safeGetTextValue(suggestion, "profile", "No profile available");
                            recommendation.setProfile(profile);

                            // Safely extract age
                            int age = safeGetIntValue(suggestion, "age", 30);
                            recommendation.setAge(age);

                            recommendation.setPriority(priority); // Set priority (1 = best match)
                            recommendation.setCreatedAt(LocalDateTime.now());
                            recommendation.setUpdatedAt(LocalDateTime.now());

                            castingRecommendations.add(recommendation);

                            System.out.println("✓ Created casting recommendation #" + priority + " for " + filmCharacter.getName() +
                                    " -> " + recommendation.getRecommendedActorName() + " (Character ID: " + filmCharacter.getId() + ")");

                            priority++;

                        } catch (Exception e) {
                            System.out.println("ERROR: Failed to process suggestion #" + priority + " for character " + characterName + ": " + e.getMessage());
                            // Continue with next suggestion instead of failing completely
                        }
                    }
                } else {
                    System.out.println("Warning: No suggestions found for character: " + characterName);
                }
            }

            System.out.println("=== CASTING SUMMARY ===");
            System.out.println("Total casting recommendations created: " + castingRecommendations.size());
            System.out.println("========================");

            return castingRecommendations;

        } catch (JsonParseException e) {
            System.out.println("ERROR: JSON parsing failed at line " + e.getLocation().getLineNr() +
                    ", column " + e.getLocation().getColumnNr());
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Problematic JSON snippet around error:");

            // Try to show the problematic area
            String[] lines = json.split("\n");
            int errorLine = (int) e.getLocation().getLineNr() - 1; // Convert to 0-based
            int startLine = Math.max(0, errorLine - 2);
            int endLine = Math.min(lines.length - 1, errorLine + 2);

            for (int i = startLine; i <= endLine; i++) {
                String prefix = (i == errorLine) ? ">>> " : "    ";
                System.out.println(prefix + "Line " + (i + 1) + ": " + lines[i]);
            }

            throw new Exception("Failed to parse casting JSON: " + e.getMessage(), e);

        } catch (Exception e) {
            System.out.println("ERROR: Unexpected error during casting extraction: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * NEW: Cleans and validates JSON response to handle common formatting issues
     */
    private String cleanJsonResponse(String json) {
        if (json == null) return null;

        // Remove any leading/trailing whitespace
        json = json.trim();

        // Remove any potential markdown code block markers
        if (json.startsWith("```json")) {
            json = json.substring(7);
        }
        if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }

        json = json.trim();

        // Basic validation - must start with { or [
        if (!json.startsWith("{") && !json.startsWith("[")) {
            System.out.println("WARNING: JSON doesn't start with { or [, attempting to find JSON start");
            int jsonStart = json.indexOf("{");
            if (jsonStart == -1) {
                jsonStart = json.indexOf("[");
            }
            if (jsonStart > 0) {
                json = json.substring(jsonStart);
            }
        }

        return json;
    }

    /**
     * NEW: Safely extracts text value from JsonNode with fallback
     */
    private String safeGetTextValue(JsonNode node, String fieldName, String defaultValue) {
        try {
            JsonNode fieldNode = node.path(fieldName);
            if (fieldNode.isMissingNode() || fieldNode.isNull()) {
                return defaultValue;
            }

            String value = fieldNode.asText();
            return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();

        } catch (Exception e) {
            System.out.println("WARNING: Failed to extract text field '" + fieldName + "', using default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * NEW: Safely extracts double value from JsonNode with fallback
     */
    private double safeGetDoubleValue(JsonNode node, String fieldName, double defaultValue) {
        try {
            JsonNode fieldNode = node.path(fieldName);
            if (fieldNode.isMissingNode() || fieldNode.isNull()) {
                return defaultValue;
            }

            if (fieldNode.isNumber()) {
                return fieldNode.asDouble();
            }

            // Try to parse as string if it's not a number
            String textValue = fieldNode.asText();
            if (textValue != null && !textValue.trim().isEmpty()) {
                // Remove any non-numeric characters except decimal point
                textValue = textValue.replaceAll("[^0-9.]", "");
                if (!textValue.isEmpty()) {
                    return Double.parseDouble(textValue);
                }
            }

            return defaultValue;

        } catch (Exception e) {
            System.out.println("WARNING: Failed to extract double field '" + fieldName + "', using default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * NEW: Safely extracts integer value from JsonNode with fallback
     */
    private int safeGetIntValue(JsonNode node, String fieldName, int defaultValue) {
        try {
            JsonNode fieldNode = node.path(fieldName);
            if (fieldNode.isMissingNode() || fieldNode.isNull()) {
                return defaultValue;
            }

            if (fieldNode.isNumber()) {
                return fieldNode.asInt();
            }

            // Try to parse as string if it's not a number
            String textValue = fieldNode.asText();
            if (textValue != null && !textValue.trim().isEmpty()) {
                // Remove any non-numeric characters
                textValue = textValue.replaceAll("[^0-9]", "");
                if (!textValue.isEmpty()) {
                    return Integer.parseInt(textValue);
                }
            }

            return defaultValue;

        } catch (Exception e) {
            System.out.println("WARNING: Failed to extract int field '" + fieldName + "', using default: " + defaultValue);
            return defaultValue;
        }
    }

    /**
     * Helper method to find character with flexible matching
     */
    private FilmCharacters findCharacterForCasting(String characterName, Map<String, FilmCharacters> characterMap) {
        if (characterName == null || characterName.trim().isEmpty()) {
            return null;
        }

        String cleanName = characterName.trim();

        // Try exact match first
        FilmCharacters character = characterMap.get(cleanName);
        if (character != null) {
            return character;
        }

        // Try normalized match
        character = characterMap.get(cleanName.toLowerCase().trim());
        if (character != null) {
            return character;
        }

        // Try partial matching
        String normalizedSearch = cleanName.toLowerCase().trim();
        for (Map.Entry<String, FilmCharacters> entry : characterMap.entrySet()) {
            String mapKey = entry.getKey().toLowerCase().trim();
            if (mapKey.contains(normalizedSearch) || normalizedSearch.contains(mapKey)) {
                return entry.getValue();
            }
        }

        return null; // Character not found
    }
}