package com.example.claquetteai.Controller;

import com.example.claquetteai.DTO.CastingContactDTOIN;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.CastingInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cast-info")
@RequiredArgsConstructor
public class CastingInfoController {

    private final CastingInfoService castingInfoService;

    // Hussam
    @GetMapping("/cast/{castingRecommendationId}")
    public ResponseEntity<?> infoCharacter(@AuthenticationPrincipal User user,
                                           @PathVariable Integer castingRecommendationId) {
        return ResponseEntity.ok(castingInfoService.showInfo(user.getId(), castingRecommendationId));
    }

    // Hussam
    @PostMapping("/contact/{castingRecommendationId}")
    public ResponseEntity<?> contactCharacter(@AuthenticationPrincipal User user,
                                              @PathVariable Integer castingRecommendationId,
                                              @RequestBody CastingContactDTOIN castingContactDTOIN) {
        castingInfoService.contactCast(user.getId(), castingRecommendationId, castingContactDTOIN);
        return ResponseEntity.ok("Message sent successfully");
    }


}