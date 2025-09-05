package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CompanySubscriptionDTOIN;
import com.example.claquetteai.DTO.CompanySubscriptionDTOOUT;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.CompanySubscriptionRepository;
import com.example.claquetteai.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanySubscriptionService {



    private final CompanySubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final PaymentRepository paymentRepository;

    private static final BigDecimal ADVANCED_PRICE = new BigDecimal("99.99");

    // 🟢 Get all subscriptions
    public List<CompanySubscriptionDTOOUT> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream()
                .map(this::mapToDTOOUT)
                .collect(Collectors.toList());
    }

    // 🟢 Add new subscription
    public void addSubscription(Integer companyId, CompanySubscriptionDTOIN dto) {
        Company company = companyRepository.findCompanyById(companyId);
        if (company == null) {
            throw new ApiException("Company not found with id " + companyId);
        }

        if (company.getActiveSubscription() != null) {
            throw new ApiException("Company already has an active subscription");
        }

        CompanySubscription subscription = new CompanySubscription();
        subscription.setCompany(company);
        subscription.setPlanType(dto.getPlanType());
        subscription.setStartDate(dto.getStartDate());
        subscription.setEndDate(dto.getStartDate().plusMonths(1));

        if ("FREE".equalsIgnoreCase(dto.getPlanType())) {
            subscription.setMonthlyPrice(null);
            subscription.setStatus("ACTIVE");
        } else if ("ADVANCED".equalsIgnoreCase(dto.getPlanType())) {
            subscription.setMonthlyPrice(ADVANCED_PRICE);
            subscription.setStatus("PENDING"); // بانتظار الدفع
        } else {
            throw new ApiException("Invalid plan type: " + dto.getPlanType());
        }

        // حفظ الاشتراك
        subscriptionRepository.save(subscription);

        // ⚡ إذا الخطة ADVANCED → أنشئ Payment مربوط بالاشتراك
        if ("ADVANCED".equalsIgnoreCase(dto.getPlanType())) {
            Payment payment = new Payment();
            payment.setId(subscription.getId()); // نفس الـ id (OneToOne @MapsId)
            payment.setCompanySubscription(subscription);
            payment.setCurrency("SAR");
            payment.setAmount(ADVANCED_PRICE.doubleValue());
            payment.setDescription("Subscription Payment for Company " + company.getName());
            payment.setCallbackUrl("https://your-server.com/api/payments/callback");

            subscription.setPayment(payment);
            paymentRepository.save(payment);
        }
    }

    // 🟢 Update subscription status
    public void updateSubscriptionStatus(Integer subscriptionId, String status) {
        CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }

        subscription.setStatus(status.toUpperCase());
        subscriptionRepository.save(subscription);
    }

    // 🟢 Delete subscription
    public void deleteSubscription(Integer subscriptionId) {
        CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }
        subscriptionRepository.delete(subscription);
    }

    // 🟢 Mapper → Entity to DTOOUT
    private CompanySubscriptionDTOOUT mapToDTOOUT(CompanySubscription subscription) {
        return new CompanySubscriptionDTOOUT(
                subscription.getId(),
                subscription.getPlanType(),
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getNextBillingDate(),
                subscription.getMonthlyPrice()
        );
    }
}
