package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilmCharactersDTOOUT {
    private String name;
    private Integer age;
    private String roleInStory;
    private String personalityTraits;
    private String background;
    private String characterArc;
}