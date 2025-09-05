package com.example.claquetteai.Service;

import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanySubscriptionRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiExpirationAlertService {

    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final UserRepository userRepository;
    private final AiReminderMailService aiReminderMailService;

    /**
     * Check subscriptions and send alerts - runs every minute for testing
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendExpirationAlerts() {
        System.out.println("Checking for Claquette AI subscription expirations...");

        LocalDate today = LocalDate.now();
        List<CompanySubscription> activeSubscriptions = companySubscriptionRepository.findByStatus("ACTIVE");

        for (CompanySubscription subscription : activeSubscriptions) {
            // Skip FREE plans or subscriptions without billing dates
            if ("FREE".equals(subscription.getPlanType()) || subscription.getNextBillingDate() == null) {
                continue;
            }

            User user = subscription.getCompany().getUser();
            if (user == null) {
                continue;
            }

            LocalDate nextBillingDate = subscription.getNextBillingDate().toLocalDate();
            int daysDiff = (int) java.time.temporal.ChronoUnit.DAYS.between(today, nextBillingDate);

            System.out.println("Subscription ID: " + subscription.getId() + ", Days until expiration: " + daysDiff);

            // Check if subscription has expired (past due date or today)
            if (daysDiff <= 0) {
                expireSubscription(subscription);
                continue; // Skip alert sending for expired subscriptions
            }

            // Send alerts at 7 days, 2 days, and 1 day before expiration
            if (daysDiff == 7) {
                sendExpirationAlert(subscription, user, "normal", 7);
            } else if (daysDiff == 2) {
                sendExpirationAlert(subscription, user, "urgent", 2);
            } else if (daysDiff == 1) {
                sendExpirationAlert(subscription, user, "urgent", 1);
            }
        }

        System.out.println("Claquette AI expiration check completed.");
    }

    /**
     * Expire a subscription and update company status
     */
    private void expireSubscription(CompanySubscription subscription) {
        try {
            System.out.println("Expiring subscription ID: " + subscription.getId() + " for company: " + subscription.getCompany().getName());

            // Update subscription status to EXPIRED
            subscription.setStatus("EXPIRED");
            subscription.getCompany().setIsSubscribed(false);

            // Save changes
            companySubscriptionRepository.save(subscription);

            // Send expiration notification email
            User user = subscription.getCompany().getUser();
            if (user != null) {
                sendExpirationNotificationEmail(subscription, user);
            }

            System.out.println("Subscription expired successfully for company: " + subscription.getCompany().getName());

        } catch (Exception e) {
            System.err.println("Failed to expire subscription ID: " + subscription.getId() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send expiration notification email (subscription has already expired)
     */
    private void sendExpirationNotificationEmail(CompanySubscription subscription, User user) {
        try {
            String subject = "Your Claquette AI " + subscription.getPlanType() + " subscription has expired";
            String htmlBody = aiReminderMailService.buildExpirationNotificationEmail(
                    user.getFullName(),
                    subscription.getPlanType(),
                    subscription.getNextBillingDate().toLocalDate().toString(),
                    subscription.getMonthlyPrice()
            );

            aiReminderMailService.sendReminderEmail(user.getEmail(), subject, htmlBody);
            System.out.println("Expiration notification sent to: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send expiration notification: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Send expiration alert email to user
     */
    private void sendExpirationAlert(CompanySubscription subscription, User user, String alertType, int daysBeforeExpiry) {
        try {
            String subject = buildEmailSubject(alertType, subscription.getPlanType());
            String htmlBody = aiReminderMailService.buildExpirationEmailHtml(
                    alertType,
                    user.getFullName(),
                    subscription.getPlanType(),
                    subscription.getNextBillingDate().toLocalDate().toString(),
                    daysBeforeExpiry,
                    subscription.getMonthlyPrice()
            );

            aiReminderMailService.sendReminderEmail(user.getEmail(), subject, htmlBody);
            System.out.println("Claquette AI expiration alert sent to: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send Claquette AI expiration alert: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build email subject based on alert type
     */
    private String buildEmailSubject(String alertType, String planType) {
        if ("urgent".equals(alertType)) {
            return "URGENT: Your Claquette AI " + planType + " subscription expires in 2 days!";
        } else {
            return "Reminder: Your Claquette AI " + planType + " subscription expires in 7 days";
        }
    }


}