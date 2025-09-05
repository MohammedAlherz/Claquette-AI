package com.example.claquetteai.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTOIN {

    private Integer id;
    private String name;
    private String number;
    private String cvc;
    private String month;
    private String year;
    private Double amount;
    private String currency;
    private String description;
    private String callbackUrl;
}
