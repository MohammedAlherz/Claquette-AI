package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil; // ✅ inject util

    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findUserByEmail(email);

        if (user == null) {
            throw new ApiException("User not found");
        }


        String token = jwtUtil.generateResetToken(user.getEmail());
        String resetLink = "http://localhost:8080/api/company/reset-password?token=" + token;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("🔒 إعادة تعيين كلمة المرور - كلاكيت AI");
            helper.setText(generateResetPasswordBody(user.getFullName(), resetLink), true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Reset password email sent to " + user.getEmail());

        } catch (MessagingException e) {
            throw new RuntimeException("❌ Failed to send reset password email", e);
        }
    }

    private String generateResetPasswordBody(String userName, String resetLink) {
        return String.format(
                "<div style='font-family: \"Segoe UI\", Tahoma, Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8f9fa; padding: 20px; direction: rtl;'>" +
                        "<div style='background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 25px rgba(0,0,0,0.1);'>" +
                        "<div style='background: linear-gradient(135deg, #D4B06A, #B8965A); color: white; padding: 30px; text-align: center;'>" +
                        "<h1 style='margin: 0; font-size: 26px; font-weight: 700;'>🔑 إعادة تعيين كلمة المرور</h1>" +
                        "</div>" +
                        "<div style='padding: 40px;'>" +
                        "<p style='font-size: 18px; color: #333;'>مرحباً %s،</p>" +
                        "<p style='color: #666; line-height: 1.8; font-size: 16px;'>لقد تلقينا طلباً لإعادة تعيين كلمة المرور الخاصة بك. يمكنك إعادة تعيين كلمة المرور من خلال الرابط التالي:</p>" +
                        "<div style='margin: 20px 0; text-align: center;'>" +
                        "<a href='%s' style='background: #2F5233; color: white; padding: 15px 25px; border-radius: 8px; text-decoration: none; font-size: 18px;'>🔒 إعادة تعيين كلمة المرور</a>" +
                        "</div>" +
                        "<p style='color: #999; font-size: 14px;'>⚠️ هذا الرابط صالح لمدة 15 دقيقة فقط.</p>" +
                        "</div>" +
                        "</div>",
                userName, resetLink
        );
    }
}
