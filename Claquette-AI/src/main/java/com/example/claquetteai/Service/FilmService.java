package com.example.claquetteai.Service;

import com.example.claquetteai.Model.Film;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService; // New service for AI calls


    // AI Generation method
    public Film generateFilmWithScenes(Project project) throws Exception {
        // Build prompt for film generation
        String prompt = promptBuilderService.filmPrompt(project.getDescription());

        // Get AI response and extract film with scenes
        String json = aiClientService.askModel(prompt);
        Film film = jsonExtractor.extractFilmWithScenes(json, project);

        // Save and return the film
        return filmRepository.save(film);
    }

}