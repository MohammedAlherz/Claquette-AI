package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CompanyDTOIN;
import com.example.claquetteai.DTO.CompanyDTOOUT;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final VerificationService verificationService;
    private final  VerificationEmailService emailService;


    public List<CompanyDTOOUT> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerCompanyWithVerification(CompanyDTOIN dto) {
        // Create User
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActiveAccount(false); // not verified yet

        User savedUser = userRepository.save(user);

        // Create Company
        Company company = new Company();
        company.setName(dto.getName());
        company.setCommercialRegNo(dto.getCommercialRegNo());
        company.setUser(savedUser);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        companyRepository.save(company);

        // 🔑 Generate and send code
        String code = verificationService.generateCode(user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
    }

    @Transactional
    public void verifyUserEmail(Integer userId, String code) {
        User user = userRepository.findUserById(userId);
        if (!verificationService.verifyCode(userId, code)) {
            throw new ApiException("❌ Invalid or expired verification code");
        }

        if (user == null) throw new ApiException("User not found");

        if (user.isActiveAccount()) {
            throw new ApiException("User already verified");
        }

        user.setActiveAccount(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }


    @Transactional
    public void resendVerificationCode(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }
        if (user.isActiveAccount()) {
            throw new ApiException("User already verified");
        }

        String code = verificationService.generateCode(user.getEmail());
        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
    }


    public void updateCompany(Integer id, CompanyDTOIN dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Company not found with id " + id));
        User user = company.getUser();
        // Update User fields
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // You should hash the password here
        user.setUpdatedAt(LocalDateTime.now());
        // Update Company fields
        company.setName(dto.getName());
        company.setCommercialRegNo(dto.getCommercialRegNo());
        company.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        companyRepository.save(company);
    }

    public void deleteCompany(Integer id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Company not found with id " + id));
        // This will also delete the user due to cascade relationship
        companyRepository.delete(company);
    }

    private CompanyDTOOUT convertToDTO(Company company) {
        CompanyDTOOUT dto = new CompanyDTOOUT();
        // User fields
        User user = company.getUser();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        // Company fields
        dto.setName(company.getName());
        dto.setCommercialRegNo(company.getCommercialRegNo());
        return dto;
    }

    @Transactional
    public void uploadProfilePhoto(Integer userId, MultipartFile file) {
        Set<String> ALLOWED = Set.of("image/png", "image/jpeg", "image/webp");
        long MAX_BYTES = 5L * 1024 * 1024; // 5 MB
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("No file uploaded");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("File too large (max 5MB)");

        String ct = file.getContentType() == null ? null : file.getContentType().toLowerCase();
        if (ct == null || !ALLOWED.contains(ct)) {
            throw new IllegalArgumentException("Unsupported type. Allowed: PNG, JPEG, WEBP");
        }

        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("user not found");

        byte[] bytes;
        try { bytes = file.getBytes(); }
        catch (IOException e) { throw new RuntimeException("Failed to read upload", e); }

        // sanity: ensure real image
        try (var in = new ByteArrayInputStream(bytes)) {
            var img = javax.imageio.ImageIO.read(in);
            if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
                throw new IllegalArgumentException("Invalid image");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid image", e);
        }

        String b64 = java.util.Base64.getEncoder().encodeToString(bytes);

        // ✅ store in the correct fields on User
        user.setProfileImageBase64(b64);          // LONGTEXT
        user.setProfileImageContentType(ct);      // "image/png" | "image/jpeg" | "image/webp"

        userRepository.save(user);
    }

    @Transactional
    public ResponseEntity<byte[]> getUserPhotoResponse(Integer userId) {
        User u = userRepository.findUserById(userId);
        if (u == null) return ResponseEntity.notFound().build();

        String b64 = u.getProfileImageBase64();
        if (b64 == null || b64.isBlank()) return ResponseEntity.notFound().build();

        byte[] bytes;
        try { bytes = java.util.Base64.getDecoder().decode(b64); }
        catch (IllegalArgumentException e) { return ResponseEntity.status(500).build(); }

        MediaType mediaType = MediaType.IMAGE_PNG; // default
        if (u.getProfileImageContentType() != null) {
            try { mediaType = MediaType.parseMediaType(u.getProfileImageContentType()); }
            catch (Exception ignored) { /* keep default */ }
        }

        return ResponseEntity.ok()
                .contentType(mediaType) // ✅ critical so browser renders the image
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"user-" + userId + imageExt(mediaType) + "\"")
                .body(bytes);
    }
    private static String imageExt(MediaType mt) {
        if (MediaType.IMAGE_JPEG.equals(mt)) return ".jpg";
        if (MediaType.valueOf("image/webp").equals(mt)) return ".webp";
        return ".png";
    }


}