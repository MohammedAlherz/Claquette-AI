package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CompanyDTOIN;
import com.example.claquetteai.DTO.CompanyDTOOUT;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final  PasswordResetService passwordResetService;
    private final JwtUtil  jwtUtil;
    private final WatheqService watheqService;

























    @Transactional
    public void forgotPassword(String email) {
        passwordResetService.sendPasswordResetEmail(email);
    }

    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        String hasPassword = bCrypt.encode(newPassword);
        if (!jwtUtil.validateToken(token)) {
            throw new ApiException("❌ Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findUserByEmail(email);
        if (user == null) throw new ApiException("User not found");

        user.setPassword(hasPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<CompanyDTOOUT> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerCompanyWithVerification(CompanyDTOIN dto) throws JsonProcessingException, JsonProcessingException {
        // ✅ Validate CR
        watheqService.validateCommercialRegNo(dto);
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

    public CompanyDTOOUT getMyCompany(Integer userId) {
        // Find the user by ID
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found with id " + userId);
        }

        // Get the user's company
        Company company = user.getCompany();
        if (company == null) {
            throw new ApiException("No company associated with this user");
        }

        // Convert to DTO and return
        return convertToDTO(company);
    }
    public void updateOwnCompany(Integer userId, CompanyDTOIN dto) {
        // Find the user by ID
        User authenticatedUser = userRepository.findUserById(userId);
        if (authenticatedUser == null) {
            throw new ApiException("User not found with id " + userId);
        }

        // Get the user's company
        Company company = authenticatedUser.getCompany();
        if (company == null) {
            throw new ApiException("No company associated with this user");
        }

        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

        // Only hash password if it's provided
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            String hashedPassword = bCrypt.encode(dto.getPassword());
            authenticatedUser.setPassword(hashedPassword);
        }

        // Update User fields
        authenticatedUser.setFullName(dto.getFullName());
        authenticatedUser.setEmail(dto.getEmail());
        authenticatedUser.setUpdatedAt(LocalDateTime.now());

        // Update Company fields
        company.setName(dto.getName());
        company.setCommercialRegNo(dto.getCommercialRegNo());
        company.setUpdatedAt(LocalDateTime.now());

        // Save both entities
        userRepository.save(authenticatedUser);
        companyRepository.save(company);
    }
    public void deleteCompany(Integer userId,Integer id) {
        User user = userRepository.findUserById(userId);
        if(user == null){
            throw new ApiException("User not found");
        }

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Company not found"));
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