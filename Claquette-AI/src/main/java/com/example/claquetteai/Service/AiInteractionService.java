package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CharacterRepository characterRepository; // Add this
    private final SceneRepository sceneRepository; // Add this

    /**
     * Main method to generate complete screenplay
     * FIXED: Added proper transaction management and entity saving order
     */
    @Transactional // This ensures all operations happen in a single transaction
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

        // CRITICAL: Save the project with characters first to establish the relationship
        project = projectRepository.save(project);

        // Step 3: Generate Film OR Episodes based on project type
        if ("FILM".equals(project.getProjectType())) {
            // Generate film using FilmService
            Film film = filmService.generateFilmWithScenes(project);
            project.setFilms(film);

            // CRITICAL: Explicitly save each scene to persist many-to-many relationships
            if (film.getScenes() != null) {
                for (Scene scene : film.getScenes()) {
                    // Make sure the scene has proper character associations
                    if (scene.getCharacters() != null && !scene.getCharacters().isEmpty()) {
                        // Save the scene - this will trigger the cascade to persist relationships
                        sceneRepository.save(scene);
                    }
                }
            }
        } else {
            // For series, determine episode count and generate episodes using EpisodeService
            int episodeCount = project.getEpisodeCount();
            Set<Episode> episodes = new HashSet<>();

            for (int i = 1; i <= episodeCount; i++) {
                Episode episode = episodeService.generateEpisodeWithScenes(project, i);
                episodes.add(episode);

                // CRITICAL: Explicitly save each scene to persist many-to-many relationships
                if (episode.getScenes() != null) {
                    for (Scene scene : episode.getScenes()) {
                        // Make sure the scene has proper character associations
                        if (scene.getCharacters() != null && !scene.getCharacters().isEmpty()) {
                            // Save the scene - this will trigger the cascade to persist relationships
                            sceneRepository.save(scene);
                        }
                    }
                }
            }

            project.setEpisodes(episodes);
        }

        // Step 4: Generate casting recommendations using CastingService
        Set<CastingRecommendation> casting = castingService.generateCasting(project);
        project.setCastingRecommendations(casting);

        // Update user AI usage
        user.setUseAI(user.getUseAI()-1);
        userRepository.save(user);

        // Save final project with all relationships
        return projectRepository.save(project);
    }
}