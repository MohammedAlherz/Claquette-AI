package com.example.claquetteai.Controller;

import com.example.claquetteai.DTO.PaymentDTOIN;
import com.example.claquetteai.DTO.PaymentDTOOUT;
import com.example.claquetteai.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/card/{subscriptionId}")
    public ResponseEntity<PaymentDTOOUT> processPayment(
            @PathVariable Integer subscriptionId,
            @RequestBody @Valid PaymentDTOIN paymentRequest
    ) {
        PaymentDTOOUT response = paymentService.processPayment(paymentRequest, subscriptionId);
        return ResponseEntity.ok(response);
    }

}
