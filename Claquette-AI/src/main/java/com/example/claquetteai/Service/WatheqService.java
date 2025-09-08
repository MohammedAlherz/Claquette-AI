package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.WatheqValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatheqService {

    private final WebClient.Builder webClientBuilder;

    @Value("${watheq.api.base-url:https://api.wathq.sa/sandbox/commercial-registration}")
    private String baseUrl;

    @Value("${watheq.api.key}")
    private String apiKey;

    @Value("${watheq.validation.enabled:true}")
    private boolean validationEnabled;

    /**
     * Validate a commercial registration number with Watheq API
     */
    public WatheqValidationResponse validateCommercialRegNo(String commercialRegNo) {
        if (!validationEnabled) {
            log.warn("⚠️ Watheq validation disabled → returning mock response");
            return createMockValidResponse(commercialRegNo);
        }

        if (commercialRegNo == null || commercialRegNo.trim().isEmpty()) {
            throw new ApiException("Commercial registration number is required");
        }

        try {
            WebClient client = webClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("THIQAH-API-ApiMsgRef", UUID.randomUUID().toString())
                    .defaultHeader("THIQAH-API-ClientMsgRef", "ClaquetteAI-" + System.currentTimeMillis())
                    .defaultHeader("apiKey", apiKey)
                    .build();

            // ✅ استقبل الـ Response الخام من API
            WatheqValidationResponse rawResponse = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/status/{id}")
                            .queryParam("language", "ar")
                            .build(commercialRegNo))
                    .retrieve()
                    .bodyToMono(WatheqValidationResponse.class)
                    .doOnError(e -> log.error("❌ Watheq API error: {}", e.getMessage()))
                    .onErrorResume(e -> Mono.error(new ApiException("Watheq service unavailable")))
                    .block();

            // ✅ Mapping للـ Response الخاص بنا
            if (rawResponse != null) {
                boolean isActive = "نشط".equalsIgnoreCase(rawResponse.getStatus()) ||
                        "نشط".equalsIgnoreCase(rawResponse.getStatusNameAr());

                return WatheqValidationResponse.builder()
                        .commercialRegNo(commercialRegNo)
                        .id(rawResponse.getId())
                        .status(rawResponse.getStatus() != null ? rawResponse.getStatus() : rawResponse.getName())
                        .statusNameAr(rawResponse.getStatusNameAr() != null ? rawResponse.getStatusNameAr() : rawResponse.getName())
                        .statusNameEn(isActive ? "Active" : "Inactive")
                        .valid(isActive)
                        .active(isActive)
                        .message(isActive ? "CR is active" : "CR is inactive")
                        .source("WATHIQ")
                        .validatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build();
            } else {
                throw new ApiException("Watheq response is empty");
            }

        } catch (Exception e) {
            log.error("❌ Unexpected error while validating CRN {}: {}", commercialRegNo, e.getMessage());
            throw new ApiException("Failed to validate commercial registration: " + e.getMessage());
        }
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
