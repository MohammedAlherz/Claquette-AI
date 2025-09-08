package com.example.claquetteai.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class CastingContactDTOIN {

    @NotEmpty(message = "full name is required")
    private String fullName;
    @Email(message = "email must be valid")
    private String email;
    @NotEmpty(message = "phone number is required")
    @Pattern(regexp = "^05\\d{8}$")
    private String phoneNumber;
    @NotEmpty(message = "message is required")
    private String message;
}
