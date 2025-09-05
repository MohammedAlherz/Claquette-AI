package com.example.claquetteai.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiReminderMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send reminder email with HTML content
     */
    public void sendReminderEmail(String to, String subject, String htmlBody) {
        try {
            System.out.println("Sending Claquette AI reminder email to: " + to);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "Claquette AI Reminders");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            // Add headers to improve deliverability
            mimeMessage.setHeader("X-Priority", "3");
            mimeMessage.setHeader("X-Mailer", "Claquette AI Platform");

            mailSender.send(mimeMessage);
            System.out.println("Reminder email sent successfully");

        } catch (MessagingException ex) {
            System.err.println("MessagingException while sending reminder: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to send reminder email", ex);
        } catch (MailException ex) {
            System.err.println("MailException while sending reminder: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to send reminder email", ex);
        } catch (Exception ex) {
            System.err.println("Unexpected error while sending reminder: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to send reminder email", ex);
        }
    }

    /**
     * Build HTML email template for Claquette AI expiration alerts in Arabic
     */
    public String buildExpirationEmailHtml(String alertType, String userName, String planType,
                                           String expirationDate, int daysBeforeExpiry, Double price) {

        String alertColor = "urgent".equals(alertType) ? "#dc2626" : "#D4B06A";
        String alertTitle = "urgent".equals(alertType) ? "تنبيه عاجل لانتهاء الاشتراك" : "تذكير بانتهاء الاشتراك";
        String formattedPrice = price != null ? String.format("%.2f ريال سعودي", price) : "غير محدد";
        String renewUrl = "https://claquette-ai.com/dashboard/subscription";
        String planNameArabic = "ADVANCED".equals(planType) ? "الخطة المتقدمة" : "الخطة المجانية";

        return String.format("""
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>كلاكيت AI - تنبيه انتهاء الاشتراك</title>
            </head>
            <body style="margin: 0; padding: 0; background: #f8f9fa; font-family: 'Segoe UI', Tahoma, Arial, sans-serif; direction: rtl;">
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background: #f8f9fa; padding: 30px 0;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 25px rgba(0,0,0,0.1);">
                                
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #D4B06A, #B8965A); color: #ffffff; padding: 30px; text-align: center; position: relative;">
                                        <div style="display: inline-block; margin-bottom: 10px;">
                                            <div style="width: 50px; height: 50px; background: #2F5233; border: 2px solid white; border-radius: 8px; display: inline-block; text-align: center; line-height: 46px; font-size: 20px; font-weight: bold; margin-left: 10px; color: white;">CA</div>
                                            <span style="font-size: 24px; font-weight: 700; vertical-align: top; margin-top: 8px; display: inline-block;">كلاكيت AI</span>
                                        </div>
                                        <br>
                                        <div style="font-size: 12px; opacity: 0.9;">منصة الذكاء الاصطناعي لإنتاج الأفلام والمسلسلات</div>
                                    </td>
                                </tr>
                                
                                <!-- Alert Banner -->
                                <tr>
                                    <td style="background: %s; color: #ffffff; padding: 15px 30px; text-align: center; font-size: 16px; font-weight: 700;">
                                        %s
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 30px; color: #333;">
                                        <h2 style="margin: 0 0 15px 0; color: #2F5233; font-size: 22px;">مرحباً %s،</h2>
                                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #666; line-height: 1.8;">
                                            اشتراكك في <strong style="color: #D4B06A;">%s</strong> في كلاكيت AI سينتهي خلال <strong style="color: %s;">%d أيام</strong> في تاريخ %s.
                                        </p>
                                        
                                        <!-- Subscription Details Box -->
                                        <div style="background: linear-gradient(145deg, #f8f9fa, #ffffff); border-right: 4px solid #D4B06A; padding: 20px; margin: 25px 0; border-radius: 6px;">
                                            <h3 style="margin: 0 0 15px 0; color: #2F5233; font-size: 16px;">تفاصيل الاشتراك</h3>
                                            <table style="width: 100%%; border-collapse: collapse;">
                                                <tr>
                                                    <td style="padding: 5px 0; color: #666; font-weight: 600; width: 30%%;">الخطة:</td>
                                                    <td style="padding: 5px 0; color: #2F5233; font-weight: 700;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 5px 0; color: #666; font-weight: 600;">السعر:</td>
                                                    <td style="padding: 5px 0; color: #2F5233; font-weight: 700;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 5px 0; color: #666; font-weight: 600;">تاريخ الانتهاء:</td>
                                                    <td style="padding: 5px 0; color: %s; font-weight: 700;">%s</td>
                                                </tr>
                                            </table>
                                        </div>
                                        
                                        <p style="margin: 20px 0; font-size: 15px; color: #666; line-height: 1.8;">
                                            لضمان استمرار الاستفادة من أدوات الذكاء الاصطناعي المتقدمة لإنتاج الأفلام، وأدوات توليد السيناريو، وتطوير الشخصيات، يرجى تجديد اشتراكك قبل تاريخ الانتهاء.
                                        </p>
                                        
                                        <!-- CTA Button -->
                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="%s" style="display: inline-block; background: linear-gradient(45deg, #2F5233, #1a2e1d); color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; font-weight: 600; font-size: 16px;" target="_blank">
                                                تجديد الاشتراك
                                            </a>
                                        </div>
                                        
                                        <!-- Features Reminder -->
                                        <div style="background: #e8f5e8; padding: 20px; border-radius: 8px; margin: 25px 0;">
                                            <h4 style="margin: 0 0 10px 0; color: #2F5233; font-size: 14px;">ما ستستمر في الاستفادة منه:</h4>
                                            <ul style="margin: 0; padding-right: 20px; color: #666; font-size: 13px; text-align: right;">
                                                <li>توليد السيناريو بالذكاء الاصطناعي المتقدم</li>
                                                <li>أدوات تطوير الشخصيات</li>
                                                <li>إدارة إنتاج الأفلام والمسلسلات</li>
                                                <li>الدعم المتميز</li>
                                            </ul>
                                        </div>
                                        
                                        <p style="margin: 20px 0 0 0; color: #999; font-size: 12px; line-height: 1.5;">
                                            هذا تذكير تلقائي من كلاكيت AI. إذا كنت قد جددت اشتراكك بالفعل، يرجى تجاهل هذه الرسالة.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background: #2F5233; color: #ffffff; padding: 20px; text-align: center;">
                                        <p style="margin: 0 0 5px 0; font-size: 13px; opacity: 0.9;">تحتاج مساعدة؟ تواصل مع فريق الدعم</p>
                                        <p style="margin: 0 0 10px 0; font-size: 12px; opacity: 0.7;">support@claquette-ai.com</p>
                                        <p style="margin: 0; font-size: 11px; opacity: 0.6; color: #D4B06A;">
                                            حقوق الطبع والنشر 2025 كلاكيت AI. جميع الحقوق محفوظة.
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """,
                alertColor,                    // Alert banner background color
                alertTitle,                    // Alert title in Arabic
                userName != null ? userName : "عميلنا الكريم", // User name
                planNameArabic,               // Plan type in Arabic
                alertColor,                   // Days color
                daysBeforeExpiry,            // Days before expiry
                expirationDate,              // Expiration date
                planNameArabic,              // Plan type (repeated) in Arabic
                formattedPrice,              // Formatted price in Arabic
                alertColor,                  // Expiration date color
                expirationDate,              // Expiration date (repeated)
                renewUrl                     // Renewal URL
        );
    }

    /**
     * Build HTML email template for expired subscription notification in Arabic
     */
    public String buildExpirationNotificationEmail(String userName, String planType, String expiredDate, Double price) {
        String formattedPrice = price != null ? String.format("%.2f ريال سعودي", price) : "غير محدد";
        String renewUrl = "https://claquette-ai.com/dashboard/subscription";
        String planNameArabic = "ADVANCED".equals(planType) ? "الخطة المتقدمة" : "الخطة المجانية";

        return String.format("""
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>كلاكيت AI - انتهى الاشتراك</title>
            </head>
            <body style="margin: 0; padding: 0; background: #f8f9fa; font-family: 'Segoe UI', Tahoma, Arial, sans-serif; direction: rtl;">
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background: #f8f9fa; padding: 30px 0;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 25px rgba(0,0,0,0.1);">
                                
                                <!-- Header -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #D4B06A, #B8965A); color: #ffffff; padding: 30px; text-align: center; position: relative;">
                                        <div style="display: inline-block; margin-bottom: 10px;">
                                            <div style="width: 50px; height: 50px; background: #2F5233; border: 2px solid white; border-radius: 8px; display: inline-block; text-align: center; line-height: 46px; font-size: 20px; font-weight: bold; margin-left: 10px; color: white;">CA</div>
                                            <span style="font-size: 24px; font-weight: 700; vertical-align: top; margin-top: 8px; display: inline-block;">كلاكيت AI</span>
                                        </div>
                                        <br>
                                        <div style="font-size: 12px; opacity: 0.9;">منصة الذكاء الاصطناعي لإنتاج الأفلام والمسلسلات</div>
                                    </td>
                                </tr>
                                
                                <!-- Expired Banner -->
                                <tr>
                                    <td style="background: #dc2626; color: #ffffff; padding: 15px 30px; text-align: center; font-size: 16px; font-weight: 700;">
                                        انتهى الاشتراك
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 30px; color: #333;">
                                        <h2 style="margin: 0 0 15px 0; color: #2F5233; font-size: 22px;">مرحباً %s،</h2>
                                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #666; line-height: 1.8;">
                                            اشتراكك في <strong style="color: #D4B06A;">%s</strong> في كلاكيت AI قد انتهى في تاريخ %s.
                                        </p>
                                        
                                        <!-- Access Status Box -->
                                        <div style="background: #fee2e2; border-right: 4px solid #dc2626; padding: 20px; margin: 25px 0; border-radius: 6px;">
                                            <h3 style="margin: 0 0 15px 0; color: #dc2626; font-size: 16px;">تم تقييد الوصول</h3>
                                            <p style="margin: 0; color: #666; font-size: 14px; line-height: 1.6;">
                                                تم إيقاف وصولك إلى الميزات المتميزة. لاستعادة الوظائف الكاملة، يرجى تجديد اشتراكك.
                                            </p>
                                        </div>
                                        
                                        <!-- Subscription Details Box -->
                                        <div style="background: linear-gradient(145deg, #f8f9fa, #ffffff); border-right: 4px solid #D4B06A; padding: 20px; margin: 25px 0; border-radius: 6px;">
                                            <h3 style="margin: 0 0 15px 0; color: #2F5233; font-size: 16px;">تفاصيل الاشتراك المنتهي</h3>
                                            <table style="width: 100%%; border-collapse: collapse;">
                                                <tr>
                                                    <td style="padding: 5px 0; color: #666; font-weight: 600; width: 30%%;">الخطة:</td>
                                                    <td style="padding: 5px 0; color: #2F5233; font-weight: 700;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 5px 0; color: #666; font-weight: 600;">السعر:</td>
                                                    <td style="padding: 5px 0; color: #2F5233; font-weight: 700;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 5px 0; color: #666; font-weight: 600;">انتهى في:</td>
                                                    <td style="padding: 5px 0; color: #dc2626; font-weight: 700;">%s</td>
                                                </tr>
                                            </table>
                                        </div>
                                        
                                        <p style="margin: 20px 0; font-size: 15px; color: #666; line-height: 1.8;">
                                            لا تفوت أدوات إنتاج الأفلام المدعومة بالذكاء الاصطناعي المتقدمة! جدد الآن لاستعادة الوصول إلى توليد السيناريو وتطوير الشخصيات وجميع الميزات المتميزة.
                                        </p>
                                        
                                        <!-- CTA Button -->
                                        <div style="text-align: center; margin: 30px 0;">
                                            <a href="%s" style="display: inline-block; background: linear-gradient(45deg, #2F5233, #1a2e1d); color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; font-weight: 600; font-size: 16px;" target="_blank">
                                                جدد الآن
                                            </a>
                                        </div>
                                        
                                        <!-- What You're Missing -->
                                        <div style="background: #fff3cd; border-right: 4px solid #D4B06A; padding: 20px; border-radius: 8px; margin: 25px 0;">
                                            <h4 style="margin: 0 0 10px 0; color: #2F5233; font-size: 14px;">ما تفتقده:</h4>
                                            <ul style="margin: 0; padding-right: 20px; color: #666; font-size: 13px; text-align: right;">
                                                <li>توليد السيناريو بالذكاء الاصطناعي المتقدم</li>
                                                <li>أدوات تطوير الشخصيات</li>
                                                <li>إدارة إنتاج الأفلام والمسلسلات</li>
                                                <li>الدعم المتميز</li>
                                                <li>ميزات التصدير والتعاون</li>
                                            </ul>
                                        </div>
                                        
                                        <p style="margin: 20px 0 0 0; color: #999; font-size: 12px; line-height: 1.5;">
                                            يتم إرسال هذا الإشعار لأن اشتراكك في كلاكيت AI قد انتهى. جدد لاستعادة الوصول إلى جميع الميزات.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background: #2F5233; color: #ffffff; padding: 20px; text-align: center;">
                                        <p style="margin: 0 0 5px 0; font-size: 13px; opacity: 0.9;">تحتاج مساعدة في التجديد؟ تواصل مع فريق الدعم</p>
                                        <p style="margin: 0 0 10px 0; font-size: 12px; opacity: 0.7;">support@claquette-ai.com</p>
                                        <p style="margin: 0; font-size: 11px; opacity: 0.6; color: #D4B06A;">
                                            حقوق الطبع والنشر 2025 كلاكيت AI. جميع الحقوق محفوظة.
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """,
                userName != null ? userName : "عميلنا الكريم", // User name
                planNameArabic,              // Plan type in Arabic
                expiredDate,                 // Expired date
                planNameArabic,              // Plan type (repeated) in Arabic
                formattedPrice,              // Formatted price in Arabic
                expiredDate,                 // Expired date (repeated)
                renewUrl                     // Renewal URL
        );
    }
}