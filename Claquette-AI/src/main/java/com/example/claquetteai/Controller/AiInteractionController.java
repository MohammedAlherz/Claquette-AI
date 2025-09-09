package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.ProjectGenerationJob;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.AiInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-interaction")
@RequiredArgsConstructor
public class AiInteractionController {

    private final AiInteractionService aiInteractionService;

    // Hussam
    @PostMapping("/project/{projectId}")
    public ResponseEntity<?> generateScreenplay(@AuthenticationPrincipal User user,
                                                @PathVariable Integer projectId) {
        String jobId = aiInteractionService.startScreenplayGeneration(projectId, user.getId());
        return ResponseEntity.accepted().body(Map.of(
                "message", "Generation started",
                "jobId", jobId
        ));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> job(@PathVariable String jobId) {
        ProjectGenerationJob j = aiInteractionService.getJobStatus(jobId);
        return ResponseEntity.ok(Map.of(
                "jobId", j.getId(), "status", j.getStatus(),
                "step", j.getCurrentStep(), "progress", j.getProgress(),
                "info", j.getInfo(), "error", j.getErrorMessage()
        ));
    }
}