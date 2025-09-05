package com.example.claquetteai.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanySubscriptionDTOIN {

    @NotEmpty(message = "Plan type cannot be null")
    @Pattern(regexp = "FREE|ADVANCED", message = "Plan type must be: FREE or ADVANCED")
    private String planType;

    private LocalDateTime startDate = LocalDateTime.now();
}
