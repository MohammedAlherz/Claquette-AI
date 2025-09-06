package com.example.claquetteai.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationEmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String userName, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🔑 كود تفعيل حسابك - كلاكيت AI");
            helper.setText(generateVerificationEmailBody(userName, code), true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Verification email sent to " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
            throw new RuntimeException("فشل إرسال البريد الإلكتروني", e);
        }
    }


    private String generateVerificationEmailBody(String userName, String code) {
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

                        // المحتوى
                        "<div style='padding: 40px;'>" +
                        "<h2 style='color: #2F5233; margin-top: 0; font-size: 24px;'>🔑 رمز التحقق</h2>" +
                        "<p style='font-size: 18px; color: #333; margin-bottom: 20px;'>عزيزي %s،</p>" +
                        "<p style='color: #666; line-height: 1.8; font-size: 16px;'>نشكرك على التسجيل في كلاكيت AI. لإكمال عملية إنشاء حسابك يرجى إدخال رمز التحقق أدناه:</p>" +

                        "<div style='background: #f0f8f0; border: 2px dashed #2F5233; padding: 20px; text-align: center; font-size: 28px; font-weight: bold; color: #2F5233; letter-spacing: 8px; margin: 20px 0;'>" +
                        "%s" +
                        "</div>" +

                        "<p style='color: #666; font-size: 14px;'>⚠️ هذا الرمز صالح لمدة 10 دقائق فقط.</p>" +

                        "<div style='background: linear-gradient(135deg, #2F5233, #1a2e1d); color: white; padding: 20px; border-radius: 12px; text-align: center; margin: 30px 0;'>" +
                        "<p style='margin: 0; font-size: 16px;'>إذا لم تقم بإنشاء حساب في كلاكيت AI، يرجى تجاهل هذا البريد الإلكتروني.</p>" +
                        "</div>" +

                        "<div style='text-align: center; margin-top: 40px; padding-top: 25px; border-top: 2px solid #f0f0f0;'>" +
                        "<p style='color: #999; margin: 0; font-size: 15px;'>مع أطيب التحيات،<br><strong style='color: #D4B06A; font-size: 16px;'>فريق كلاكيت AI</strong></p>" +
                        "</div>" +

                        "</div>" +

                        "<div style='text-align: center; padding: 25px; color: #999; font-size: 13px; background: #2F5233; color: white;'>" +
                        "<p style='margin: 0; opacity: 0.9;'>© 2025 كلاكيت AI. جميع الحقوق محفوظة.</p>" +
                        "</div>" +
                        "</div>",
                userName, code
        );
    }
}
