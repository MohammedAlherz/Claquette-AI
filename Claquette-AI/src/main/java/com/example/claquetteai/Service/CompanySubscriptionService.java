package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CompanySubscriptionDTOIN;
import com.example.claquetteai.DTO.CompanySubscriptionDTOOUT;
import com.example.claquetteai.DTO.HistorySubscription;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.CompanySubscriptionRepository;
import com.example.claquetteai.Repository.PaymentRepository;
import com.example.claquetteai.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanySubscriptionService {

    private final CompanySubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    private static final Double ADVANCED_PRICE = 1999.99;

    // Existing methods remain the same...
    public List<CompanySubscriptionDTOOUT> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(this::mapToDTOOUT)
                .collect(Collectors.toList());
    }

    public ResponseEntity<Map<String, String>> addSubscription(Integer companyId, String planType, Payment payment) {
        Company company = companyRepository.findCompanyById(companyId);
        if (company == null) {
            throw new ApiException("Company not found with id " + companyId);
        }

        CompanySubscription activeSub = company.getActiveSubscription();
        if (activeSub != null && "ACTIVE".equalsIgnoreCase(activeSub.getStatus())) {
            throw new ApiException("Company already has an active subscription");
        }

        CompanySubscription subscription = new CompanySubscription();
        subscription.setCompany(company);
        subscription.setPlanType(planType);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));

        if ("FREE".equalsIgnoreCase(planType)) {
            subscription.setMonthlyPrice(0.0);
            subscription.setStatus("FREE_PLAN");
            company.setIsSubscribed(false);
        } else if ("ADVANCED".equalsIgnoreCase(planType)) {
            subscription.setMonthlyPrice(ADVANCED_PRICE);
            subscription.setStatus("PENDING");
            company.setIsSubscribed(true);
        } else {
            throw new ApiException("Invalid plan type");
        }

        subscriptionRepository.save(subscription);
        companyRepository.save(company);
        return paymentService.processPayment(payment, subscription.getId());
    }

    @Transactional
    public ResponseEntity<Map<String, String>> renewSubscription(Integer companyId, Payment newPaymentData) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ApiException("Company not found with id " + companyId));

        CompanySubscription subscription = subscriptionRepository.findById(companyId)
                .orElseThrow(() -> new ApiException("Subscription not found"));

        // Verify ownership
        if (!subscription.getCompany().getId().equals(companyId)) {
            throw new ApiException("Subscription does not belong to this company");
        }

        // Check if subscription is expired
        if (!"EXPIRED".equals(subscription.getStatus())) {
            throw new ApiException("Subscription is still active and not eligible for renewal yet");
        }

        // Update subscription
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setStatus("PENDING");
        subscription.setMonthlyPrice(ADVANCED_PRICE);
        company.setIsSubscribed(true);

        // Find existing payment and update it
        Payment existingPayment = paymentRepository.findPaymentByCompanySubscription(subscription);
        if (existingPayment != null) {
            // Update existing payment with new card details
            existingPayment.setName(newPaymentData.getName());
            existingPayment.setNumber(newPaymentData.getNumber());
            existingPayment.setCvc(newPaymentData.getCvc());
            existingPayment.setMonth(newPaymentData.getMonth());
            existingPayment.setYear(newPaymentData.getYear());
            existingPayment.setAmount(ADVANCED_PRICE);
            existingPayment.setPaymentDate(LocalDateTime.now());
            existingPayment.setStatus("PENDING"); // Reset status for renewal

            paymentRepository.save(existingPayment);
        }

        companyRepository.save(company);
        subscriptionRepository.save(subscription);

        return paymentService.processPayment(existingPayment, subscription.getId());
    }

    // Activate cancelled subscription
    @Transactional
    public void activateSubscription(Integer userId, Integer subscriptionId) {
        User user = userRepository.findUserById(userId);
        CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);

        if (user == null) {
            throw new ApiException("User not found");
        }

        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }

        if (!subscription.getCompany().getUser().equals(user)) {
            throw new ApiException("Not Authorized");
        }

        // Check if subscription is cancelled (can only activate cancelled subscriptions)
        if (!"CANCELLED".equals(subscription.getStatus())) {
            throw new ApiException("Subscription is not cancelled. Only cancelled subscriptions can be activated.");
        }

        // Check if subscription hasn't expired
        if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Subscription has expired. Please renew instead of activating.");
        }

        // Activate the subscription
        if ("FREE".equals(subscription.getPlanType())) {
            subscription.setStatus("FREE_PLAN");
            subscription.getCompany().setIsSubscribed(false);
        } else {
            subscription.setStatus("ACTIVE");
            subscription.getCompany().setIsSubscribed(true);
        }

        subscriptionRepository.save(subscription);
        companyRepository.save(subscription.getCompany());
    }

    public void updateSubscriptionStatus(Integer userId, Integer subscriptionId, String status) {
        User user = userRepository.findUserById(userId);
        CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (!subscription.getCompany().getUser().equals(user)) {
            throw new ApiException("Not Authorized");
        }

        subscription.setStatus(status.toUpperCase());
        subscriptionRepository.save(subscription);
    }


    public void cancelSubscription(Integer userId, Integer subscriptionId) {
        User user = userRepository.findUserById(userId);
        CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (!subscription.getCompany().getUser().equals(user)) {
            throw new ApiException("Not Authorized");
        }
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }

        subscription.setStatus("CANCELLED");
        subscriptionRepository.save(subscription);
    }


    @Scheduled(cron = "0 * * * * *")
    public void checkStatusSubscriptionExpired() {
        System.out.println("Checking for expired subscriptions...");

        List<CompanySubscription> subscriptions = subscriptionRepository.findAll();
        LocalDate today = LocalDate.now();

        for (CompanySubscription subscription : subscriptions) {
            if (subscription.getNextBillingDate() == null || !"ACTIVE".equals(subscription.getStatus())) {
                continue;
            }

            if ("FREE".equals(subscription.getPlanType())) {
                continue;
            }

            LocalDate nextBillingDate = subscription.getNextBillingDate().toLocalDate();

            if (!nextBillingDate.isAfter(today)) {
                subscription.setStatus("EXPIRED");
                subscription.getCompany().setIsSubscribed(false);
                subscriptionRepository.save(subscription);
            }
        }
    }

    private CompanySubscriptionDTOOUT mapToDTOOUT(CompanySubscription subscription) {
        return new CompanySubscriptionDTOOUT(
                subscription.getPlanType(),
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getNextBillingDate(),
                subscription.getMonthlyPrice()
        );
    }


    public List<HistorySubscription> historyOfSubscription(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        List<CompanySubscription> companySubscriptions = subscriptionRepository.findCompanySubscriptionsByCompany_User(user);
        List<HistorySubscription> historySubscriptions = new ArrayList<>();
        for (CompanySubscription c : companySubscriptions) {
            Payment payment = paymentRepository.findPaymentByCompanySubscription(c);
            if (payment == null) {
                throw new ApiException("payment not found");
            }
            HistorySubscription h = new HistorySubscription();
            h.setPrice(c.getMonthlyPrice());
            h.setPaidAt(c.getStartDate());
            h.setIsPaid(payment.getStatus());
            historySubscriptions.add(h);
        }
        return historySubscriptions;
    }

    public Map<String, Object> getSubscriptionDashboard(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        Company company = companyRepository.findCompanyByUser(user);
        if (company == null) {
            throw new ApiException("Company not found for user");
        }

        Map<String, Object> dashboard = new HashMap<>();

        // Current subscription info
        CompanySubscription activeSubscription = company.getActiveSubscription();
        if (activeSubscription != null) {
            Map<String, Object> currentPlan = new HashMap<>();
            currentPlan.put("planType", activeSubscription.getPlanType());
            currentPlan.put("status", activeSubscription.getStatus());
            currentPlan.put("monthlyPrice", activeSubscription.getMonthlyPrice());
            currentPlan.put("startDate", activeSubscription.getStartDate());
            currentPlan.put("endDate", activeSubscription.getEndDate());
            currentPlan.put("nextBillingDate", activeSubscription.getNextBillingDate());
            dashboard.put("currentSubscription", currentPlan);
        } else {
            dashboard.put("currentSubscription", null);
        }

        // Subscription history
        List<HistorySubscription> history = historyOfSubscription(userId);
        dashboard.put("subscriptionHistory", history);

        // Payment statistics
        List<CompanySubscription> allSubscriptions = subscriptionRepository.findCompanySubscriptionsByCompany_User(user);
        Double totalSpent = allSubscriptions.stream()
                .filter(sub -> !"FREE".equals(sub.getPlanType()))
                .mapToDouble(CompanySubscription::getMonthlyPrice)
                .sum();

        Map<String, Object> paymentStats = new HashMap<>();
        paymentStats.put("totalAmountSpent", totalSpent);
        paymentStats.put("totalPayments", allSubscriptions.size());
        paymentStats.put("activePayments", allSubscriptions.stream()
                .filter(sub -> "ACTIVE".equals(sub.getStatus()))
                .count());

        dashboard.put("paymentStatistics", paymentStats);

        // Subscription alerts
        List<String> alerts = new ArrayList<>();
        if (activeSubscription != null) {
            if ("EXPIRED".equals(activeSubscription.getStatus())) {
                alerts.add("انتهت صلاحية اشتراكك. يرجى التجديد للاستمرار في استخدام الخدمات المتقدمة");
            } else if (activeSubscription.getNextBillingDate() != null) {
                LocalDate nextBilling = activeSubscription.getNextBillingDate().toLocalDate();
                LocalDate today = LocalDate.now();
                long daysUntilBilling = java.time.temporal.ChronoUnit.DAYS.between(today, nextBilling);

                if (daysUntilBilling <= 3 && daysUntilBilling >= 0) {
                    alerts.add("سيتم تجديد اشتراكك خلال " + daysUntilBilling + " أيام");
                }
            }
        }
        dashboard.put("alerts", alerts);

        return dashboard;
    }

}