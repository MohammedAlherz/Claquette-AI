package com.example.claquetteai.Service;

import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.Payment;
import com.example.claquetteai.Model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfMailService {

    private final JavaMailSender mailSender;
    private final PdfService pdfService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendHtmlEmailWithAttachment(
            String to,
            String subject,
            String htmlBody,
            String attachmentFilename,
            byte[] pdfBytes,
            @Nullable String... cc
    ) {
        try {
            System.out.println("Sending email from: " + fromEmail + " to: " + to);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Claquette AI");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }

            helper.addAttachment(attachmentFilename, new ByteArrayResource(pdfBytes), "application/pdf");

            System.out.println("Sending email...");
            mailSender.send(mimeMessage);
            System.out.println("Email sent successfully to SMTP server");

        } catch (MessagingException ex) {
            System.err.println("MessagingException details: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to send email - MessagingException", ex);
        } catch (MailException ex) {
            System.err.println("MailException details: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to send email - MailException", ex);
        } catch (Exception ex) {
            System.err.println("Unexpected error: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to send email - Unexpected error", ex);
        }
    }

    /**
     * Generate and send invoice for successful payment
     */
    public void generateAndSendInvoice(CompanySubscription subscription, Payment payment, User user) {
        try {
            // Generate PDF using the improved PdfService
            byte[] pdfBytes = pdfService.generateInvoicePdf(subscription, payment, user);

            // Generate filename
            String filename = generateInvoiceFilename(subscription.getId());

            // Generate email content
            String emailSubject = "Claquette AI - Invoice #CLA-" + subscription.getId();
            String emailBody = generateEmailBody(user, subscription);

            // Send email with PDF attachment
            sendHtmlEmailWithAttachment(
                    user.getEmail(),
                    emailSubject,
                    emailBody,
                    filename,
                    pdfBytes
            );

            System.out.println("Invoice sent successfully to: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to generate and send invoice: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to avoid breaking the payment process
        }
    }

    private String generateInvoiceFilename(Integer subscriptionId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("Claquette_Invoice_CLA-%d_%s.pdf", subscriptionId, timestamp);
    }

    private String generateEmailBody(User user, CompanySubscription subscription) {
        String nextBilling = subscription.getNextBillingDate() != null ?
                subscription.getNextBillingDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "Lifetime Access";

        String userName = StringUtils.hasText(user.getFullName()) ? user.getFullName() : "Valued Customer";
        String planType = subscription.getPlanType().equals("ADVANCED") ? "Advanced Plan" : "Free Plan";

        return String.format(
                "<div style='font-family: \"Segoe UI\", Tahoma, Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8f9fa; padding: 20px; direction: rtl;'>" +
                        "<div style='background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 25px rgba(0,0,0,0.1);'>" +
                        "<div style='background: linear-gradient(135deg, #D4B06A, #B8965A); color: white; padding: 40px; text-align: center;'>" +
                        "<div style='display: flex; align-items: center; justify-content: center; margin-bottom: 15px;'>" +
                        "<div style='width: 50px; height: 50px; background: #2F5233; border: 2px solid white; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 20px; margin-left: 15px;'>🎭</div>" +
                        "<h1 style='margin: 0; font-size: 32px; font-weight: 700;'>كلاكيت AI</h1>" +
                        "</div>" +
                        "<p style='margin: 0; opacity: 0.9; font-size: 16px;'>منصة الذكاء الاصطناعي لإنتاج الأفلام والمسلسلات</p>" +
                        "</div>" +
                        "<div style='padding: 40px;'>" +
                        "<h2 style='color: #2F5233; margin-top: 0; font-size: 24px;'>🎬 مرحباً بك في كلاكيت AI!</h2>" +
                        "<p style='font-size: 18px; color: #333; margin-bottom: 20px;'>عزيزي %s،</p>" +
                        "<p style='color: #666; line-height: 1.8; font-size: 16px;'>نشكرك على اشتراكك في كلاكيت AI! تم تفعيل اشتراكك بنجاح ويمكنك الآن الوصول إلى جميع ميزات منصتنا المتقدمة لإنتاج الأفلام والمسلسلات بالذكاء الاصطناعي.</p>" +
                        "<div style='background: linear-gradient(145deg, #f8f9fa, #ffffff); padding: 25px; border-radius: 12px; border-right: 4px solid #D4B06A; margin: 30px 0; box-shadow: 0 2px 8px rgba(0,0,0,0.05);'>" +
                        "<h3 style='margin: 0 0 20px 0; color: #2F5233; font-size: 20px;'>📋 تفاصيل الاشتراك</h3>" +
                        "<table style='width: 100%%; border-collapse: collapse;'>" +
                        "<tr><td style='padding: 10px 0; color: #666; width: 40%%; font-weight: 600;'>رقم الاشتراك:</td><td style='color: #2F5233; font-weight: 700;'>CLA-%d</td></tr>" +
                        "<tr><td style='padding: 10px 0; color: #666; font-weight: 600;'>نوع الخطة:</td><td style='color: #2F5233; font-weight: 700;'>%s</td></tr>" +
                        "<tr><td style='padding: 10px 0; color: #666; font-weight: 600;'>الحالة:</td><td><span style='background: linear-gradient(45deg, #D4B06A, #B8965A); color: white; padding: 6px 16px; border-radius: 20px; font-size: 12px; font-weight: bold;'>✅ نشط</span></td></tr>" +
                        "<tr><td style='padding: 10px 0; color: #666; font-weight: 600;'>الفوترة التالية:</td><td style='color: #2F5233; font-weight: 700;'>%s</td></tr>" +
                        "</table>" +
                        "</div>" +
                        "<div style='background: linear-gradient(145deg, #e8f5e8, #f0f8f0); padding: 25px; border-radius: 12px; margin: 30px 0; border: 1px solid #D4B06A;'>" +
                        "<h4 style='margin: 0 0 12px 0; color: #2F5233; font-size: 18px;'>📎 الفاتورة مرفقة</h4>" +
                        "<p style='margin: 0; color: #2F5233; font-size: 15px;'>تم إرفاق فاتورتك بهذا البريد الإلكتروني لسجلاتك.</p>" +
                        "</div>" +
                        "<div style='background: linear-gradient(135deg, #2F5233, #1a2e1d); color: white; padding: 25px; border-radius: 12px; text-align: center; margin: 30px 0;'>" +
                        "<h4 style='margin: 0 0 12px 0; font-size: 20px;'>🚀 جاهز للبدء؟</h4>" +
                        "<p style='margin: 0 0 18px 0; opacity: 0.9; font-size: 16px;'>ادخل إلى لوحة التحكم وابدأ في إنشاء أفلام مذهلة بالذكاء الاصطناعي</p>" +
                        "</div>" +
                        "<p style='color: #666; line-height: 1.8; font-size: 15px;'>إذا كان لديك أي أسئلة أو تحتاج إلى مساعدة، فريق الدعم لدينا هنا لمساعدتك في الاستفادة القصوى من اشتراكك في كلاكيت AI.</p>" +
                        "<div style='text-align: center; margin-top: 40px; padding-top: 25px; border-top: 2px solid #f0f0f0;'>" +
                        "<p style='color: #999; margin: 0; font-size: 15px;'>مع أطيب التحيات،<br><strong style='color: #D4B06A; font-size: 16px;'>فريق كلاكيت AI</strong></p>" +
                        "</div>" +
                        "</div>" +
                        "<div style='text-align: center; padding: 25px; color: #999; font-size: 13px; background: #2F5233; color: white;'>" +
                        "<p style='margin: 0; opacity: 0.9;'>© 2025 كلاكيت AI. جميع الحقوق محفوظة.</p>" +
                        "<p style='margin: 8px 0 0 0; opacity: 0.7;'>تم إرسال هذا البريد الإلكتروني بخصوص اشتراكك CLA-%d</p>" +
                        "</div>" +
                        "</div>" +
                        "</div>",
                userName,
                subscription.getId(),
                planType,
                nextBilling,
                subscription.getId()
        );
    }
}