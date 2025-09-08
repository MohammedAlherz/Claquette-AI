package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.PaymentDTOIN;
import com.example.claquetteai.DTO.PaymentDTOOUT;
import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanySubscriptionRepository;
import com.example.claquetteai.Repository.PaymentRepository;
import com.example.claquetteai.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final UserRepository userRepository;
    private final PdfMailService pdfMailService;

    @Value("${moyasar.api.key}")
    private String apiKey;

    private static final String MOYASAR_API_URL = "https://api.moyasar.com/v1/payments/";

    private final PaymentRepository paymentRepository;

    public ResponseEntity<Map<String, String>> processPayment(Payment paymentRequest, Integer subscriptionId) {
        CompanySubscription companySubscription = companySubscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (companySubscription == null) {
            throw new ApiException("Subscription not found");
        }


        String callbackUrl = "https://dashboard.moyasar.com/entities/f0144c0a-b82c-4fdf-aefb-6c7be5b87cb7/payments"; // Replace with your real callback
        paymentRequest.setName(companySubscription.getCompany().getUser().getFullName());
        paymentRequest.setAmount(companySubscription.getMonthlyPrice());
        paymentRequest.setCurrency("SAR");

        if(paymentRequest.getAmount() < companySubscription.getMonthlyPrice()){
            throw new ApiException("insufficient amount!!!");
        }

        String requestBody = String.format(
                "source[type]=card&source[name]=%s&source[number]=%s&source[cvc]=%s" +
                        "&source[month]=%s&source[year]=%s&amount=%d&currency=%s&callback_url=%s",
                paymentRequest.getName(),
                paymentRequest.getNumber(),
                paymentRequest.getCvc(),
                paymentRequest.getMonth(),
                paymentRequest.getYear(),
                (int) (paymentRequest.getAmount() * 100), // convert SAR to halalah
                paymentRequest.getCurrency(),
                callbackUrl
        );

        // 🔐 Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // ⛓ Wrap payload and headers
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                MOYASAR_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String transactionId = jsonResponse.get("id").asText();
            String transactionUrl = jsonResponse.get("source").get("transaction_url").asText();

            paymentRequest.setTransactionId(transactionId);
            paymentRequest.setRedirectToCompletePayment(transactionUrl); // ✅ رابط الدفع
            paymentRequest.setCompanySubscription(companySubscription);
            paymentRequest.setPaymentDate(LocalDateTime.now());
            paymentRequest.setStatus(jsonResponse.get("status").asText()); // initiated
            paymentRepository.save(paymentRequest);

            // ✅ return both
            Map<String, String> result = new HashMap<>();
            result.put("transactionId", transactionId);
            result.put("transactionUrl", transactionUrl);

            return ResponseEntity.ok(result);

        } catch (JsonProcessingException e) {
            throw new ApiException("Error parsing JSON");
        }

    }


    public String subscribePaymentStatus(Integer userId, Integer subscriptionId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        CompanySubscription companySubscription = companySubscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (companySubscription == null) {
            throw new ApiException("subscription not found");
        }
        if (!companySubscription.getStatus().equalsIgnoreCase("PENDING")) {
            throw new ApiException("Subscription is already confirmed or invalid");
        }
        Payment payment = paymentRepository.findPaymentByCompanySubscription(companySubscription);
        if (payment == null) {
            throw new ApiException("Payment not found");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                MOYASAR_API_URL + payment.getTransactionId(),
                HttpMethod.GET,
                entity,
                String.class
        );

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            String paymentStatus = jsonResponse.get("status").asText();

            if (paymentStatus.equalsIgnoreCase("paid")) {
                companySubscription.setStatus("CONFIRMED");
                companySubscription.setPayment(payment);
                companySubscriptionRepository.save(companySubscription);

                user.getCompany().setIsSubscribed(true);
                userRepository.save(user);
            }

            return response.getBody();
        } catch (Exception e) {
            throw new ApiException("Failed to parse Moyasar response");
        }

    }

    public void updateAndConfirmPayment(Integer subscriptionId, String transactionId, Integer userId) throws JsonProcessingException {
        // 1. Fetch subscription
        CompanySubscription subscription = companySubscriptionRepository.findCompanySubscriptionById(subscriptionId);
        if (subscription == null) {
            throw new ApiException("Subscription not found with id " + subscriptionId);
        }

        // 2. Fetch payment
        Payment payment = paymentRepository.findPaymentByCompanySubscription(subscription);
        if (payment == null) {
            throw new ApiException("Payment not found for subscription " + subscriptionId);
        }

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }
        User user1 = userRepository.findUserByCompany_ActiveSubscription(subscription);
        if (!user1.equals(user)) {
            throw new ApiException("not authorised");
        }

        // 3. Decide which transactionId to use
        String txId = (transactionId != null && !transactionId.isEmpty())
                ? transactionId
                : payment.getTransactionId();

        // 4. Call Moyasar API
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                MOYASAR_API_URL + txId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.getBody());
        if (json.has("status")) {
            String status = json.get("status").asText();
            payment.setStatus(status);
            if ("paid".equalsIgnoreCase(status)) {
                payment.setPaymentDate(LocalDateTime.now());
            }
            paymentRepository.save(payment);

            if ("paid".equalsIgnoreCase(status)) {
                subscription.setStatus("ACTIVE");
                subscription.setPayment(payment);
                subscription.setNextBillingDate(LocalDateTime.now().plusDays(30));
                companySubscriptionRepository.save(subscription);

                subscription.getCompany().setIsSubscribed(true);
                userRepository.save(subscription.getCompany().getUser());

                // Get user for invoice - use the authenticated user
                User invoiceUser = userRepository.findUserByCompany_ActiveSubscription(subscription);
                System.out.println("Sending invoice to: " + invoiceUser.getEmail());

                // Send invoice email with PDF attachment
                try {
                    pdfMailService.generateAndSendInvoice(subscription, payment, invoiceUser);
                    System.out.println("Invoice email sent successfully to: " + invoiceUser.getEmail());
                } catch (Exception emailException) {
                    System.err.println("Failed to send invoice email: " + emailException.getMessage());
                    // Log the error but don't fail the payment process
                    emailException.printStackTrace();
                }
            }
        } else {
            String errorMessage = json.has("message") ? json.get("message").asText() : "Unknown error";
            throw new ApiException("Moyasar error: " + errorMessage);
        }
    }
}
