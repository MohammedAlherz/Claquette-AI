package com.example.claquetteai.Controller;

import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/episode")
@RequiredArgsConstructor
public class EpisodeController {
    private final EpisodeService episodeService;


    @GetMapping("/{userId}/project-episodes/{projectId}")
    public ResponseEntity<?> getProjectEpisodes(@PathVariable Integer userId,@PathVariable Integer projectId){
        return ResponseEntity.ok(episodeService.getMyEpisodes(userId,projectId));
    }

    @PostMapping("/{userId}/generate-episodes/{projectId}")
    public ResponseEntity<?> generateEpisodesAI(@PathVariable Integer userId,@PathVariable Integer projectId) throws Exception {
        episodeService.generateEpisodes(userId, projectId);
        return ResponseEntity.ok("episode generated successfully");
    }
}
