package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDTOOUT {
    private Integer episodeNumber;
    private String title;
    private String summary;
}