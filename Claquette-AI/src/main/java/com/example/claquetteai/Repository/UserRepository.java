package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserById(Integer id);

    User findUserByCompany_ActiveSubscription(CompanySubscription companyActiveSubscription);
    User findUserByEmail(String email);
}
