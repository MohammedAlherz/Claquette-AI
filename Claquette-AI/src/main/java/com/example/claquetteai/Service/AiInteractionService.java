package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiInteractionService {
    private final ProjectRepository projectRepository;
    private final FilmService filmService;
    private final CharacterService characterService;
    private final EpisodeService episodeService;
    private final CastingService castingService;
    private final UserRepository userRepository;

    /**
     * Main method to generate complete screenplay
     */
    public Project generateFullScreenplay(Integer projectId, Integer userId) throws Exception {
        // Step 1: Get existing project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        if (!user.getCompany().getIsSubscribed() && user.getUseAI() <= 0){
            throw new ApiException("you cannot generate project using AI subscribe");
        }

        // Step 2: Generate characters for the project using CharacterService
        Set<FilmCharacters> characters = characterService.generateCharacters(project, project.getDescription());
        project.setCharacters(characters);

        // Step 3: Generate Film OR Episodes based on project type
        if ("FILM".equals(project.getProjectType())) {
            // Generate film using FilmService
            Film film = filmService.generateFilmWithScenes(project);
            project.setFilms(film);
        } else {
            // For series, determine episode count and generate episodes using EpisodeService
            int episodeCount = project.getEpisodeCount();
            Set<Episode> episodes = new HashSet<>();

            for (int i = 1; i <= episodeCount; i++) {
                Episode episode = episodeService.generateEpisodeWithScenes(project, i);
                episodes.add(episode);
            }

            project.setEpisodes(episodes);
        }

        // Step 4: Generate casting recommendations using CastingService
        Set<CastingRecommendation> casting = castingService.generateCasting(project);
        project.setCastingRecommendations(casting);

        user.setUseAI(user.getUseAI()-1);
        userRepository.save(user);

        // Save final project with all relationships
        return projectRepository.save(project);
    }

//    /**
//     * Determines episode count for series projects
//     * You can extend this logic based on your business rules
//     */
//    private int determineEpisodeCount(Project project) {
//        // Default based on genre or other criteria
//        if (project.getGenre() != null && project.getGenre().toLowerCase().contains("mini")) {
//            return 3; // Mini series
//        } else if ("Comedy".equalsIgnoreCase(project.getGenre())) {
//            return 8; // Comedy series
//        } else {
//            return 6; // Default series length
//        }
//    }
}