package com.example.claquetteai.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDTOIN {

    // User fields
    @NotEmpty(message = "Full name cannot be null")
    @Size(min = 4, max = 20, message = "Full name should be between 4 and 20 characters")
    private String fullName;

    @NotEmpty(message = "Email should not be null")
    @Email(message = "Email should be valid email")
    private String email;

    @NotEmpty(message = "Password cannot be null")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    private String password;

    // Company fields
    @NotEmpty(message = "Company name cannot be null")
    @Size(min = 2, max = 200, message = "Company name should be between 2 and 200 characters")
    private String name;

    @NotEmpty(message = "Commercial registration number cannot be null")
    @Size(min = 10, max = 10, message = "Commercial registration number must be exactly 10 digits")
    private String commercialRegNo;
}