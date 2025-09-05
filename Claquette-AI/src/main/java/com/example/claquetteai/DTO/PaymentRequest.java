package com.example.claquetteai.DTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull
    private Integer amount; // in halalas (e.g., 10000 = 100 SAR)

    @NotNull
    private String currency; // "SAR"

    private String description;
    private String callbackUrl; // optional
    private String sourceType; // "creditcard"
    private String cardName;
    private String cardNumber;
    private String cvc;
    private String month;
    private String year;
}
