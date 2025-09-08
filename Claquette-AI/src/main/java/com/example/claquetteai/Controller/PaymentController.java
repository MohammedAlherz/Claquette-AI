package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    //  Mohammed Shamhani
    @PostMapping("/confirm/{subscriptionId}/transaction/{transactionId}")
    public ResponseEntity<?> confirmPayment(@AuthenticationPrincipal User user,
                                            @PathVariable Integer subscriptionId,
                                            @PathVariable String transactionId) throws JsonProcessingException {
        paymentService.updateAndConfirmPayment(subscriptionId, transactionId, user.getId());
        return ResponseEntity.ok(new ApiResponse("Payment confirmed"));
    }
}