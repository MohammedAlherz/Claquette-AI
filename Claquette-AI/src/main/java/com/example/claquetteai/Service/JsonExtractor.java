package com.example.claquetteai.Service;

import com.example.claquetteai.Model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JsonExtractor {

        private final ObjectMapper mapper = new ObjectMapper();

//        public Project extractProject(String screenplayJson) throws Exception {
//            JsonNode root = mapper.readTree(screenplayJson);
//
//            // 🎯 Project
//            Project project = new Project();
//            project.setTitle(root.path("project").path("title").asText());
//            project.setDescription(root.path("project").path("story_description").asText());
//            project.setProjectType("SERIES"); // or FILM if you detect type
//            project.setStatus("IN_DEVELOPMENT");
//
//            // budget not in JSON, you can set default
//            project.setBudget(0.0);
//
//            // 🎯 Themes (merge into description for now)
//            if (root.has("themes")) {
//                List<String> themes = new ArrayList<>();
//                root.path("themes").forEach(node -> themes.add(node.asText()));
//                project.setDescription(project.getDescription() + " | Themes: " + String.join(" | ", themes));
//            }
//
//            // 🎯 Characters
//            Set<FilmCharacters> characters = new HashSet<>();
//            Set<String> seen = new HashSet<>();
//            for (JsonNode charNode : root.path("characters")) {
//                String uniqueKey = charNode.path("name").asText() + "-" + charNode.path("age").asInt();
//                if (seen.contains(uniqueKey)) continue;
//                seen.add(uniqueKey);
//
//                FilmCharacters c = new FilmCharacters();
//                c.setProject(project);
//                c.setName(charNode.path("name").asText());
//                c.setAge(charNode.path("age").asInt());
//                c.setRoleInStory(charNode.path("role").asText());
//
//                // traits
//                if (charNode.has("traits")) {
//                    List<String> traitsList = new ArrayList<>();
//                    for (JsonNode t : charNode.path("traits")) {
//                        traitsList.add(t.asText());
//                    }
//                    c.setPersonalityTraits(String.join(" | ", traitsList));
//                }
//
//                // backstory + relationships + goal + obstacle → background
//                List<String> backgroundParts = new ArrayList<>();
//                if (charNode.has("backstory")) backgroundParts.add(charNode.path("backstory").asText());
//                if (charNode.has("relationships")) {
//                    List<String> rels = new ArrayList<>();
//                    for (JsonNode r : charNode.path("relationships")) rels.add(r.asText());
//                    backgroundParts.add("العلاقات: " + String.join(" | ", rels));
//                }
//                if (charNode.has("goal")) backgroundParts.add("الهدف: " + charNode.path("goal").asText());
//                if (charNode.has("obstacle")) backgroundParts.add("العقبة: " + charNode.path("obstacle").asText());
//                c.setBackground(String.join(" | ", backgroundParts));
//
//                // arc + voice_notes → characterArc
//                List<String> arcParts = new ArrayList<>();
//                if (charNode.has("arc")) arcParts.add(charNode.path("arc").asText());
//                if (charNode.has("voice_notes"))
//                    arcParts.add("ملاحظات الصوت: " + charNode.path("voice_notes").asText());
//                c.setCharacterArc(String.join(" | ", arcParts));
//
//                characters.add(c);
//            }
//            project.setCharacters(characters);
//
//            // 🎯 Episodes
//            Set<Episode> episodes = new HashSet<>();
//            for (JsonNode epNode : root.path("episodes")) {
//                Episode e = new Episode();
//                e.setProject(project);
//                e.setEpisodeNumber(epNode.path("episode").asInt());
//                e.setTitle(epNode.path("title").asText());
//
//                String summary = epNode.path("summary").asText();
//                if (epNode.has("dramatic_goal")) {
//                    summary += " | Goal: " + epNode.path("dramatic_goal").asText();
//                }
//                e.setSummary(summary);
//
//                // duration not in JSON, default to 40
//                e.setDurationMinutes(40);
//
//                episodes.add(e);
//            }
//            project.setEpisodes(episodes);
//
//            // 🎯 Casting
//            Set<CastingRecommendation> casting = new HashSet<>();
//            for (JsonNode castNode : root.path("casting")) {
//                String characterName = castNode.path("character").asText();
//                for (JsonNode sugNode : castNode.path("suggestions")) {
//                    CastingRecommendation cr = new CastingRecommendation();
//                    cr.setProject(project);
//                    cr.setCharacterName(characterName);
//                    cr.setRecommendedActorName(sugNode.path("actor").asText());
//                    cr.setReasoning(sugNode.path("why").asText());
//                    cr.setMatchScore(sugNode.path("match_percent").asDouble() / 100.0); // convert %
//                    cr.setProfile("Casting suggestion");
//                    casting.add(cr);
//                }
//            }
//            project.setCastingRecommendations(casting);
//
//            return project;
//        }

    // 🟢 Project فقط
    public Project extractProject(String json) throws Exception {
        JsonNode root = mapper.readTree(json);

        Project project = new Project();
        project.setTitle(root.path("project").path("title").asText());
        project.setDescription(root.path("project").path("story_description").asText());
        project.setProjectType("SERIES");
        project.setStatus("IN_DEVELOPMENT");
        project.setBudget(0.0);
        project.setStartProjectDate(LocalDateTime.now());
        project.setEndProjectDate(LocalDateTime.now().plusDays(10));


        return project;
    }

    // 🟢 Episodes + Scenes
    public Set<Episode> extractEpisodes(String json, Project project) throws Exception {
        JsonNode root = mapper.readTree(json);
        Set<Episode> episodes = new HashSet<>();

        for (JsonNode epNode : root.path("episodes")) {
            Episode e = new Episode();
            e.setProject(project);
            e.setEpisodeNumber(epNode.path("episode").asInt());
            e.setTitle(epNode.path("title").asText());

            String summary = epNode.path("summary").asText();
            if (epNode.has("dramatic_goal")) {
                summary += " | Goal: " + epNode.path("dramatic_goal").asText();
            }
            e.setSummary(summary);
            e.setDurationMinutes(40);

            // 🎬 Scenes
            Set<Scene> scenes = new HashSet<>();
            int sceneCounter = 1;
            for (JsonNode sceneNode : epNode.path("scenes")) {
                Scene scene = new Scene();
                scene.setSceneNumber(sceneCounter++);
                scene.setSetting(sceneNode.path("slug").asText());
                scene.setActions(sceneNode.path("action").asText());

                // Flatten dialogue array into one string
                StringBuilder dialogueBuilder = new StringBuilder();
                for (JsonNode d : sceneNode.path("dialogue")) {
                    String character = d.path("character").asText();
                    String line = d.path("line").asText();
                    String aside = d.has("aside") ? " (" + d.path("aside").asText() + ")" : "";
                    dialogueBuilder.append(character).append(": ").append(line).append(aside).append("\n");
                }
                scene.setDialogue(dialogueBuilder.toString().trim());

                // Put other info into departmentNotes
                StringBuilder notes = new StringBuilder();
                if (sceneNode.has("sound")) notes.append("Sound: ").append(sceneNode.path("sound").asText()).append(" | ");
                if (sceneNode.has("mood_light")) notes.append("Mood: ").append(sceneNode.path("mood_light").asText()).append(" | ");
                if (sceneNode.has("purpose")) notes.append("Purpose: ").append(sceneNode.path("purpose").asText()).append(" | ");
                if (sceneNode.has("turning_point")) notes.append("Turning: ").append(sceneNode.path("turning_point").asText());
                scene.setDepartmentNotes(notes.toString().trim());

                scenes.add(scene);
            }

            e.setScenes(scenes); // Make sure Episode entity has a OneToMany to Scene
            episodes.add(e);
        }

        return episodes;
    }



    public Set<FilmCharacters> extractCharacters(JsonNode root, Project project) {
        Set<FilmCharacters> characters = new HashSet<>();
        Set<String> seen = new HashSet<>(); // لتجنب التكرار

        for (JsonNode charNode : root.path("characters")) {
            String uniqueKey = charNode.path("name").asText() + "-" + charNode.path("age").asInt();
            if (seen.contains(uniqueKey)) continue;
            seen.add(uniqueKey);

            FilmCharacters c = new FilmCharacters();
            c.setProject(project);
            c.setName(charNode.path("name").asText());
            c.setAge(charNode.path("age").asInt());
            c.setRoleInStory(charNode.path("role").asText());

            // traits
            if (charNode.has("traits")) {
                List<String> traitsList = new ArrayList<>();
                for (JsonNode t : charNode.path("traits")) {
                    traitsList.add(t.asText());
                }
                c.setPersonalityTraits(String.join(" | ", traitsList));
            }

            // background
            List<String> backgroundParts = new ArrayList<>();
            if (charNode.has("backstory")) backgroundParts.add(charNode.path("backstory").asText());
            if (charNode.has("relationships")) {
                List<String> rels = new ArrayList<>();
                for (JsonNode r : charNode.path("relationships")) rels.add(r.asText());
                backgroundParts.add("العلاقات: " + String.join(" | ", rels));
            }
            if (charNode.has("goal")) backgroundParts.add("الهدف: " + charNode.path("goal").asText());
            if (charNode.has("obstacle")) backgroundParts.add("العقبة: " + charNode.path("obstacle").asText());
            c.setBackground(String.join(" | ", backgroundParts));

            // arc
            List<String> arcParts = new ArrayList<>();
            if (charNode.has("arc")) arcParts.add(charNode.path("arc").asText());
            if (charNode.has("voice_notes")) arcParts.add("ملاحظات الصوت: " + charNode.path("voice_notes").asText());
            c.setCharacterArc(String.join(" | ", arcParts));

            characters.add(c);
        }

        return characters;
    }


    // 🟢 Casting
    public Set<CastingRecommendation> extractCasting(String json, Project project) throws Exception {
        JsonNode root = mapper.readTree(json);
        Set<CastingRecommendation> casting = new HashSet<>();

        for (JsonNode castNode : root.path("casting")) {
            String characterName = castNode.path("character").asText();
            for (JsonNode sugNode : castNode.path("suggestions")) {
                System.out.println("Character JSON: " + castNode.toPrettyString());
                CastingRecommendation cr = new CastingRecommendation();
                cr.setProject(project);
                cr.setCharacterName(characterName);
                cr.setRecommendedActorName(sugNode.path("actor").asText());
                cr.setReasoning(sugNode.path("why").asText());
                cr.setMatchScore(sugNode.path("match_percent").asDouble() / 100.0);
                cr.setProfile("Casting suggestion");
                casting.add(cr);
            }
        }

        return casting;
    }


    }
