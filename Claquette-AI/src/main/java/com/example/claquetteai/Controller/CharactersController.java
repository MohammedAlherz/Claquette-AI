package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharactersController {

    private final CharacterService characterService;

    @GetMapping("/{userId}/character-count")
    public ResponseEntity<?> characterCount(@PathVariable Integer userId){
        return ResponseEntity.ok(characterService.charactersCount(userId));
    }

    @PostMapping("/{userId}/generate-characters/{projectId}")
    public ResponseEntity<?> generateCharacters(@PathVariable Integer userId, @PathVariable Integer projectId) throws Exception {
        characterService.generateCharacterOnly(userId, projectId);
        return ResponseEntity.ok("character generated successfully");
    }

}
