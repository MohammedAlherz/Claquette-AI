package com.example.claquetteai.Controller;

import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharactersController {

    private final CharacterService characterService;

    @GetMapping("/character-count")
    public ResponseEntity<?> characterCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(characterService.charactersCount(user.getId()));
    }

    @PostMapping("/generate-characters/{projectId}")
    public ResponseEntity<?> generateCharacters(@AuthenticationPrincipal User user,
                                                @PathVariable Integer projectId) throws Exception {
        characterService.generateCharacterOnly(user.getId(), projectId);
        return ResponseEntity.ok("Characters generated successfully");
    }
}