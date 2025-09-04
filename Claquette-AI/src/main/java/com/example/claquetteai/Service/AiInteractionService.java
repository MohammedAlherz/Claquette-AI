package com.example.claquetteai.Service;

import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiInteractionService {

    // OpenAI API configuration
    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    // Injected dependencies
    private final JsonExtractor jsonExtractor;
    private final ProjectRepository projectRepository;
    private final PromptBuilderService promptBuilderService;
    private final CompanyRepository companyRepository;

    // HTTP client and JSON mapper
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Cleans and validates JSON response from AI
     * Removes markdown formatting and fixes common JSON issues
     */
    private String sanitizeJson(String raw) {
        // Remove backticks and markdown code blocks
        String cleaned = raw.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)```(json)?", "").trim();
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        // Extract only the JSON object part
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
            throw new RuntimeException("No valid JSON object found in AI response");
        }

        cleaned = cleaned.substring(firstBrace, lastBrace + 1);

        // Fix common JSON issues from AI
        cleaned = fixCommonJsonIssues(cleaned);

        // Final validation
        if (!cleaned.startsWith("{") || !cleaned.endsWith("}")) {
            throw new RuntimeException("Invalid JSON format from AI");
        }

        return cleaned;
    }

    /**
     * Fixes common JSON formatting issues that AI might produce
     * Specifically handles the assumptions object/array issue
     */
    private String fixCommonJsonIssues(String json) {
        // Fix assumptions object to array - simple replacement
        if (json.contains("\"assumptions\": {")) {
            json = json.replaceAll("\"assumptions\"\\s*:\\s*\\{[^}]*\\}", "\"assumptions\": []");
        }

        // Remove trailing commas before closing braces/brackets
        json = json.replaceAll(",\\s*([}\\]])", "$1");

        return json;
    }

    /**
     * Main method to generate complete screenplay
     * Uses existing project data to determine type and episode count
     */
    public Project generateFullScreenplay(Integer projectId) throws Exception {
        // Step 1: Get existing project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        // Step 2: Generate characters for the project
        generateCharacters(project, project.getDescription());

        // Step 3: Generate Film OR Episodes based on project type
        if ("FILM".equals(project.getProjectType())) {
            // Generate film with scenes directly (NOT as episode)
            generateFilmWithScenes(project);
        } else {
            // For series, determine episode count and generate episodes
            int episodeCount = determineEpisodeCount(project);
            for (int i = 1; i <= episodeCount; i++) {
                generateEpisodeWithScenes(project, i);
            }
        }

        // Step 4: Generate casting recommendations
        generateCasting(project);

        // Save final project with all relationships
        return projectRepository.save(project);
    }

    /**
     * Generates a film with scenes (NOT as episode)
     * Creates Film entity with scenes directly attached
     */
    public void generateFilmWithScenes(Project project) throws Exception {
        // Build prompt for film generation
        String prompt = promptBuilderService.filmPrompt(project.getDescription());

        // Get AI response and extract film with scenes
        String json = askModel(prompt);
        Film film = jsonExtractor.extractFilmWithScenes(json, project);

        // Set film on project
        project.setFilms(film);

        projectRepository.save(project);
    }

    /**
     * Generates a specific episode with its scenes
     * Creates detailed scenes and dialogue for one episode
     */
    public void generateEpisodeWithScenes(Project project, int episodeNumber) throws Exception {
        // Build prompt for specific episode with scenes
        String prompt = promptBuilderService.episodePrompt(project.getDescription(), episodeNumber);

        // Get AI response and extract episode with scenes
        String json = askModel(prompt);
        Episode episode = jsonExtractor.extractEpisodeWithScenes(json, project, episodeNumber);

        // Add episode to project
        if (project.getEpisodes() == null) {
            project.setEpisodes(new java.util.HashSet<>());
        }
        project.getEpisodes().add(episode);

        projectRepository.save(project);
    }

    /**
     * Determines episode count for series projects
     * You can extend this logic based on your business rules
     */
    private int determineEpisodeCount(Project project) {
        // Default based on genre or other criteria
        if (project.getGenre() != null && project.getGenre().toLowerCase().contains("mini")) {
            return 3; // Mini series
        } else if ("Comedy".equalsIgnoreCase(project.getGenre())) {
            return 8; // Comedy series
        } else {
            return 6; // Default series length
        }
    }

    /**
     * Generates casting recommendations for all characters
     * Suggests Saudi actors for each character role
     */
    public void generateCasting(Project project) throws Exception {
        // Build simple project context string instead of JSON conversion
        String projectInfo = String.format(
                "Project: %s, Type: %s, Description: %s",
                project.getTitle(),
                project.getProjectType(),
                project.getDescription()
        );

        // Build casting prompt with project context
        String prompt = promptBuilderService.castingPrompt(projectInfo);

        // Get AI response and extract casting recommendations
        String json = askModel(prompt);
        Set<CastingRecommendation> casting = jsonExtractor.extractCasting(json, project);

        // Add casting to project
        if (project.getCastingRecommendations() == null) {
            project.setCastingRecommendations(new java.util.HashSet<>());
        }
        project.getCastingRecommendations().addAll(casting);

        projectRepository.save(project);
    }

    /**
     * Generates characters for the project
     * Creates detailed character profiles with backgrounds and arcs
     */
    public void generateCharacters(Project project, String storyDescription) throws Exception {
        // Build character generation prompt
        String prompt = promptBuilderService.charactersPrompt(storyDescription);

        // Get AI response and extract characters
        String json = askModel(prompt);
        JsonNode root = new ObjectMapper().readTree(json);
        Set<FilmCharacters> characters = jsonExtractor.extractCharacters(root, project);

        // Add characters to project
        if (project.getCharacters() == null) {
            project.setCharacters(new java.util.HashSet<>());
        }
        project.getCharacters().addAll(characters);

        projectRepository.save(project);
    }

    /**
     * Core method that communicates with OpenAI API
     * Sends prompt and returns cleaned JSON response
     */
    private String askModel(String prompt) throws Exception {
        // Build the API request body
        String requestBody = """
        {
          "model": "gpt-4",
          "messages": [
            {"role": "system", "content": "You are a professional Saudi screenwriter. CRITICAL RULES: 1) Return ONLY valid JSON. 2) 'assumptions' must be an ARRAY of strings, not an object. 3) No explanations, no markdown, no code blocks. 4) Start with { and end with }. 5) No trailing commas."},
            {"role": "user", "content": %s}
          ],
          "temperature": 0.7,
          "max_tokens": 4000
        }
        """.formatted(mapper.writeValueAsString(prompt));

        // Create and send HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Get response from OpenAI
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = mapper.readTree(response.body());

        // Check for API errors
        if (body.has("error")) {
            throw new RuntimeException("OpenAI API Error: " + body.get("error").get("message").asText());
        }

        // Extract and clean the AI response
        String jsonResponse = sanitizeJson(body.get("choices").get(0).get("message").get("content").asText());

        // Validate JSON before returning
        try {
            mapper.readTree(jsonResponse);
            return jsonResponse;
        } catch (Exception e) {
            System.err.println("Invalid JSON from AI: " + jsonResponse);
            throw new RuntimeException("AI returned invalid JSON: " + e.getMessage());
        }
    }
}