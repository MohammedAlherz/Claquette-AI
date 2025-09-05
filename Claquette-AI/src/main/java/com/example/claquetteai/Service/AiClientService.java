//package com.example.claquetteai.Service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//
//@Service
//@RequiredArgsConstructor
//public class AiClientService {
//    @Value("${spring.ai.openai.api-key}")
//    private String API_KEY;
//    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
//
//    private final HttpClient client = HttpClient.newHttpClient();
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public String askModel(String prompt) throws Exception {
//        // Build the API request body
//        String requestBody = """
//        {
//          "model": "gpt-4.1-mini",
//          "messages": [
//            {"role": "system", "content": "You are a professional Saudi screenwriter. CRITICAL RULES: 1) Return ONLY valid JSON. 2) 'assumptions' must be an ARRAY of strings, not an object. 3) No explanations, no markdown, no code blocks. 4) Start with { and end with }. 5) No trailing commas."},
//            {"role": "user", "content": %s}
//          ],
//          "temperature": 0.7,
//          "max_tokens": 4000
//        }
//        """.formatted(mapper.writeValueAsString(prompt));
//
//        // Create and send HTTP request
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(ENDPOINT))
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer " + API_KEY)
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                .build();
//
//        // Get response from OpenAI
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        // Parse the response to extract the AI's content
//        JsonNode responseBody = mapper.readTree(response.body());
//
//        // Check for API errors
//        if (responseBody.has("error")) {
//            throw new RuntimeException("OpenAI API Error: " + responseBody.get("error").get("message").asText());
//        }
//
//        // Extract the AI's response content (not the entire API response)
//        JsonNode choices = responseBody.get("choices");
//        if (choices == null || choices.size() == 0) {
//            throw new RuntimeException("No choices in OpenAI response");
//        }
//
//        JsonNode message = choices.get(0).get("message");
//        if (message == null) {
//            throw new RuntimeException("No message in OpenAI choice");
//        }
//
//        String aiContent = message.get("content").asText();
//        if (aiContent == null || aiContent.trim().isEmpty()) {
//            throw new RuntimeException("Empty content in AI response");
//        }
//
//        // Extract and clean the AI response
//        return sanitizeJson(aiContent);
//    }
//
//    /**
//     * Cleans and validates JSON response from AI
//     * Removes markdown formatting and fixes common JSON issues
//     */
//    private String sanitizeJson(String raw) {
//        // Remove backticks and markdown code blocks
//        String cleaned = raw.trim();
//
//        if (cleaned.startsWith("```")) {
//            cleaned = cleaned.replaceAll("(?s)```(json)?", "").trim();
//        }
//        if (cleaned.endsWith("```")) {
//            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
//        }
//
//        // Extract only the JSON object part
//        int firstBrace = cleaned.indexOf('{');
//        int lastBrace = cleaned.lastIndexOf('}');
//
//        if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
//            throw new RuntimeException("No valid JSON object found in AI response");
//        }
//
//        cleaned = cleaned.substring(firstBrace, lastBrace + 1);
//
//        // Fix common JSON issues from AI
//        cleaned = fixCommonJsonIssues(cleaned);
//
//        // Final validation
//        if (!cleaned.startsWith("{") || !cleaned.endsWith("}")) {
//            throw new RuntimeException("Invalid JSON format from AI");
//        }
//
//        return cleaned;
//    }
//
//    /**
//     * Fixes common JSON formatting issues that AI might produce
//     * Specifically handles the assumptions object/array issue
//     */
//    private String fixCommonJsonIssues(String json) {
//        // Fix assumptions object to array - simple replacement
//        if (json.contains("\"assumptions\": {")) {
//            json = json.replaceAll("\"assumptions\"\\s*:\\s*\\{[^}]*\\}", "\"assumptions\": []");
//        }
//
//        // Remove trailing commas before closing braces/brackets
//        json = json.replaceAll(",\\s*([}\\]])", "$1");
//
//        return json;
//    }
//}
package com.example.claquetteai.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiClientService {

    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;

    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final ObjectMapper mapper = new ObjectMapper();

    // Inject WebClient.Builder via Spring Boot
    private final WebClient.Builder webClientBuilder;

    public String askModel(String prompt) throws Exception {
        // Build the request body
        String requestBody = """
        {
          "model": "gpt-4.1-mini",
          "messages": [
            {"role": "system", "content": "You are a professional Saudi screenwriter. CRITICAL RULES: 1) Return ONLY valid JSON. 2) 'assumptions' must be an ARRAY of strings, not an object. 3) No explanations, no markdown, no code blocks. 4) Start with { and end with }. 5) No trailing commas."},
            {"role": "user", "content": %s}
          ],
          "temperature": 0.7
        }
        """.formatted(mapper.writeValueAsString(prompt));
//        "max_tokens": 10000

        // Use WebClient to call OpenAI
        String responseBody = webClientBuilder
                .baseUrl(ENDPOINT)
                .build()
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + API_KEY)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)   // reactive -> returns Mono<String>
                .block();                  // block to make it sync

        // Parse the response JSON
        JsonNode responseJson = mapper.readTree(responseBody);

        if (responseJson.has("error")) {
            throw new RuntimeException("OpenAI API Error: " + responseJson.get("error").get("message").asText());
        }

        JsonNode choices = responseJson.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in OpenAI response");
        }

        JsonNode message = choices.get(0).get("message");
        if (message == null) {
            throw new RuntimeException("No message in OpenAI choice");
        }

        String aiContent = message.get("content").asText();
        if (aiContent == null || aiContent.trim().isEmpty()) {
            throw new RuntimeException("Empty content in AI response");
        }

        return sanitizeJson(aiContent);
    }

    private String sanitizeJson(String raw) {
        String cleaned = raw.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)```(json)?", "").trim();
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
            throw new RuntimeException("No valid JSON object found in AI response");
        }

        cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        cleaned = fixCommonJsonIssues(cleaned);

        if (!cleaned.startsWith("{") || !cleaned.endsWith("}")) {
            throw new RuntimeException("Invalid JSON format from AI");
        }

        return cleaned;
    }

    private String fixCommonJsonIssues(String json) {
        if (json.contains("\"assumptions\": {")) {
            json = json.replaceAll("\"assumptions\"\\s*:\\s*\\{[^}]*\\}", "\"assumptions\": []");
        }
        json = json.replaceAll(",\\s*([}\\]])", "$1");
        return json;
    }
}
