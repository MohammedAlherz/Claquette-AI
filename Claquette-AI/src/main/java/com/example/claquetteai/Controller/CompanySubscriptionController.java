package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.CompanySubscriptionDTOIN;
import com.example.claquetteai.DTO.HistorySubscription;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Service.CompanySubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class CompanySubscriptionController {

    private final CompanySubscriptionService subscriptionService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    @PostMapping("/add/{companyId}/plan-type/{planType}")
    public ResponseEntity<?> addSubscription(@PathVariable Integer companyId,
                                             @PathVariable String planType, @RequestBody Payment payment) {
        return ResponseEntity.ok(subscriptionService.addSubscription(companyId, planType,payment));
    }

    @PutMapping("/update-status/{userId}/{subscriptionId}")
    public ResponseEntity<?> updateSubscriptionStatus(@PathVariable Integer userId,@PathVariable Integer subscriptionId,
                                                      @RequestParam String status) {
        subscriptionService.updateSubscriptionStatus(userId,subscriptionId, status);
        return ResponseEntity.ok(new ApiResponse("Subscription status has been updated successfully"));
    }

    @PutMapping("/cancel-subscription/{userId}/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(@PathVariable Integer userId,@PathVariable Integer subscriptionId) {
        subscriptionService.cancelSubscription(userId,subscriptionId);
        return ResponseEntity.ok(new ApiResponse("Subscription has Cancelled successfully"));
    }


    @GetMapping("/{userId}/history-payment")
    public ResponseEntity<?> historySubscriptions(@PathVariable Integer userId){
        return ResponseEntity.ok(subscriptionService.historyOfSubscription(userId));
    }
}
