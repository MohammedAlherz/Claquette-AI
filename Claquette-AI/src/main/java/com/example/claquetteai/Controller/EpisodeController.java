package com.example.claquetteai.Controller;

import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}