package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.CompanySubscriptionDTOIN;
import com.example.claquetteai.DTO.HistorySubscription;
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

    @PostMapping("/add/{companyId}")
    public ResponseEntity<?> addSubscription(@PathVariable Integer companyId,
                                             @RequestBody @Valid CompanySubscriptionDTOIN dto) {
        subscriptionService.addSubscription(companyId, dto);
        return ResponseEntity.ok(new ApiResponse("Subscription has been added successfully"));
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateSubscriptionStatus(@PathVariable Integer id,
                                                      @RequestParam String status) {
        subscriptionService.updateSubscriptionStatus(id, status);
        return ResponseEntity.ok(new ApiResponse("Subscription status has been updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSubscription(@PathVariable Integer id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.ok(new ApiResponse("Subscription has been deleted successfully"));
    }


    @GetMapping("/{userId}/history-payment")
    public ResponseEntity<List<HistorySubscription>> historySubscriptions(@PathVariable Integer userId){
        return ResponseEntity.ok(subscriptionService.historyOfSubscription(userId));
    }
}
