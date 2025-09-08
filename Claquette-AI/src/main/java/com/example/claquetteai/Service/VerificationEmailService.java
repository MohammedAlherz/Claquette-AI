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

    public void sendContactEmail(String toEmail,
                                 String fullName,
                                 String senderEmail,
                                 String phoneNumber,
                                 String messageText) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            // Send to the target inbox
            helper.setTo(toEmail);
            // Subject as requested
            helper.setSubject("contact request");

            // Set From with personal name; also set Reply-To to ensure replies go to the sender
            // If your SMTP rejects arbitrary From, keep your authenticated account as default From
            // and rely on Reply-To.
            helper.setFrom(senderEmail, fullName);
            helper.setReplyTo(senderEmail, fullName);

            // Build and set HTML body
            helper.setText(generateContactEmailBody(fullName, senderEmail, phoneNumber, messageText), true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Contact email sent to " + toEmail + " from " + fullName + " <" + senderEmail + ">");
        } catch (Exception e) { // catch MessagingException | UnsupportedEncodingException
            System.err.println("❌ Failed to send contact email: " + e.getMessage());
            throw new RuntimeException("فشل إرسال رسالة التواصل عبر البريد الإلكتروني", e);
        }
    }

    private String generateContactEmailBody(String fullName, String email, String phone, String message) {
        // Styled to match your verification theme, with RTL and branded header
        return "<div style='font-family:\"Segoe UI\", Tahoma, Arial, sans-serif; max-width:600px; margin:0 auto; background:#f8f9fa; padding:20px; direction:rtl;'>"
                + "<div style='background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 8px 25px rgba(0,0,0,0.1);'>"

                + "<div style='background:linear-gradient(135deg,#D4B06A,#B8965A); color:#fff; padding:32px; text-align:center;'>"
                + "  <div style='display:flex; align-items:center; justify-content:center; gap:12px;'>"
                + "    <div style='width:44px; height:44px; background:#2F5233; border:2px solid #fff; border-radius:8px; display:flex; align-items:center; justify-content:center; font-size:18px;'>📬</div>"
                + "    <h1 style='margin:0; font-size:28px; font-weight:700;'>كلاكيت AI</h1>"
                + "  </div>"
                + "  <p style='margin:8px 0 0; opacity:.9; font-size:15px;'>طلب تواصل جديد من النموذج</p>"
                + "</div>"

                + "<div style='padding:28px;'>"
                + "  <h2 style='color:#2F5233; margin:0 0 16px; font-size:22px;'>تفاصيل الطلب</h2>"

                + "  <div style='background:#f7fbf7; border:1px solid #e2efe2; border-radius:10px; padding:16px; margin-bottom:16px;'>"
                + "    <div style='display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px dashed #e6e6e6;'>"
                + "      <strong style='color:#2F5233;'>الاسم الكامل:</strong>"
                + "      <span style='color:#333;'>" + escape(fullName) + "</span>"
                + "    </div>"
                + "    <div style='display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px dashed #e6e6e6;'>"
                + "      <strong style='color:#2F5233;'>البريد الإلكتروني:</strong>"
                + "      <a href='mailto:" + escape(email) + "' style='color:#1b5e20; text-decoration:none;'>" + escape(email) + "</a>"
                + "    </div>"
                + "    <div style='display:flex; justify-content:space-between; padding:8px 0;'>"
                + "      <strong style='color:#2F5233;'>رقم الجوال:</strong>"
                + "      <span style='color:#333;'>" + escape(phone) + "</span>"
                + "    </div>"
                + "  </div>"

                + "  <h3 style='color:#2F5233; margin:20px 0 10px; font-size:18px;'>الرسالة</h3>"
                + "  <div style='background:#fff; border:1px solid #eee; border-radius:10px; padding:16px; line-height:1.8; color:#333; white-space:pre-wrap;'>"
                +       nl2br(escape(message))
                + "  </div>"

                + "  <div style='background:linear-gradient(135deg,#2F5233,#1a2e1d); color:#fff; padding:16px; border-radius:12px; text-align:center; margin:24px 0 0;'>"
                + "    <p style='margin:0; font-size:14px;'>للرد على المرسل، يمكنك الضغط على البريد أعلاه أو الرد مباشرة على هذا الإيميل.</p>"
                + "  </div>"
                + "</div>"

                + "</div>"

                + "<div style='text-align:center; padding:18px; font-size:12px; background:#2F5233; color:#fff;'>"
                + "  <p style='margin:0; opacity:.9;'>© 2025 كلاكيت AI. جميع الحقوق محفوظة.</p>"
                + "</div>"
                + "</div>";
    }

    // Simple helpers to keep HTML safe and readable
    private String escape(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String nl2br(String s) {
        return s == null ? "" : s.replace("\n", "<br>");
    }
}
