package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
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

    // Hussam
    @GetMapping("/character-count")
    public ResponseEntity<?> characterCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(characterService.charactersCount(user.getId()));
    }

    // Hussam
    @PostMapping("/generate-characters/{projectId}")
    public ResponseEntity<?> generateCharacters(@AuthenticationPrincipal User user,
                                                @PathVariable Integer projectId) throws Exception {
        characterService.generateCharacterOnly(user.getId(), projectId);
        return ResponseEntity.ok(new ApiResponse("Characters generated successfully"));
    }

    // Get characters for a specific project (with authorization check)
    // Mohammed Alherz
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectCharacters(@AuthenticationPrincipal User user,
                                                  @PathVariable Integer projectId) {
        return ResponseEntity.ok(characterService.getProjectCharacters(user.getId(), projectId));
    }
}