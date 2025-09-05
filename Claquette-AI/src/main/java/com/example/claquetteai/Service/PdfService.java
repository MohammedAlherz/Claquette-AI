package com.example.claquetteai.Service;

import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Model.User;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;

    /**
     * Generic HTML to PDF converter using OpenHTMLToPDF
     */
    public byte[] generatePdf(String templateName, Map<String, Object> data) {
        try {
            Context context = new Context();
            if (data != null) {
                context.setVariables(data);
            }

            String html = templateEngine.process(templateName, context);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(baos);
                builder.run();
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    /**
     * Generate invoice PDF for Claquette AI subscription
     */
    public byte[] generateInvoicePdf(CompanySubscription subscription, Payment payment, User user) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("subscriptionId", "CLA-" + subscription.getId());
        vars.put("userName", user.getFullName() != null ? user.getFullName() : "Valued Customer");
        vars.put("userEmail", user.getEmail());
        vars.put("serviceName", "Claquette AI Platform");
        vars.put("planName", determinePlanName(subscription));
        vars.put("period", determinePeriod(subscription));
        vars.put("priceFormatted", formatMoney(payment.getAmount()));
        vars.put("totalFormatted", formatMoney(payment.getAmount()));
        vars.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        vars.put("paymentMethod", "Credit Card");
        vars.put("status", "PAID");

        return generatePdf("invoice-template", vars);
    }

    private String determinePlanName(CompanySubscription subscription) {
        if ("ADVANCED".equals(subscription.getPlanType())) {
            if (subscription.getNextBillingDate() == null ||
                    subscription.getNextBillingDate().isAfter(LocalDateTime.now().plusDays(365))) {
                return "Advanced Lifetime Plan";
            }
            return "Advanced Monthly Plan";
        }
        return "Free Plan";
    }

    private String determinePeriod(CompanySubscription subscription) {
        if (subscription.getNextBillingDate() == null ||
                subscription.getNextBillingDate().isAfter(LocalDateTime.now().plusDays(365))) {
            return "One-time";
        }
        return "Monthly";
    }

    private String formatMoney(Double amount) {
        if (amount == null) return "0.00 SAR";
        return String.format("%.2f SAR", amount);
    }
}