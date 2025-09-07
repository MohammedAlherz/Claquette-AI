package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.CompanyDTOIN;
import com.example.claquetteai.Service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/companies")
    public ResponseEntity<?> getAllCompanies() {
        return ResponseEntity.ok().body(companyService.getAllCompanies());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid CompanyDTOIN dto) {
        companyService.registerCompanyWithVerification(dto);
        return ResponseEntity.ok().body(new ApiResponse(
                "Company registered successfully. Please check your email to verify your account."
        ));
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<?> verify(@PathVariable Integer userId, @RequestParam("code") String code) {
        companyService.verifyUserEmail(userId, code);
        return ResponseEntity.ok().body(new ApiResponse("Email verified successfully!"));
    }

    @PostMapping("/resend/{userId}")
    public ResponseEntity<?> resend(@PathVariable Integer userId) {
        companyService.resendVerificationCode(userId);
        return ResponseEntity.ok().body(new ApiResponse("New verification code sent to your email."));
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Integer id,
                                           @RequestBody @Valid CompanyDTOIN dto) {
        companyService.updateCompany(id, dto);
        return ResponseEntity.ok().body(new ApiResponse("Company updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Integer id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().body(new ApiResponse("Company deleted successfully"));
    }

    @PostMapping(value = "/{userId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserPhoto(@PathVariable Integer userId,
                                                @RequestParam("file") MultipartFile file) {
        companyService.uploadProfilePhoto(userId, file);
        return ResponseEntity.ok(new ApiResponse("photo uploaded successfully"));
    }

    @GetMapping("/{userId}/photo")
    public ResponseEntity<?> getUserPhoto(@PathVariable Integer userId) {
        return companyService.getUserPhotoResponse(userId);
    }



}