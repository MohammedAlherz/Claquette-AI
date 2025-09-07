package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.SceneDTOIN;
import com.example.claquetteai.DTO.SceneDTOOUT;
import com.example.claquetteai.Service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scene")
@RequiredArgsConstructor
public class SceneController {
    private final SceneService sceneService;

    @GetMapping("/{userId}/project/{projectId}")
    public ResponseEntity<?> scenes(@PathVariable Integer userId, @PathVariable Integer projectId){
        return ResponseEntity.ok(sceneService.getScenes(userId, projectId));
    }

    @GetMapping("/{userId}/project/{projectId}/characters-count")
    public ResponseEntity<?> charactersCount(@PathVariable Integer userId, @PathVariable Integer projectId){
        return ResponseEntity.ok(sceneService.characterScene(userId, projectId));
    }

}
