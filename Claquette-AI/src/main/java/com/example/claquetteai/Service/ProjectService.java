package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public void addProject(Project project, Integer companyId) {
        Company company = companyRepository.findCompanyById(companyId);
        if (company == null) {
            throw new ApiException("Company not found with id " + companyId);
        }

        project.setCompany(company);

        projectRepository.save(project);
    }

    public void updateProject(Integer id, Project updatedProject) {
        Project project = projectRepository.findProjectById(id);
        if (project == null) {
            throw new ApiException("Project not found with id " + id);
        }

        project.setTitle(updatedProject.getTitle());
        project.setDescription(updatedProject.getDescription());
        project.setScenario(updatedProject.getScenario());
        project.setProjectType(updatedProject.getProjectType());
        project.setGenre(updatedProject.getGenre());
        project.setBudget(updatedProject.getBudget());
        project.setLocation(updatedProject.getLocation());
        project.setTargetAudience(updatedProject.getTargetAudience());
        project.setStatus(updatedProject.getStatus());
        project.setStartProjectDate(updatedProject.getStartProjectDate());
        project.setEndProjectDate(updatedProject.getEndProjectDate());

        projectRepository.save(project);
    }

    public void deleteProject(Integer id) {
        if (!projectRepository.existsById(id)) {
            throw new ApiException("Project not found with id " + id);
        }
        projectRepository.deleteById(id);
    }
}
