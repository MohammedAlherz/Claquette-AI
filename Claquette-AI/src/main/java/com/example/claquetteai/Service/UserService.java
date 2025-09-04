package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void addUser(User user, Integer companyId) {
        Company company = companyRepository.findCompanyById(companyId);
        if (company == null) {
            throw new ApiException("Company is required when creating a user");
        }


        user.setCompany(company);
        company.setUser(user);


        userRepository.save(user);
    }


    public void updateUser(Integer id, User updatedUser) {
        User user = userRepository.findUserById(id);

        if (user == null) {
            throw new ApiException("User not found with id " + id);
        }

        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(updatedUser.getPassword());

        userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new ApiException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }


//    public void addUserWithCompany(User user, Company company) {
//
//        user.setCompany(company);
//        company.setUser(user);
//
//        userRepository.save(user);
//    }
//
//    public void assignCompanyToUser(Integer userId, Integer companyId) {
//        User user = userRepository.findUserById(userId);
//        Company company = companyRepository.findCompanyById(companyId);
//
//        if (user == null || company == null) {
//            throw new ApiException("User or Company not found");
//        }
//
//
//        user.setCompany(company);
//        company.setUser(user);
//
//
//        userRepository.save(user);
//        companyRepository.save(company);
//    }
//


}
