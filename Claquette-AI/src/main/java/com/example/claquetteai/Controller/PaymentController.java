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

    @PostMapping("/{userId}/payments/confirm/{subscriptionId}/transaction/{transactionId}")
    public ResponseEntity<?> confirmPasyment(@PathVariable Integer subscriptionId, @PathVariable String transactionId,@PathVariable Integer userId) throws JsonProcessingException {
      paymentService.updateAndConfirmPayment(subscriptionId, transactionId, userId);
        return ResponseEntity.ok(new ApiResponse("Paid confirmed"));
    }



}
