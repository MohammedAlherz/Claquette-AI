package com.example.claquetteai.Service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AiJson {

    private static final ObjectMapper LENIENT = new ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonParser.Feature.ALLOW_TRAILING_COMMA)
            .enable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);

    private static final ObjectMapper STRICT = new ObjectMapper();

    private AiJson() {}

    /** Public entrypoint: returns a strict, reparsed JsonNode (object or array). */
    public static JsonNode extractStrictJsonOrThrow(String aiText, Set<String> requiredTopLevelKeys) {
        if (aiText == null || aiText.isBlank()) {
            throw new IllegalArgumentException("AI response was empty");
        }
        String stripped = stripMarkdownFences(aiText.trim());

        // Try: direct parse of the biggest balanced JSON object/array
        String candidate = findLargestBalancedJson(stripped);
        if (candidate == null) {
            // fallback: greedy between first '{' and last '}' (still common)
            candidate = greedyCurlyRange(stripped);
        }
        if (candidate == null) {
            throw new IllegalArgumentException("No valid JSON object found in AI response");
        }

        // Lenient parse, then re-serialize strictly to eliminate quirks
        JsonNode lenientNode = parseLenient(candidate);
        if (requiredTopLevelKeys != null && !requiredTopLevelKeys.isEmpty() && lenientNode.isObject()) {
            requireKeys((ObjectNode) lenientNode, requiredTopLevelKeys);
        }
        return reparseStrict(lenientNode);
    }

    /** Remove ```json fences and leading “Here is…” chatter */
    private static String stripMarkdownFences(String s) {
        // remove triple backtick blocks
        Pattern fenced = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```", Pattern.CASE_INSENSITIVE);
        Matcher m = fenced.matcher(s);
        if (m.find()) {
            return m.group(1).trim();
        }
        // also remove leading code-fence markers if partially present
        return s.replaceAll("^```\\w*\\s*", "").replaceAll("\\s*```\\s*$", "").trim();
    }

    /** Finds the largest balanced {...} or [...] block via stack scanning. */
    private static String findLargestBalancedJson(String s) {
        int bestStart = -1, bestEnd = -1, depth = 0;
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{' || c == '[') {
                stack.push(i);
                depth++;
            } else if (c == '}' || c == ']') {
                if (!stack.isEmpty()) {
                    int start = stack.pop();
                    depth--;
                    // consider this balanced block
                    if (stack.isEmpty()) {
                        if (start <= i && (i - start) > (bestEnd - bestStart)) {
                            bestStart = start;
                            bestEnd = i;
                        }
                    }
                }
            }
        }
        return (bestStart >= 0 && bestEnd > bestStart) ? s.substring(bestStart, bestEnd + 1) : null;
        // returns null if nothing balanced was found
    }

    /** Fallback: greedily take first '{' to last '}' */
    private static String greedyCurlyRange(String s) {
        int a = s.indexOf('{');
        int b = s.lastIndexOf('}');
        if (a >= 0 && b > a) return s.substring(a, b + 1);
        // arrays?
        a = s.indexOf('[');
        b = s.lastIndexOf(']');
        if (a >= 0 && b > a) return s.substring(a, b + 1);
        return null;
    }

    private static JsonNode parseLenient(String candidate) {
        try {
            return LENIENT.readTree(candidate);
        } catch (JsonProcessingException e) {
            // try a simple cleanup (common: trailing commas before } or ])
            String cleaned = candidate.replaceAll(",\\s*([}\\]])", "$1");
            try {
                return LENIENT.readTree(cleaned);
            } catch (JsonProcessingException e2) {
                String head = candidate.substring(0, Math.min(candidate.length(), 500));
                throw new IllegalArgumentException("Failed to parse AI JSON (lenient). Head: " + head, e2);
            }
        }
    }

    private static JsonNode reparseStrict(JsonNode node) {
        try {
            String strict = STRICT.writeValueAsString(node);
            return STRICT.readTree(strict);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to normalize JSON strictly", e);
        }
    }

    private static void requireKeys(ObjectNode obj, Set<String> keys) {
        List<String> missing = new ArrayList<>();
        for (String k : keys) {
            if (!obj.has(k)) missing.add(k);
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("AI JSON missing required keys: " + missing);
        }
    }
}
