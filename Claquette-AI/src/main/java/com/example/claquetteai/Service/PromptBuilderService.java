package com.example.claquetteai.Service;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilderService {
        // 🟢 المقدمة الأساسية (تستخدم في كل الأقسام)
        private static final String BASE_PROMPT = """
        Generate a COMPLETE, production-ready ARABIC screenplay in valid JSON (UTF-8) 
        using only the provided story_description (Arabic, string) and episodes_count (integer). 
        Adhere to all Saudi cultural, legal, and moral standards. 
        Fill missing information with realistic assumptions, listing them under project.assumptions. 
        The output must strictly conform to the schema and guidelines below.
        

        Persist until the screenplay is complete for all episodes, producing 2 fully written scenes per episode 
        (with med, richly detailed scene content and clear, realistic script dialogue and structure for each scene, 
        ensuring notable length and substance for every scene). 
        Think step-by-step to ensure dialogue distribution, progression, and compliance before producing the full result.

        ## STEPS
        1. **Project Details:** Derive and generate a strong Arabic title from story_description. Populate all fields.
        2. **Themes:** Extract 2–5 clear themes relevant to the story.
        3. **Characters:** Invent 6–10 Saudi characters with details (traits, arc, backstory, relationships, voice_notes).
        4. **Season Map:** Assign each episode a unique title, short logline, focus characters, and themes.
        5. **Episodes:** Each episode must have 2 med scenes with action + dialogue.
        6. **Casting:** Suggest 1–3 Saudi actors for each character (2019+ generation).
        7. **Production Book:** Suggest realistic Saudi locations, sets, props, wardrobe, music, and risk mitigation.
        8. **Compliance Checklist:** Ensure cultural, legal, and moral compliance.
        """;

        // 🟢 قسم Project
        public String projectPrompt(String storyDescription, int episodesCount) {
            return BASE_PROMPT + """

        ### TASK
        Generate ONLY the "project" section.

        story_description: "%s"
        episodes_count: %d
        """.formatted(storyDescription, episodesCount);
        }

        // 🟢 قسم Characters
        public String charactersPrompt(String storyDescription) {
            return BASE_PROMPT + """

        ### TASK
        Generate ONLY the "characters" section with 6–10 characters.

        story_description: "%s"
        """.formatted(storyDescription);
        }

        // 🟢 قسم Episode واحد
        public String episodePrompt(String projectJson, int episodeNumber) {
            return BASE_PROMPT + """

        ### TASK
        Based on this project and characters:
        %s

        Generate ONLY episode %d in JSON with: episode, title, summary, dramatic_goal, key_characters, scenes, climax, tag.
        """.formatted(projectJson, episodeNumber);
        }

        // 🟢 قسم Casting
        public String castingPrompt(String projectJson) {
            return BASE_PROMPT + """

        ### TASK
        Based on this project and characters:
        %s

        Generate ONLY the "casting" section.
        """.formatted(projectJson);
        }

    }