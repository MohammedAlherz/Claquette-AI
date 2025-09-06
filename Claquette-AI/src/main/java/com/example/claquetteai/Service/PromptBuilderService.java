package com.example.claquetteai.Service;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilderService {
    // Base prompt for all AI interactions
    private static final String BASE_PROMPT = """
        Generate a COMPLETE, production-ready ARABIC screenplay in valid JSON (UTF-8) 
        using only the provided story_description (Arabic, string) and episodes_count (integer). 
        Adhere to all Saudi cultural, legal, and moral standards. 
        Fill missing information with realistic assumptions, listing them under project.assumptions. 
        The output must strictly conform to the schema and guidelines below.
        
        Persist until the screenplay is complete for all episodes, producing 15-25 fully written scenes per episode 
        (with very long, richly detailed scene content and clear, realistic script dialogue and structure for each scene, 
        ensuring notable length and substance for every scene). 
        Think step-by-step to ensure dialogue distribution, progression, and compliance before producing the full result.

        ## STEPS
        1. **Project Details:** Derive and generate a strong Arabic title from story_description. Populate all fields.
        2. **Themes:** Extract 2–5 clear themes relevant to the story.
        3. **Characters:** Invent 6–10 Saudi characters with details (traits, arc, backstory, relationships, voice_notes).
        4. **Season Map:** Assign each episode a unique title, short logline, focus characters, and themes.
        5. **Episodes:** Each episode must have 15-25 detailed scenes with action + dialogue.
        6. **Casting:** Suggest 1–3 Saudi actors for each character (2022+ generation).
        7. **Production Book:** Suggest realistic Saudi locations, sets, props, wardrobe, music, and risk mitigation.
        8. **Compliance Checklist:** Ensure cultural, legal, and moral compliance.
        
        CRITICAL: Return ONLY valid JSON. No explanations, no markdown, no code blocks. Start with { and end with }.
        """;

    /**
     * Generates prompt for creating project information
     */
    public String projectPrompt(String storyDescription, int episodesCount) {
        return BASE_PROMPT + """

        ### TASK
        Generate ONLY the "project" section in JSON format.

        {
          "project": {
            "title": "string (عنوان اشتقه من الوصف)",
            "story_description": "string (من المدخل)",
            "episodes_count": number,
            "assumptions": ["string", "..."]
          }
        }

        story_description: "%s"
        episodes_count: %d
        """.formatted(storyDescription, episodesCount);
    }

    /**
     * Generates prompt for creating character profiles
     */
    public String charactersPrompt(String storyDescription) {
        return BASE_PROMPT + """

        ### TASK
        Generate ONLY the "characters" section with 6–10 characters in JSON format.

        {
          "characters": [
            {
              "name": "string (اسم سعودي/خليجي)",
              "age": number,
              "role": "string (بطل/مساند/خصم...)",
              "traits": ["string", "..."],
              "backstory": "string",
              "relationships": ["string", "..."],
              "goal": "string",
              "obstacle": "string",
              "arc": "string",
              "voice_notes": "string (نبرة/إيقاع/جُمل مفتاحية)"
            }
          ]
        }
        
        only first name with out last name
        make the age of characters logically not all older or all young make it average and teens and young and parents range of ages from 6 to 40 and if some grandfathers make the range of age 50 to 70

        story_description: "%s"
        """.formatted(storyDescription);
    }

    /**
     * Generates prompt for creating episode with scenes
     * Fixed method signature to match AiInteractionService call
     */
    public String episodePrompt(String projectDescription, int episodeNumber) {
        return BASE_PROMPT + """

        ### TASK
        Based on this project description: "%s"
        
        Generate episode %d with detailed scenes in JSON format:

        {
          "episode": {
            "episode": %d,
            "title": "string",
            "summary": "string (3–4 جمل)",
            "dramatic_goal": "string",
            "key_characters": ["string", "..."],
            "scenes": [
              {
                "slug": "string (INT./EXT. – المكان – وقت اليوم)",
                "sound": "string (وصف الأصوات)",
                "mood_light": "string (إضاءة/جو)",
                "purpose": "string (Beat درامي)",
                "action": "string (وصف بصري/حركة/سياق)",
                "dialogue": [
                  { "character": "string", "line": "string (جملة/جمل متعددة)", "aside": "string (اختياري: نبرة/فعل موجز)" }
                ],
                "internal_monologue": [
                  { "character": "string", "thought": "string" }
                ],
                "turning_point": "string (نقطة تغيير داخل المشهد إن وُجدت)"
              }
            ],
            "climax": "string (ذروة الحلقة)",
            "tag": "string (خطّاف للحلقة التالية)"
          }
        }

        Create 15-25 detailed scenes with rich dialogue and action.
        """.formatted(projectDescription, episodeNumber, episodeNumber);
    }

    /**
     * Generates prompt for creating film with scenes
     */
    public String filmPrompt(String projectDescription) {
        return BASE_PROMPT + """

        ### TASK
        Based on this project description: "%s"
        
        Generate a complete FILM with scenes in JSON format:

        {
          "film": {
            "title": "string",
            "summary": "string (وصف الفيلم)",
            "duration_minutes": number,
            "scenes": [
              {
                "slug": "string (INT./EXT. – المكان – وقت اليوم)",
                "sound": "string (وصف الأصوات)",
                "mood_light": "string (إضاءة/جو)",
                "purpose": "string (Beat درامي)",
                "action": "string (وصف بصري/حركة/سياق)",
                "dialogue": [
                  { "character": "string", "line": "string (جملة/جمل متعددة)", "aside": "string (اختياري: نبرة/فعل موجز)" }
                ],
                "internal_monologue": [
                  { "character": "string", "thought": "string" }
                ],
                "turning_point": "string (نقطة تغيير داخل المشهد إن وُجدت)"
              }
            ]
          }
        }

        Create 80-120 detailed scenes for a complete feature film.
        """.formatted(projectDescription);
    }

    /**
     * Generates prompt for creating casting recommendations
     */
    public String castingPrompt(String projectInfo) {
        return BASE_PROMPT + """

        ### TASK
        Based on this project information: "%s"
        
        Generate ONLY the "casting" section in JSON format:

        {
          "casting": [
            {
              "character": "string",
              "suggestions": [
                { 
                  "actor": "string (ممثل سعودي من الجيل الجديد)", 
                  "why": "string", 
                  "match_percent": number,
                  "profile": "string" (نبذه عن تاريخ الممثل هذا),
                  "age": number
                }
              ]
            }
          ]
        }

        Suggest 2-3 suitable Saudi actors (born after 1980 and they has new movies or series in saudi arabia tv 2022+) for each main character.
        Include detailed reasoning for each casting choice.
        """.formatted(projectInfo);
    }
}