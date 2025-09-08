package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.CastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/casting-recommendation")
@RequiredArgsConstructor
public class CastingRecommendationController {
    private final CastingService castingService;

    // Mohammed Alherz
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> recommendedCast(@AuthenticationPrincipal User user,
                                             @PathVariable Integer projectId) {
        return ResponseEntity.ok(castingService.castingRecommendations(user.getId(), projectId));
    }

    // Hussam
    @GetMapping("/project/{projectId}/character/{charId}")
    public ResponseEntity<?> recommendedInfo(@AuthenticationPrincipal User user,
                                             @PathVariable Integer projectId,
                                             @PathVariable Integer charId) {
        return ResponseEntity.ok(castingService.personDetails(user.getId(), projectId, charId));
    }

    // Add this to your CastingController
    // Hussam
    @PostMapping("/generate-casting/{projectId}")
    public ResponseEntity<?> generateCasting(@AuthenticationPrincipal User user,
                                             @PathVariable Integer projectId) throws Exception {
        castingService.generateCastingRecommendations(user.getId(), projectId);
        return ResponseEntity.ok(new ApiResponse("Casting recommendations generated successfully"));
    }
}