package com.example.claquetteai.Controller;

import com.example.claquetteai.Service.CastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/casting-recommendation")
@RequiredArgsConstructor
public class CastingRecommendationController {
    private final CastingService castingService;
}
