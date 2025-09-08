package com.example.claquetteai.Controller;

import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/episode")
@RequiredArgsConstructor
public class EpisodeController {
    private final EpisodeService episodeService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectEpisodes(@AuthenticationPrincipal User user,
                                                @PathVariable Integer projectId) {
        return ResponseEntity.ok(episodeService.getMyEpisodes(user.getId(), projectId));
    }

    @PostMapping("/{userId}/generate-episodes/{projectId}")
    public ResponseEntity<?> generateEpisodesAI(@PathVariable Integer userId,@PathVariable Integer projectId) throws Exception {
        episodeService.generateEpisodes(userId, projectId);
        return ResponseEntity.ok("episode generated successfully");
    }
}
