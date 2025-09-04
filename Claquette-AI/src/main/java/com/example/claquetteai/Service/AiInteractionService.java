package com.example.claquetteai.Service;

import com.example.claquetteai.Model.CastingRecommendation;
import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Root;
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
    @Value("${spring.ai.openai.api-key}")
    private String API_KEY;
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private final JsonExtractor jsonExtractor; // inject extractor
    private final ProjectRepository projectRepository;
    private final PromptBuilderService promptBuilderService;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

//    public void generateScreenplay(String storyDescription, Integer episodesCount) throws Exception {
//        String prompt = """
//                Generate a COMPLETE, production-ready ARABIC screenplay in valid JSON (UTF-8) using only the provided story_description (Arabic, string) and episodes_count (integer). Adhere to all Saudi cultural, legal, and moral standards. Fill missing information with realistic assumptions, listing them under project.assumptions. The output must strictly conform to the schema and guidelines below.
//
//                Persist until the screenplay is complete for all episodes, producing 20–30 fully written scenes per episode (with **very long, richly detailed scene content** and clear, realistic script dialogue and structure for each scene, ensuring **notable length and substance** for every scene). Think step-by-step to ensure dialogue distribution, progression, and compliance before producing the full result.
//
//                ## STEPS
//
//                1. **Project Details:** \s
//                   - Derive and generate a strong Arabic title from story_description. \s
//                   - Populate all project fields, including creative/realistic assumptions.
//
//                2. **Themes:** \s
//                   - Extract clear 2–5 core themes relevant to the story.
//
//                3. **Characters:** \s
//                   - Invent 6–10 characters with Saudi names, logical ages and clear roles. \s
//                   - Specify traits, arc, backstory, relationships, voice_notes, meaningful goals/obstacles.
//
//                4. **Season Map:** \s
//                   - For each episode, assign a unique Arabic episode title, short logline, focus characters, and themes.
//
//                5. **Episodes:** \s
//                   - For each episode (count = episodes_count):
//                     - Write **at least 20–30 scenes per episode**. \s
//                     - **Every scene must be substantially long and extended.** \s
//                     - **Each scene must include:** \s
//                       • slug (INT./EXT. – location – time in Arabic), \s
//                       • sound (scene-specific sound description), \s
//                       • mood_light (lighting/mood), \s
//                       • purpose (dramatic beat), \s
//                       • action (clear visual/movement/context), \s
//                       • dialogue (rich, authentic Saudi dialect in-character exchanges, written as fully developed, multi-line script dialogue with lines, beats, and asides), \s
//                       • internal_monologue (when impactful), \s
//                       • turning_point (leave empty if none).
//                     - **Scenes must be very long and contain a fully clear, multi-line script for at least 2 characters per scene (using Saudi dialect for speech, Modern Standard Arabic for action), resulting in substantial and clear screenplay content per scene.** \s
//                     - Ensure entrances/exits, cause-effect logic, a strong climax, and a tag for next-episode attraction.
//
//                6. **Casting:** \s
//                   - For each main character, suggest 1–3 suitable Saudi actors (2019+ generation) with rationale and match_percent.
//
//                7. **Production Book:** \s
//                   - Suggest realistic Saudi locations, sets, props, wardrobe, music/sound palette, practical shooting blocks, and risk mitigation steps.
//
//                8. **Compliance Checklist:** \s
//                   - Ensure checklist covers all legal/moral/cultural points for Saudi TV.
//
//                ## OUTPUT FORMAT
//
//                - Return **ONE valid JSON** object conforming to the schema (below).
//                - **All keys** as in schema and in the shown order, nestings preserved, no additional or missing keys.
//                - **All values** in Arabic (except for English keys as per schema).
//                - Numbers as numbers. Empty arrays permitted if truly necessary.
//                - NO markdown, comments, nor prose outside of the JSON (output the JSON alone).
//                - **No trailing commas, and all JSON syntactic rules must be valid.**
//
//                ### JSON SCHEMA
//
//                {
//                  "project": {
//                    "title": "string (عنوان اشتقه من الوصف)",
//                    "story_description": "string (من المدخل)",
//                    "episodes_count": number,
//                    "episode_duration_hint": "string (30–40 دقيقة)",
//                    "assumptions": ["string", "..."]
//                  },
//                  "themes": ["string", "..."],
//                  "characters": [
//                    {
//                      "name": "string (اسم سعودي/خليجي)",
//                      "age": number,
//                      "role": "string (بطل/مساند/خصم...)",
//                      "traits": ["string", "..."],
//                      "backstory": "string",
//                      "relationships": ["string", "..."],
//                      "goal": "string",
//                      "obstacle": "string",
//                      "arc": "string",
//                      "voice_notes": "string (نبرة/إيقاع/جُمل مفتاحية)"
//                    }
//                  ],
//                  "season_map": [
//                    {
//                      "episode": number,
//                      "title": "string",
//                      "logline": "string (جملة خطّافة)",
//                      "focus_characters": ["string", "..."],
//                      "themes": ["string", "..."]
//                    }
//                  ],
//                  "episodes": [
//                    {
//                      "episode": number,
//                      "title": "string",
//                      "summary": "string (3–4 جمل)",
//                      "dramatic_goal": "string",
//                      "key_characters": ["string", "..."],
//                      "scenes": [
//                        {
//                          "slug": "string (INT./EXT. – المكان – وقت اليوم)",
//                          "sound": "string (وصف الأصوات)",
//                          "mood_light": "string (إضاءة/جو)",
//                          "purpose": "string (Beat درامي)",
//                          "action": "string (وصف بصري/حركة/سياق)",
//                          "dialogue": [
//                            { "character": "string", "line": "string (جملة/جمل متعددة)", "aside": "string (اختياري: نبرة/فعل موجز)" },
//                            { "character": "string", "line": "string" }
//                          ],
//                          "internal_monologue": [
//                            { "character": "string", "thought": "string" }
//                          ],
//                          "turning_point": "string (نقطة تغيير داخل المشهد إن وُجدت)"
//                        }
//                      ],
//                      "climax": "string (ذروة الحلقة)",
//                      "tag": "string (خطّاف للحلقة التالية)"
//                    }
//                  ],
//                  "casting": [
//                    {
//                      "character": "string",
//                      "suggestions": [
//                        { "actor": "string (ممثل سعودي من الجيل الجديد)", "why": "string", "match_percent": number }
//                      ]
//                    }
//                  ],
//                  "production_book": {
//                    "locations": ["string (مدينة/حي/أماكن داخلية/خارجية)", "..."],
//                    "art_wardrobe_props": {
//                      "sets": ["string", "..."],
//                      "props": ["string", "..."],
//                      "wardrobe": ["string", "..."]
//                    },
//                    "music_sound_identity": "string (مزاج/آلات/وقفات صمت محافظة)",
//                    "schedule_outline": ["string (بلوكات تصوير واقعية)", "..."],
//                    "risk_mitigation": ["string (طقس/مواقع/توفر ممثلين وحلول بديلة)", "..."]
//                  },
//                  "compliance_checklist": [
//                    "string (احترام الذوق العام)",
//                    "string (عدم تبرير الأذى/عدم رومَنة المعتدي)",
//                    "string (تمثيل إيجابي للأسرة/المجتمع/الجهات الرسمية)",
//                    "string (حذف الألفاظ البذيئة/المشاهد الصريحة)"
//                  ]
//                }
//
//                ---
//
//                ### EXAMPLE (TRUNCATED for structure, use longer fields in real output):
//
//                **Input:** \s
//                story_description: "قصة صعود رائد أعمال سعودي شاب من الأحساء يبتكر تطبيقاً يُغيّر حياة مجتمعه رغم مقاومة المجتمع التقليدي." \s
//                episodes_count: 5 \s
//
//                **CORRECT OUTPUT:** \s
//                { \s
//                  "project": {...},
//                  "themes": [...],
//                  "characters": [
//                    {...}, {...}
//                  ],
//                  "season_map": [
//                    {"episode": 1, ...},
//                    {"episode": 2, ...}
//                  ],
//                  "episodes": [
//                    {
//                      "episode": 1,
//                      "title": "...",
//                      ...
//                      "scenes": [
//                        {
//                          "slug": "داخلي - منزل يوسف - مساء",
//                          "sound": "هدوء مع أصوات تلفاز ...",
//                          "mood_light": "إضاءة دافئة ...",
//                          "purpose": "تقديم البطل وصراعه الأولي ...",
//                          "action": "يوسف يدخل وهو يحمل حقيبة ...",
//                          "dialogue": [
//                            {"character": "يوسف", "line": "سلام عليكم يا أمي!", "aside": "بحماس"},
//                            {"character": "أم يوسف", "line": "وعليكم السلام..."}
//                          ],
//                          "internal_monologue": [
//                            {"character": "يوسف", "thought": "لازم أنجح هالمرة..."}
//                          ],
//                          "turning_point": "أول خلاف مع الأب حول الفكرة."
//                        },
//                        ...
//                      ],
//                      "climax": "...",
//                      "tag": "..."
//                    },
//                    ...
//                  ],
//                  "casting": [
//                    {
//                      "character": "يوسف",
//                      "suggestions": [
//                        {"actor": "فهد البتيري", "why": "يجيد أدوار الشباب العصري.", "match_percent": 87}
//                      ]
//                    },
//                    ...
//                  ],
//                  "production_book": {
//                    "locations": ["الأحساء", "مقهى حديث", "منزل أسرة يوسف"],
//                    "art_wardrobe_props": {
//                      "sets": ["غرفة يوسف", "مكتب صغير"],
//                      "props": ["هاتف حديث", "لوحة مشروع"],
//                      "wardrobe": ["ثوب أبيض عصري", "عباءة تقليدية"]
//                    },
//                    "music_sound_identity": "آلات شرقية حديثة مع لحظات هادئة",
//                    "schedule_outline": ["تصوير في الأحساء ثلاثة أيام...", "..."],
//                    "risk_mitigation": ["اختيار أوقات سفر مناسبة...", "..."]
//                  },
//                  "compliance_checklist": [
//                    "احترام الذوق العام",
//                    "عدم تبرير الأذى",
//                    "تمثيل إيجابي للأسرة",
//                    "حذف الألفاظ البذيئة"
//                  ]
//                }
//
//                ---
//
//                **REMEMBER:** \s
//                - Output must be a single, fully-structured, valid JSON object ONLY, written in Arabic, adhering to all compliance and schema requirements.
//                - Scenes line must be **very long** and substantially written to reflect 30 to 40 minutes per episode, with fully clear, extended screenplay script and substantial multi-line scene content. **Write 20 to 30 scenes per episode, making each scene detailed and long.**
//                - Do not return anything outside the JSON object.
//                - Persist in expanding all episodes and scenes with realistic, richly detailed content for a Saudi TV screenplay.
//                story_description: "%s"
//                episodes_count: %d
//                """.formatted(storyDescription, episodesCount);

