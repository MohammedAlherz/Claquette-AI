package com.example.claquetteai.Controller;

import com.example.claquetteai.Service.CastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/casting-recommendation")
@RequiredArgsConstructor
public class CastingRecommendationController {
    private final CastingService castingService;

    @GetMapping("/{userId}/project/{projectId}")
    public ResponseEntity<?> recommendedCast(@PathVariable Integer userId,@PathVariable Integer projectId){
        return ResponseEntity.ok(castingService.castingRecommendations(userId,projectId));
    }

    @GetMapping("/{userId}/project/{projectId}/character/{charId}")
    public ResponseEntity<?> recommendedInfo(@PathVariable Integer userId, @PathVariable Integer projectId, @PathVariable Integer charId){
        return ResponseEntity.ok(castingService.personDetails(userId, projectId, charId));
    }
}
