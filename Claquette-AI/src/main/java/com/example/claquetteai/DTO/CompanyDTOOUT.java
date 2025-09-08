package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDTOOUT {


    // User fields
    private String fullName;
    private String email;

    // Company fields
    private String name;
    private String commercialRegNo;

}