//        String requestBody = """
//        {
//          "model": "gpt-4.1",
//          "messages": [
//            {"role": "system", "content": "You are a professional Saudi screenwriter and must output JSON only."},
//            {"role": "user", "content": %s}
//          ],
//          "temperature": 0.7
//        }
//        """.formatted(mapper.writeValueAsString(prompt));
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(ENDPOINT))
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer " + API_KEY)
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        System.out.println("API Response: " + response.body());
//        JsonNode body = mapper.readTree(response.body());
//        String screenplayJson = body.get("choices").get(0).get("message").get("content").asText();
//
//        Project project = jsonExtractor.extractProject(screenplayJson);
//        projectRepository.save(project);
//    }

    // 🟢 إنشاء المشروع (project + themes + characters + season_map)


    private String sanitizeJson(String raw) {
        // شيل backticks أو أي "```json" أو "```"
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("(?s)```(json)?", "").trim();
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }
        return cleaned;
    }



    public Project generateFullScreenplay(String storyDescription, int episodesCount) throws Exception {

        // 1️⃣ إنشاء المشروع (Project)
        Project project = generateProject(storyDescription, episodesCount);

        // 2️⃣ إنشاء الشخصيات (Characters)
        generateCharacters(project, storyDescription);


        // 3️⃣ إنشاء الحلقات (Episodes)
        for (int i = 1; i <= episodesCount; i++) {
            generateEpisode(project, i);
        }

        // 4️⃣ إنشاء الترشيحات (Casting)
        generateCasting(project);

        // حفظ التغييرات الأخيرة
        return projectRepository.save(project);
    }




    public Project generateProject(String storyDescription, int episodesCount) throws Exception {
        String prompt = promptBuilderService.projectPrompt(storyDescription, episodesCount);

        String json = askModel(prompt);
        Project project = jsonExtractor.extractProject(json);

        return projectRepository.save(project);
    }

    public void generateEpisode(Project project, int episodeNumber) throws Exception {
        String prompt = promptBuilderService.episodePrompt(project.getDescription(), episodeNumber);
        String json = askModel(prompt);
        Set<Episode> episodes = jsonExtractor.extractEpisodes(json, project);
        project.setEpisodes(episodes);
        projectRepository.save(project);
    }

    public void generateCasting(Project project) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode projectJson = mapper.valueToTree(project);

        String prompt = promptBuilderService.castingPrompt(projectJson.toString());

        String json = askModel(prompt);
        Set<CastingRecommendation> casting = jsonExtractor.extractCasting(json, project);
        project.setCastingRecommendations(casting);
        projectRepository.save(project);
    }


    public void generateCharacters(Project project, String storyDescription) throws Exception {
        String prompt = promptBuilderService.charactersPrompt(storyDescription);
        String json = askModel(prompt);
        JsonNode root = new ObjectMapper().readTree(json);
        Set<FilmCharacters> characters = jsonExtractor.extractCharacters(root, project);
        project.setCharacters(characters);
        projectRepository.save(project);
    }




    // ⭕ دالة عامة ترسل الطلب للـ API
    private String askModel(String prompt) throws Exception {
        String requestBody = """
        {
          "model": "gpt-4.1",
          "messages": [
            {"role": "system", "content": "You are a professional Saudi screenwriter and must output JSON only."},
            {"role": "user", "content": %s}
          ],
          "temperature": 0.7
        }
        """.formatted(mapper.writeValueAsString(prompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = mapper.readTree(response.body());

        if (body.has("error")) {
            throw new RuntimeException("OpenAI API Error: " + body.get("error").get("message").asText());
        }

        return sanitizeJson(body.get("choices").get(0).get("message").get("content").asText());
    }

}
