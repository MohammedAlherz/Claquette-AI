package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Integer> {

    CompanySubscription findCompanySubscriptionById(Integer subscriptionId);

    List<CompanySubscription> findByStatus(String status);

    List<CompanySubscription> findCompanySubscriptionsByCompany_User(User companyUser);
}
