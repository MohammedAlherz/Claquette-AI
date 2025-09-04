package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CompanyDTOIN;
import com.example.claquetteai.DTO.CompanyDTOOUT;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<CompanyDTOOUT> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void addCompany(CompanyDTOIN dto) {
        // Create User
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // You should hash the password here
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Create Company
        Company company = new Company();
        // Don't set ID manually - @MapsId will handle it
        company.setName(dto.getName());
        company.setCommercialRegNo(dto.getCommercialRegNo());
        company.setUser(savedUser);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        companyRepository.save(company);
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
}