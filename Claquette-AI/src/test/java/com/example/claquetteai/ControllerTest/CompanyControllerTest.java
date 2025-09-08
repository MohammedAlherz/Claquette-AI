package com.example.claquetteai.ControllerTest;

import com.example.claquetteai.Controller.CompanyController;
import com.example.claquetteai.DTO.CompanyDTOOUT;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;


import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc + Mockito: no Spring context, no web server.
 */
@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    CompanyService companyService;

    @InjectMocks
    CompanyController controller;

    MockMvc mockMvc;

    // Simple resolver to inject @AuthenticationPrincipal User params in controller methods
    private HandlerMethodArgumentResolver authPrincipalResolver(User fixedUser) {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && User.class.isAssignableFrom(parameter.getParameterType());
            }
            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          org.springframework.web.context.request.NativeWebRequest webRequest,
                                          org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                return fixedUser;
            }
        };
    }

    private User mockUser;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(77);
        mockUser.setFullName("Mock User");
        mockUser.setEmail("mock@ex.com");
        mockUser.setRole("COMPANY");

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(authPrincipalResolver(mockUser))
                .build();
    }

    // 1) POST /forgot-password
    @Test
    void forgotPassword_returnsOk_andCallsService() throws Exception {
        String email = "john@example.com";

        mockMvc.perform(post("/api/v1/company/forgot-password")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reset password email sent successfully")));

        ArgumentCaptor<String> emailCap = ArgumentCaptor.forClass(String.class);
        verify(companyService).forgotPassword(emailCap.capture());
        verifyNoMoreInteractions(companyService);
        org.junit.jupiter.api.Assertions.assertEquals(email, emailCap.getValue());
    }

    // 2) POST /reset-password
    @Test
    void resetPassword_returnsOk_andCallsService() throws Exception {
        String token = "abc123";
        String newPass = "NewPass1@";

        mockMvc.perform(post("/api/v1/company/reset-password")
                        .param("token", token)
                        .param("newPassword", newPass))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Password reset successfully")));

        verify(companyService).resetPasswordWithToken(token, newPass);
        verifyNoMoreInteractions(companyService);
    }

    // 3) GET /companies
    @Test
    void getAllCompanies_returnsEmptyArray_andCallsService() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(emptyList());

        mockMvc.perform(get("/api/v1/company/companies"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(companyService).getAllCompanies();
        verifyNoMoreInteractions(companyService);
    }

    private static RequestPostProcessor authWith(User u) {
        return request -> {
            var auth = new UsernamePasswordAuthenticationToken(u, null, emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
            return request;
        };
    }

    // 4) GET /my-company  (uses @AuthenticationPrincipal User)
    @Test
    void getMyCompany_usesAuthPrincipalId_andReturnsBody() throws Exception {
        // given principal with id=77
        User principal = new User();
        principal.setId(77);

        // stub service to return a DTO (no id field assumed)
        CompanyDTOOUT dto = new CompanyDTOOUT();
        dto.setEmail("Hello@gmail.com");
        dto.setFullName("Hussam");
        dto.setName("Acme Studios");
        dto.setCommercialRegNo("1234567890");

        when(companyService.getMyCompany(77)).thenReturn(dto);

        // when + then
        mockMvc.perform(get("/api/v1/company/my-company").with(authWith(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Studios"))
                .andExpect(jsonPath("$.commercialRegNo").value("1234567890"))
                .andExpect(jsonPath("$.email").value("Hello@gmail.com"))
                .andExpect(jsonPath("$.fullName").value("Hussam"));

        verify(companyService).getMyCompany(77);
        verifyNoMoreInteractions(companyService);
    }

    // 5) POST /photo  (multipart upload + @AuthenticationPrincipal)
    @Test
    void uploadPhoto_returnsOk_andCallsServiceWithUserId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/company/photo").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Photo uploaded successfully")));

        ArgumentCaptor<Integer> idCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<MultipartFile> fileCap = ArgumentCaptor.forClass(MultipartFile.class);
        verify(companyService).uploadProfilePhoto(idCap.capture(), fileCap.capture());
        verifyNoMoreInteractions(companyService);

        org.junit.jupiter.api.Assertions.assertEquals(77, idCap.getValue());
        org.junit.jupiter.api.Assertions.assertEquals("avatar.png", fileCap.getValue().getOriginalFilename());
        org.junit.jupiter.api.Assertions.assertEquals(3, fileCap.getValue().getBytes().length);
    }
}
