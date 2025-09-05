package com.example.claquetteai.Controller;

import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/episode")
@RequiredArgsConstructor
public class EpisodeController {
    private final EpisodeService episodeService;


    @GetMapping("/{userId}/project-episodes/{projectId}")
    public ResponseEntity<List<Episode>> getProjectEpisodes(@PathVariable Integer userId,@PathVariable Integer projectId){
        return ResponseEntity.ok(episodeService.getMyEpisodes(userId,projectId));
    }
}
