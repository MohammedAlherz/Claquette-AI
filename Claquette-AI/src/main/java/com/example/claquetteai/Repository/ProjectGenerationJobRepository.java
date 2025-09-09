package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.ProjectGenerationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectGenerationJobRepository extends JpaRepository<ProjectGenerationJob, String> {
}
