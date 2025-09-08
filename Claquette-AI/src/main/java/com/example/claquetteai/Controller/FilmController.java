package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.FilmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/film")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    // Mohammed Alherz
    @PostMapping("/generate-film/{projectId}")
    public ResponseEntity<?> generateFilm(@AuthenticationPrincipal User user,
                                          @PathVariable Integer projectId) throws Exception {
        filmService.generateFilm(user.getId(), projectId);
        return ResponseEntity.ok(new ApiResponse("Film generated successfully"));
    }

    // Mohammed Alherz
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectFilm(@AuthenticationPrincipal User user,
                                            @PathVariable Integer projectId) {
        return ResponseEntity.ok(filmService.getProjectFilm(user.getId(), projectId));
    }

    // Mohammed Alherz
    @GetMapping("/project/{projectId}/scenes")
    public ResponseEntity<?> getFilmScenes(@AuthenticationPrincipal User user,
                                           @PathVariable Integer projectId) {
        return ResponseEntity.ok(filmService.getFilmScenes(user.getId(), projectId));
    }

    // Mohammed Alherz
    @GetMapping("/my-films")
    public ResponseEntity<?> getUserFilms(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(filmService.getUserFilms(user.getId()));
    }

}