package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.AiInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai-interaction")
@RequiredArgsConstructor
public class AiInteractionController {

    private final AiInteractionService aiInteractionService;

    // Hussam
    @PostMapping("/project/{projectId}")
    public ResponseEntity<?> generateScreenplay(@AuthenticationPrincipal User user,
                                                @PathVariable Integer projectId) throws Exception {
        aiInteractionService.generateFullScreenplay(projectId, user.getId());
        return ResponseEntity.ok(new ApiResponse("Screenplay generated successfully"));
    }
}