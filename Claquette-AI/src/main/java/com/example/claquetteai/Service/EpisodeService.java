package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.EpisodeRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

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


    public List<Episode> getMyEpisodes(Integer userId, Integer projectId){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        Project project = projectRepository.findProjectById(projectId);
        if (project == null){
            throw new ApiException("project not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("user not authorised to do this");
        }
        List<Episode> episodes = episodeRepository.findEpisodesByProject(project);
        return episodes;
    }

}