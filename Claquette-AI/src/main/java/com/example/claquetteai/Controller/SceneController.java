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
    public ResponseEntity<List<SceneDTOOUT>> scenes(@PathVariable Integer userId, @PathVariable Integer projectId){
        return ResponseEntity.ok(sceneService.scenes(userId, projectId));
    }

    @GetMapping("/{userId}/project/{projectId}/characters-count")
    public ResponseEntity<ApiResponse> charactersCount(@PathVariable Integer userId, @PathVariable Integer projectId){
        return ResponseEntity.ok(new ApiResponse(sceneService.characterScene(userId, projectId).toString()));
    }

    @PutMapping("/{userId}/project/{projectId}/scene/{sceneId}")
    public ResponseEntity<ApiResponse> updateScene(@PathVariable Integer userId,@PathVariable Integer projectId,@PathVariable Integer sceneId,@RequestBody SceneDTOIN sceneDTOIN){
        sceneService.updateScene(userId, projectId, sceneId, sceneDTOIN);
        return ResponseEntity.ok(new ApiResponse("scene updated"));
    }
}
