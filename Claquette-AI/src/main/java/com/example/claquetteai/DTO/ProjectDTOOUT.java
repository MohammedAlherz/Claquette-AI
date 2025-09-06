package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTOOUT {

    private String title;

    private String description;

    private String projectType;

    private String genre;

    private Double budget;

    private String targetAudience;

    private String location;

    private String status;

    private LocalDateTime startProjectDate;

    private LocalDateTime endProjectDate;
}