package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CompanyDTO;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public void addCompany(CompanyDTO dto) {
        User user = userRepository.findUserById(dto.getUserId());
        if (user == null) {
            throw new ApiException("User not found with id " + dto.getUserId());
        }

        if (user.getCompany() != null) {
            throw new ApiException("This user already has a company");
        }

        Company company = new Company();
        company.setId(user.getId());
        company.setName(dto.getName());
        company.setCommercialRegNo(dto.getCommercialRegNo());
        company.setUser(user);

        companyRepository.save(company);
    }


    public void updateCompany(Integer id, CompanyDTO dto) {
        Company company = companyRepository.findCompanyById(id);
        if (company == null) {
            throw new ApiException("Company not found with id " + id);
        }

        company.setName(dto.getName());
        company.setCommercialRegNo(dto.getCommercialRegNo());

        companyRepository.save(company);
    }

    public void deleteCompany(Integer id) {
        Company company = companyRepository.findCompanyById(id);
        if (company == null) {
            throw new ApiException("Company not found with id " + id);
        }

        companyRepository.delete(company);
    }




}
