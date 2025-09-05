package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.PaymentDTOIN;
import com.example.claquetteai.DTO.PaymentDTOOUT;
import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Repository.CompanySubscriptionRepository;
import com.example.claquetteai.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PaymentService {


    @Value("${moyasar.api.key}")
    private String apiKey;

    private static final String MOYASAR_API_URL = "https://api.moyasar.com/v1/payments";

    private final PaymentRepository paymentRepository;
    private final CompanySubscriptionRepository subscriptionRepository;
    private final CompanySubscriptionService subscriptionService;
    public PaymentDTOOUT processPayment(PaymentDTOIN paymentRequest, Integer subscriptionId) {
        String callbackUrl = "https://your-server.com/api/payments/callback";

        String requestBody = String.format(
                "source[type]=creditcard&source[name]=%s&source[number]=%s&source[cvc]=%s" +
                        "&source[month]=%s&source[year]=%s&amount=%d&currency=%s&description=%s&callback_url=%s",
                paymentRequest.getName(),
                paymentRequest.getNumber(),
                paymentRequest.getCvc(),
                paymentRequest.getMonth(),
                paymentRequest.getYear(),
                (int) (paymentRequest.getAmount() * 100),
                paymentRequest.getCurrency(),
                paymentRequest.getDescription(),
                callbackUrl
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                MOYASAR_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            CompanySubscription subscription = subscriptionRepository.findCompanySubscriptionById(subscriptionId);
            if (subscription == null) {
                throw new ApiException("Subscription not found with id " + subscriptionId);
            }

            subscriptionService.updateSubscriptionStatus(subscription.getId(), "ACTIVE");

            Payment payment = new Payment();
            payment.setId(subscription.getId());
            payment.setName(paymentRequest.getName());
            payment.setNumber(paymentRequest.getNumber());
            payment.setCvc(paymentRequest.getCvc());
            payment.setMonth(paymentRequest.getMonth());
            payment.setYear(paymentRequest.getYear());
            payment.setAmount(paymentRequest.getAmount());
            payment.setCurrency(paymentRequest.getCurrency());
            payment.setDescription(paymentRequest.getDescription());
            payment.setCallbackUrl(callbackUrl);
            payment.setCompanySubscription(subscription);

            paymentRepository.save(payment);

            return new PaymentDTOOUT(
                    payment.getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getDescription(),
                    "SUCCESS",
                    "https://redirect.url/success"
            );
        }

        return new PaymentDTOOUT(
                null,
                paymentRequest.getAmount(),
                paymentRequest.getCurrency(),
                paymentRequest.getDescription(),
                "FAILED",
                null
        );
    }

}
