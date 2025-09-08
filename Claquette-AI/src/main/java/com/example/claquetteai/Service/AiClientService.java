package com.example.claquetteai.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

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


    public String generatePhoto(String prompt) throws Exception {
        // Build request body for Images API
        // Build payload as a Map -> Jackson serializes reliably
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "dall-e-3");
        payload.put("prompt", "you are saudi designer in saudi arabia you will generate photo using your mind as Saudi with all roles of Saudi to generate a photo using this description make it with saudi inspiration: "+prompt);
        payload.put("size", "1024x1024");
        payload.put("n", 1);
        payload.put("response_format", "b64_json");

        // WebClient with baseUrl = https://api.openai.com
        WebClient client = webClientBuilder
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // 50MB
                .build();


        // Try /v1/images first
        try {
            String resp = client.post()
                    .uri("/v1/images")
                    .bodyValue(payload) // ✅ no manual JSON strings
                    .retrieve()
                    .onStatus(
                            s -> s.is4xxClientError() || s.is5xxServerError(),
                            r -> r.bodyToMono(String.class).map(msg ->
                                    new RuntimeException("OpenAI error (" + r.statusCode() + "): " + msg))
                    )
                    .bodyToMono(String.class)
                    .block();
            return extractB64OrUrl(resp);
        } catch (RuntimeException ex) {
            // If it's not a 404, bubble up
            if (!String.valueOf(ex.getMessage()).contains("404")) throw ex;
        }

        // Fallback: /v1/images/generations
        String resp = client.post()
                .uri("/v1/images/generations")
                .bodyValue(payload) // ✅ same payload
                .retrieve()
                .onStatus(
                        s -> s.is4xxClientError() || s.is5xxServerError(),
                        r -> r.bodyToMono(String.class).map(msg ->
                                new RuntimeException("OpenAI error (" + r.statusCode() + "): " + msg))
                )
                .bodyToMono(String.class)
                .block();
        return extractB64OrUrl(resp);
    }

    private String extractB64OrUrl(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            if (root.has("error")) {
                throw new RuntimeException(root.path("error").path("message").asText());
            }
            JsonNode data = root.path("data");
            if (!data.isArray() || data.isEmpty()) {
                throw new RuntimeException("No image data returned");
            }

            // Try Base64 first (works when response_format = b64_json)
            String b64 = data.get(0).path("b64_json").asText(null);
            if (b64 != null && !b64.isBlank()) {
                return b64;
            }

            // Fallback: URL (default for dall-e-3)
            String url = data.get(0).path("url").asText(null);
            if (url == null || url.isBlank()) {
                throw new RuntimeException("Image response has neither b64_json nor url");
            }

            // Download the bytes and return Base64 (so you can still store it on Project)
            byte[] bytes = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (bytes == null || bytes.length == 0) {
                throw new RuntimeException("Failed to download image from returned URL");
            }
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse image response", e);
        }
    }
}
