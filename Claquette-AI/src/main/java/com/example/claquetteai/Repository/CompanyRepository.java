package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Company findCompanyById(Integer id);

}
