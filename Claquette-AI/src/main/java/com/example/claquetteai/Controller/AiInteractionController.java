package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Service.AiInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai-interaction")
@RequiredArgsConstructor
public class AiInteractionController {

    private final AiInteractionService aiInteractionService;

    @PostMapping
    public ResponseEntity<ApiResponse> generateFilm(@RequestParam("description") String description,@RequestParam("episodes") Integer episodes) throws Exception {
        aiInteractionService.generateFullScreenplay(description, episodes);
        return ResponseEntity.ok(new ApiResponse("project generated successfully"));
    }
}
