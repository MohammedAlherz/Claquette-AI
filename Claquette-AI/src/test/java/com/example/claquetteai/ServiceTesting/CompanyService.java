package com.example.claquetteai.ServiceTesting;

import com.example.claquetteai.DTO.CompanyDTOIN;
import com.example.claquetteai.DTO.CompanyDTOOUT;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.UserRepository;
import com.example.claquetteai.Service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyService {

    @InjectMocks
    com.example.claquetteai.Service.CompanyService companyService;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordResetService passwordResetService;

    @Mock
    WatheqService watheqService;

    @Mock
    JwtUtil jwtUtil;


    // ---------- Existing ----------
    @Test
    public void registerCompanyWithVerification_callsWatheqValidate() throws JsonProcessingException {
        CompanyDTOIN dto = new CompanyDTOIN();
        dto.setCommercialRegNo("7001234567");

        companyService.registerCompanyWithVerification(dto);

        verify(watheqService, times(1)).validateCommercialRegNo(dto);
        verifyNoMoreInteractions(watheqService);
    }

    // ---------- getAllCompanies ----------
    @Test
    public void getAllCompanies_returnsMappedDTOs() {
        User u1 = new User();
        u1.setFullName("Alice");
        u1.setEmail("alice@example.com");
        Company c1 = new Company();
        c1.setName("Acme");
        c1.setCommercialRegNo("7001111111");
        c1.setUser(u1);

        User u2 = new User();
        u2.setFullName("Bob");
        u2.setEmail("bob@example.com");
        Company c2 = new Company();
        c2.setName("Beta");
        c2.setCommercialRegNo("7002222222");
        c2.setUser(u2);

        when(companyRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CompanyDTOOUT> result = companyService.getAllCompanies();

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Alice", result.get(0).getFullName());
        Assertions.assertEquals("alice@example.com", result.get(0).getEmail());
        Assertions.assertEquals("Acme", result.get(0).getName());
        Assertions.assertEquals("7001111111", result.get(0).getCommercialRegNo());

        Assertions.assertEquals("Bob", result.get(1).getFullName());
        Assertions.assertEquals("bob@example.com", result.get(1).getEmail());
        Assertions.assertEquals("Beta", result.get(1).getName());
        Assertions.assertEquals("7002222222", result.get(1).getCommercialRegNo());

        verify(companyRepository, times(1)).findAll();
    }

    @Test
    public void getAllCompanies_empty_returnsEmptyList() {
        when(companyRepository.findAll()).thenReturn(List.of());

        List<CompanyDTOOUT> result = companyService.getAllCompanies();

        Assertions.assertTrue(result.isEmpty());
        verify(companyRepository, times(1)).findAll();
    }

    // ---------- forgotPassword ----------
    @Test
    public void forgotPassword_delegatesToPasswordResetService() {
        String email = "user@example.com";
        companyService.forgotPassword(email);
        verify(passwordResetService, times(1)).sendPasswordResetEmail(email);
    }

    // ---------- resetPasswordWithToken ----------
    @Test
    public void resetPasswordWithToken_valid_updatesPasswordAndSaves() {
        String token = "valid-token";
        String email = "user@example.com";
        String newPassword = "N3wP@ss!";

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn(email);

        User u = new User();
        u.setEmail(email);
        when(userRepository.findUserByEmail(email)).thenReturn(u);

        companyService.resetPasswordWithToken(token, newPassword);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        User saved = captor.getValue();

        Assertions.assertNotNull(saved.getPassword());
        Assertions.assertNotEquals(newPassword, saved.getPassword()); // should be hashed
    }


    }
