package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CastingRecommendationDTOOUT {
    private String name;
    private Integer age;
    private Double matchScore;
    private String profile;
    private String characterName; // NEW: Character name from linked character
    private String reasoning; // NEW: Why this actor was recommended
}
