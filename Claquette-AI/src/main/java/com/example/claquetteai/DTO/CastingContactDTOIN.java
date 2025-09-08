package com.example.claquetteai.DTO;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class CastingContactDTOIN {

    private String fullName;
    @Email(message = "email must be valid")
    private String email;
    private String phoneNumber;
    private String message;
}
