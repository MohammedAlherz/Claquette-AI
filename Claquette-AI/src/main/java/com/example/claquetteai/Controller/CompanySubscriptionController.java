package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.CompanySubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class CompanySubscriptionController {

    private final CompanySubscriptionService subscriptionService;

    // ADMIN ONLY - Get all subscription plans
    //  Mohammed Shamhani
    @GetMapping("/get")
    public ResponseEntity<?> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    // COMPANY ONLY - Subscribe to a plan
    //  Mohammed Shamhani
    @PostMapping("/add/plan-type/{planType}")
    public ResponseEntity<?> addSubscription(@AuthenticationPrincipal User user,
                                             @PathVariable String planType,
                                             @RequestBody Payment payment) {
        return ResponseEntity.ok(subscriptionService.addSubscription(
                user.getCompany().getId(), planType, payment));
    }


    // COMPANY ONLY - Cancel own subscription
    // Mohammed Alherz
    @PutMapping("/cancel-subscription/{subscriptionId}")
    public ResponseEntity<?> cancelSubscription(@AuthenticationPrincipal User user,
                                                @PathVariable Integer subscriptionId) {
        subscriptionService.cancelSubscription(user.getId(), subscriptionId);
        return ResponseEntity.ok(new ApiResponse("Subscription has been cancelled successfully"));
    }

    // Mohammed Alherz
    @PutMapping("/activate-subscription/{subscriptionId}")
    public ResponseEntity<?> activateSubscription(@AuthenticationPrincipal User user,
                                                  @PathVariable Integer subscriptionId) {
        subscriptionService.activateSubscription(user.getId(), subscriptionId);
        return ResponseEntity.ok(new ApiResponse("Subscription has been activated successfully"));
    }

    // COMPANY ONLY - Renew own subscription
    // Mohammed Alherz
    @PutMapping("/renew")
    public ResponseEntity<?> renewSubscription(@AuthenticationPrincipal User user,
                                               @RequestBody Payment payment) {
        return ResponseEntity.ok(subscriptionService.renewSubscription(
                user.getCompany().getId(), payment));
    }

    // COMPANY ONLY - Get own payment history
    // Hussam
    @GetMapping("/history-payment")
    public ResponseEntity<?> historySubscriptions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subscriptionService.historyOfSubscription(user.getId()));
    }

    // COMPANY ONLY - Get own subscription dashboard
    // Mohammed Alherz
    @GetMapping("/manage-subscription")
    public ResponseEntity<?> getSubscriptionDashboard(@AuthenticationPrincipal User user) {
        Map<String, Object> dashboard = subscriptionService.getSubscriptionDashboard(user.getId());
        return ResponseEntity.ok(dashboard);
    }
}