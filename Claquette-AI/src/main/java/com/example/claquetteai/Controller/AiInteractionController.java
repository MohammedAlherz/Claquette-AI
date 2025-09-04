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
    public ResponseEntity<ApiResponse> generateScreenplay(
            @RequestParam("projectId") Integer projectId) throws Exception {

        aiInteractionService.generateFullScreenplay(projectId);
        return ResponseEntity.ok(new ApiResponse("Screenplay generated successfully"));
    }
}