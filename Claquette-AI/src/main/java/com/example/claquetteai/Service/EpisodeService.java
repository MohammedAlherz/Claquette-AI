package com.example.claquetteai.Service;

import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;

    // Regular CRUD operations
    public Episode createEpisode(Episode episode) {
        return episodeRepository.save(episode);
    }

    // AI Generation method
    public Episode generateEpisodeWithScenes(Project project, int episodeNumber) throws Exception {
        // Build prompt for specific episode with scenes
        String prompt = promptBuilderService.episodePrompt(project.getDescription(), episodeNumber);

        // Get AI response and extract episode with scenes
        String json = aiClientService.askModel(prompt);
        Episode episode = jsonExtractor.extractEpisodeWithScenes(json, project, episodeNumber);

        // Save and return the episode
        return episodeRepository.save(episode);
    }

}