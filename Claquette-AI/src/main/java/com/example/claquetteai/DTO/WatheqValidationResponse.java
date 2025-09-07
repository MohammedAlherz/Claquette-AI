package com.example.claquetteai.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)   // ✅ تجاهل أي حقول غير معروفة
public class WatheqValidationResponse {

    // حقول أساسية راح تجيك من API
    private String id;         // ممكن يجي رقم كـ String أو int → نخليه String
    private String name;       // "نشط" أو غيره

    // حقول إضافية نستخدمها داخليًا
    private String commercialRegNo;   // رقم السجل التجاري
    private String status;            // حالة السجل
    private String statusNameAr;      // الاسم بالعربية
    private String statusNameEn;      // الاسم بالإنجليزية
    private boolean valid;            // صالح؟
    private boolean active;           // نشط؟
    private String message;           // رسالة النظام
    private String source;            // مصدر التحقق
    private String validatedAt;       // وقت التحقق
    private String reason;            // لو فيه سبب إيقاف أو رفض
    private String date;              // أي تاريخ يرجع من API
}
