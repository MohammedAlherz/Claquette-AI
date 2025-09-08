
package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CompanyDTOIN;
import com.example.claquetteai.DTO.WatheqValidationResponse;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatheqService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final VerificationService verificationService;
    private final VerificationEmailService emailService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${watheq.api.key}")
    private String wathqApiKey;

    /**
     * Validate a commercial registration number with Watheq API
     */
    public void validateCommercialRegNo(CompanyDTOIN commercialRegNo) throws JsonProcessingException {

        if (commercialRegNo == null || commercialRegNo.getCommercialRegNo().isEmpty()) {
            throw new ApiException("Commercial registration number is required");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("apiKey", wathqApiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> req = new HttpEntity<>(headers);

        String url = "https://api.wathq.sa/commercial-registration/fullinfo/" + commercialRegNo.getCommercialRegNo();

        try {
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, req, String.class);
            JsonNode root = mapper.readTree(res.getBody());

            String status = root.path("status").path("name").asText();
            if (!"نشط".equalsIgnoreCase(status)) {
                throw new ApiException("Commercial registration is not Active: " + status);
            }

            // If validation is successful, proceed with user and company creation
            createUserAndCompany(commercialRegNo);

        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Watheq API Bad Request: {}", e.getMessage());

            // Parse the error response to get specific error details
            try {
                JsonNode errorResponse = mapper.readTree(e.getResponseBodyAsString());
                String errorCode = errorResponse.path("code").asText();
                String errorMessage = errorResponse.path("message").asText();

                if ("400.1.5".equals(errorCode)) {
                    throw new ApiException(
                            "Invalid registration number format."+
                                    "Please provide the correct national commercial registration number."
                    );
                }

                // Handle other specific error codes if needed
                throw new ApiException("Registration validation failed: " + errorMessage);

            } catch (JsonProcessingException jsonEx) {
                // If we can't parse the error response, provide a generic message
                throw new ApiException(
                        "Commercial registration number validation failed. Please ensure you're using a valid commercial registration national number (700 series)."
                );
            }

        } catch (HttpClientErrorException e) {
            log.error("Watheq API Error: {} - {}", e.getStatusCode(), e.getMessage());

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ApiException("Commercial registration number not found in Watheq database");
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ApiException("API authentication failed. Please contact support.");
            } else {
                throw new ApiException("Registration validation service is temporarily unavailable. Please try again later.");
            }

        } catch (Exception e) {
            log.error("Unexpected error during registration validation: {}", e.getMessage(), e);
            throw new ApiException("An unexpected error occurred during registration validation. Please try again.");
        }
    }

    /**
     * Create user and company after successful validation
     */
    private void createUserAndCompany(CompanyDTOIN commercialRegNo) {
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        String hasPassword = bCrypt.encode(commercialRegNo.getPassword());

        // Create User
        User user = new User();
        user.setFullName(commercialRegNo.getFullName());
        user.setEmail(commercialRegNo.getEmail());
        user.setPassword(hasPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActiveAccount(false); // not verified yet
        user.setRole("COMPANY");
        User savedUser = userRepository.save(user);

        // Create Company
        Company company = new Company();
        company.setName(commercialRegNo.getName());
        company.setCommercialRegNo(commercialRegNo.getCommercialRegNo());
        company.setUser(savedUser);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        companyRepository.save(company);

        // Send verification email
        String code = verificationService.generateCode(user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
    }

    /**
     * Validate the format of the commercial registration number
     * Saudi commercial registration numbers are typically 10 digits starting with 1 or 4
     */
    private boolean isValidRegistrationNumberFormat(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            return false;
        }

        // Remove any spaces or special characters
        String cleanRegNo = regNo.replaceAll("[^0-9]", "");

        // Check if it's 10 digits and starts with 1 or 4
        return cleanRegNo.matches("^[14]\\d{9}$");
    }

    /**
     * Mock response if validation is disabled
     */
    private WatheqValidationResponse createMockValidResponse(String regNo) {
        return WatheqValidationResponse.builder()
                .commercialRegNo(regNo)
                .valid(true)
                .active(true)
                .status("active")
                .statusNameAr("السجل التجاري قائم")
                .statusNameEn("Commercial registration is active")
                .message("✅ Mock validation - Watheq disabled")
                .source("MOCK")
                .validatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}