package com.example.claquetteai.Controller;

import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scene")
@RequiredArgsConstructor
public class SceneController {
    private final SceneService sceneService;

    // Hussam
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> scenes(@AuthenticationPrincipal User user,
                                    @PathVariable Integer projectId) {
        return ResponseEntity.ok(sceneService.getScenes(user.getId(), projectId));
    }
    // Hussam
    @GetMapping("/project/{projectId}/characters-count")
    public ResponseEntity<?> charactersCount(@AuthenticationPrincipal User user,
                                             @PathVariable Integer projectId) {
        return ResponseEntity.ok(sceneService.characterScene(user.getId(), projectId));
    }
}