package com.example.claquetteai.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SceneDTOIN {
    @NotEmpty(message = "dialogue is required")
    private String dialogue;
}
