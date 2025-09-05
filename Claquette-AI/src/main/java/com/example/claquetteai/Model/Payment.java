package com.example.claquetteai.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Payment {
    @Id
    private Integer id;

    private String name;
    private String number;
    private String cvc;
    private String month;
    private String year;
    private double amount;
    private String currency;
    private String description;
    private String callbackUrl;

    @JsonIgnore
    private String paymentUserId;

    @JsonIgnore
    private String redirectToCompletePayment;

    // CORRECT: One-to-One relationship
    @OneToOne
    @MapsId
    @JsonIgnore
    private CompanySubscription companySubscription;
}
