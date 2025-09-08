package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.CompanyDTOIN;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.CompanyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // PUBLIC - No authentication required
    //  Mohammed Shamhani
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        companyService.resetPasswordWithToken(token, newPassword);
        return ResponseEntity.ok(new ApiResponse("Password reset successfully"));
    }

    // PUBLIC - No authentication required
    //  Mohammed Shamhani
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        companyService.forgotPassword(email);
        return ResponseEntity.ok(new ApiResponse("Reset password email sent successfully"));
    }

    // ADMIN ONLY - Get all companies in system
    //  Mohammed Shamhani
    @GetMapping("/companies")
    public ResponseEntity<?> getAllCompanies() {
        return ResponseEntity.ok().body(companyService.getAllCompanies());
    }

    //  Mohammed Shamhani
    @GetMapping("/my-company")
    public ResponseEntity<?> getUserCompany(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok().body(companyService.getMyCompany(user.getId()));
    }

    // PUBLIC - No authentication required
    //  Mohammed Shamhani
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid CompanyDTOIN dto) throws JsonProcessingException {
        companyService.registerCompanyWithVerification(dto);
        return ResponseEntity.ok().body(new ApiResponse(
                "Company registered successfully. Please check your email to verify your account."
        ));
    }

    // PUBLIC - No authentication required
    //  Mohammed Shamhani
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@AuthenticationPrincipal User user, @RequestParam("code") String code) {
        companyService.verifyUserEmail(user.getId(), code);
        return ResponseEntity.ok().body(new ApiResponse("Email verified successfully!"));
    }

    // COMPANY or ADMIN - resend verification
    //  Mohammed Shamhani
    @PostMapping("/resend")
    public ResponseEntity<?> resend(@AuthenticationPrincipal User user) {
        companyService.resendVerificationCode(user.getId());
        return ResponseEntity.ok().body(new ApiResponse("New verification code sent to your email."));
    }

    // COMPANY - Update company (users can update their company)
    //  Mohammed Shamhani
    @PutMapping("/update")
    public ResponseEntity<?> updateCompany(@AuthenticationPrincipal User user,
                                           @RequestBody @Valid CompanyDTOIN dto) {
        // Authorization check inside service method
        companyService.updateOwnCompany(user.getId(), dto);
        return ResponseEntity.ok().body(new ApiResponse("Company updated successfully"));
    }

    // ADMIN ONLY - Delete any company
    //  Mohammed Shamhani
    @DeleteMapping("/delete/{companyId}")
    public ResponseEntity<?> deleteCompany(@AuthenticationPrincipal User user,@PathVariable Integer companyId) {
        companyService.deleteCompany(user.getId(),companyId);
        return ResponseEntity.ok().body(new ApiResponse("Company deleted successfully"));
    }

    // COMPANY or ADMIN - Upload photo for authenticated user
    // Hussam
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserPhoto(@AuthenticationPrincipal User user,
                                             @RequestParam("file") MultipartFile file) {
        companyService.uploadProfilePhoto(user.getId(), file);
        return ResponseEntity.ok(new ApiResponse("Photo uploaded successfully"));
    }

    // COMPANY - Get own profile info
    // Hussam
    @GetMapping(value = "/profile-photo", produces = {
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    })
    public ResponseEntity<byte[]> getProfile(@AuthenticationPrincipal User user) {
        return companyService.getUserPhotoResponse(user.getId());
    }
}