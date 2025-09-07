package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharactersController {

    private final CharacterService characterService;

    @GetMapping("/{userId}/character-count")
    public ResponseEntity<?> characterCount(@PathVariable Integer userId){
        return ResponseEntity.ok(characterService.charactersCount(userId));
    }



}
