package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.PaymentDTOIN;
import com.example.claquetteai.DTO.PaymentDTOOUT;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/card/{subscriptionId}")
    public ResponseEntity<Map<String, String>> processPayment(
            @PathVariable Integer subscriptionId,
            @RequestBody @Valid Payment paymentRequest
    ) {
        return paymentService.processPayment(paymentRequest, subscriptionId);
    }

    @PostMapping("/{userId}/sub/{subscriptionId}")
    public ResponseEntity<?> processStatus(@PathVariable Integer userId,@PathVariable Integer subscriptionId){
        String response = paymentService.subscribePaymentStatus(userId, subscriptionId);
        return ResponseEntity.ok(new ApiResponse(response));
    }

    @GetMapping("/payments/confirm/{subscriptionId}/transaction/{transactionId}")
    public ResponseEntity<String> confirmPayment(@PathVariable Integer subscriptionId, @PathVariable String transactionId) throws JsonProcessingException {
        String result = paymentService.updateAndConfirmPayment(subscriptionId, transactionId);
        return ResponseEntity.ok(result);
    }



}
