package com.example.claquetteai.Controller;

import com.example.claquetteai.DTO.CastingContactDTOIN;
import com.example.claquetteai.Service.CastingInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cast-info")
@RequiredArgsConstructor
public class CastingInfoController {

    private final CastingInfoService castingInfoService;


    @GetMapping("/{userId}/cast/{castingRecommendationId}")
    public ResponseEntity<?> infoCharacter(@PathVariable Integer userId,@PathVariable Integer castingRecommendationId){
        return ResponseEntity.ok(castingInfoService.showInfo(userId,castingRecommendationId));
    }

    @GetMapping("/{userId}/contact/{castingRecommendationId}")
    public ResponseEntity<?> contactCharacter(@PathVariable Integer userId, @PathVariable Integer castingRecommendationId,@RequestBody CastingContactDTOIN castingContactDTOIN){
        castingInfoService.contactCast(userId,castingRecommendationId, castingContactDTOIN);
        return ResponseEntity.ok("message sent successfully");
    }
}
