package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.Film;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.FilmRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;


    public Film generateFilmFromAI(Integer projectId) {
        try {
            // Get the project
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ApiException("Project not found"));

            // Get AI response from project's AiInteraction
            String aiResponseJson = project.getAiInteractions().getAiResponse();

            // Parse AI response JSON
            JsonNode rootNode = objectMapper.readTree(aiResponseJson);
            JsonNode projectNode = rootNode.get("project");

            // Create Film entity from AI response
            Film film = new Film();
            film.setTitle(projectNode.get("title").asText());
            film.setSummary(projectNode.get("story_description").asText());
            film.setDurationMinutes(projectNode.get("duration_minutes").asInt());
            film.setProject(project);
            film.setCreatedAt(LocalDateTime.now());
            film.setUpdatedAt(LocalDateTime.now());

            // Save and return
            return filmRepository.save(film);

        } catch (Exception e) {
            throw new ApiException("Failed to generate film from AI response: ");
        }
    }
}