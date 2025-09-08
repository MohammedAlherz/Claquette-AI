package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTOOUT {
    private Integer id;
    private double amount;
    private String currency;
    private String description;
    private String status;
    private String redirectUrl;
}

