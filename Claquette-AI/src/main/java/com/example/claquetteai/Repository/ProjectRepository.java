package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Project findProjectById(Integer id);

    List<Project> findProjectsByCompany_User_Id(Integer companyUserId);
}
