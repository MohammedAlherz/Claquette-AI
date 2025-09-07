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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public void updateSubscriptionStatus(Integer userId,Integer subscriptionId, String status) {
        User user = userRepository.findUserById(userId);
        CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }
        if(user == null){
            throw new ApiException("User not found");
        }
        if(!subscription.getCompany().getUser().equals(user)){
            throw new ApiException("Not Authorized");
        }

        subscription.setStatus(status.toUpperCase());
        subscriptionRepository.save(subscription);
    }

    public void cancelSubscription(Integer userId,Integer subscriptionId) {
        User user = userRepository.findUserById(userId);
        CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }
        if(user == null){
            throw new ApiException("User not found");
        }
        if(!subscription.getCompany().getUser().equals(user)){
            throw new ApiException("Not Authorized");
        }        if (subscription == null) {
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


    public List<HistorySubscription> historyOfSubscription(Integer userId){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        List<CompanySubscription> companySubscriptions = subscriptionRepository.findCompanySubscriptionsByCompany_User(user);
        List<HistorySubscription> historySubscriptions = new ArrayList<>();
        for (CompanySubscription c : companySubscriptions){
            Payment payment = paymentRepository.findPaymentByCompanySubscription(c);
            if (payment == null){
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
}