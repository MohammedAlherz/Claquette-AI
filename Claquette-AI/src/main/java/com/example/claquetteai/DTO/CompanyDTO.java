package com.example.claquetteai.DTO;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CompanyDTO {

    @Id
    private Integer userId;

    @NotEmpty(message = "Company name cannot be null")
    @Size(min = 2, max = 200, message = "Company name should be between 2 and 200 characters")
    private String name;

    @NotEmpty(message = "Commercial registration number cannot be null")
    @Size(max = 50, message = "Commercial registration number should not exceed 50 characters")
    private String commercialRegNo;
}
