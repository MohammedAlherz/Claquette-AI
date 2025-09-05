package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.CompanySubscription;
import com.example.claquetteai.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findPaymentByCompanySubscription(CompanySubscription companySubscription);
}
