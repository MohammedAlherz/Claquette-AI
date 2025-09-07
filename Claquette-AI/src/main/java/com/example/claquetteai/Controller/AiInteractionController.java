package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Service.AiInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai-interaction")
@RequiredArgsConstructor
public class AiInteractionController {

    private final AiInteractionService aiInteractionService;

    @PostMapping("/{userId}/project/{projectId}")
    public ResponseEntity<?> generateScreenplay(
            @PathVariable("projectId") Integer projectId, @PathVariable Integer userId) throws Exception {

        aiInteractionService.generateFullScreenplay(projectId, userId);
        return ResponseEntity.ok(new ApiResponse("Screenplay generated successfully"));
    }
}