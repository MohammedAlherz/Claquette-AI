
package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilmSceneDTOOUT {
    private Integer sceneNumber;
    private String setting;
    private String actions;
    private String dialogue;
    private String departmentNotes;
}