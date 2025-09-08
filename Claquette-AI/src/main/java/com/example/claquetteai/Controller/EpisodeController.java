package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.EpisodeDTOOUT;
import com.example.claquetteai.DTO.SceneDTOOUT;
import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/episode")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    // Generate episodes for a project
    // Hussam
    @PostMapping("/generate-episodes/{projectId}")
    public ResponseEntity<?> generateEpisodes(@AuthenticationPrincipal User user,
                                              @PathVariable Integer projectId) throws Exception {
        episodeService.generateEpisodes(user.getId(), projectId);
        return ResponseEntity.ok(new ApiResponse("Episodes generated successfully"));
    }
    // Hussam
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectEpisodes(@AuthenticationPrincipal User user,
                                                            @PathVariable Integer projectId) {
        List<Episode> episodes = episodeService.getMyEpisodes(user.getId(), projectId);
        return ResponseEntity.ok(episodes);
    }
    // Get specific episode for a project
    // Mohammed Alherz
    @GetMapping("/project/{projectId}/episode/{episodeId}")
    public ResponseEntity<?> getProjectEpisodeDetail(@AuthenticationPrincipal User user, @PathVariable Integer projectId,
                                                           @PathVariable Integer episodeId) {
        EpisodeDTOOUT episode = episodeService.getProjectEpisode(user.getId(), projectId, episodeId);
        return ResponseEntity.ok(episode);
    }

    // Get scenes for a specific episode
    // Mohammed Alherz
    @GetMapping("/project/{projectId}/episode/{episodeId}/scenes")
    public ResponseEntity<Set<SceneDTOOUT>> getEpisodeScenes(@AuthenticationPrincipal User user,
                                                             @PathVariable Integer projectId,
                                                             @PathVariable Integer episodeId) {
        Set<SceneDTOOUT> scenes = episodeService.getEpisodeScenes(user.getId(), projectId, episodeId);
        return ResponseEntity.ok(scenes);
    }

}