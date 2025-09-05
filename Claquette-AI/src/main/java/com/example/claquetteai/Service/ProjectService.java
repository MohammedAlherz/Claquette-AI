package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CharactersDTOOUT;
import com.example.claquetteai.DTO.ProjectDTOOUT;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CharacterRepository;
import com.example.claquetteai.Repository.CompanyRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    public List<ProjectDTOOUT> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProjectDTOOUT> getProjectById(Integer userId) {
        List<Project> project = projectRepository.findProjectsByCompany_User_Id((userId));
        if (project.isEmpty()) {
            throw new ApiException("Project not found with id " + userId);
        }
        return convertToDTO(project);
    }

    public void addProject(Project project, Integer companyId) {
        Company company = companyRepository.findCompanyById(companyId);
        if (company == null) {
            throw new ApiException("Company not found with id " + companyId);
        }

        project.setCompany(company);
        project.setStatus("IN_DEVELOPMENT");
        projectRepository.save(project);
    }

    public void updateProject(Integer id, Project updatedProject) {
        Project project = projectRepository.findProjectById(id);
        if (project == null) {
            throw new ApiException("Project not found with id " + id);
        }

        project.setTitle(updatedProject.getTitle());
        project.setDescription(updatedProject.getDescription());
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

    private ProjectDTOOUT convertToDTO(Project project) {
        ProjectDTOOUT dto = new ProjectDTOOUT();
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setProjectType(project.getProjectType());
        dto.setGenre(project.getGenre());
        dto.setBudget(project.getBudget());
        dto.setTargetAudience(project.getTargetAudience());
        dto.setLocation(project.getLocation());
        dto.setStatus(project.getStatus());
        dto.setStartProjectDate(project.getStartProjectDate());
        dto.setEndProjectDate(project.getEndProjectDate());

        return dto;
    }
    private List<ProjectDTOOUT> convertToDTO(List<Project> projects) {
        List<ProjectDTOOUT> dtoList = new ArrayList<>();
        for (Project p : projects) {
            ProjectDTOOUT dto = new ProjectDTOOUT();
            dto.setTitle(p.getTitle());
            dto.setDescription(p.getDescription());
            dto.setProjectType(p.getProjectType());
            dto.setGenre(p.getGenre());
            dto.setBudget(p.getBudget());
            dto.setTargetAudience(p.getTargetAudience());
            dto.setLocation(p.getLocation());
            dto.setStatus(p.getStatus());
            dto.setStartProjectDate(p.getStartProjectDate());
            dto.setEndProjectDate(p.getEndProjectDate());
            dtoList.add(dto);
        }
        return dtoList;
    }


    public Integer projectsCount(Integer userId){
        List<Project> projects = projectRepository.findProjectsByCompany_User_Id(userId);
        return projects.size();
    }


    public Double getTotalBudget(Integer userId){
        List<Project> projects = projectRepository.findProjectsByCompany_User_Id(userId);
        Double total = 0.0;
        for (Project p : projects){
            total+=p.getBudget();
        }
        return total;
    }

    public List<CharactersDTOOUT> projectCharacters(Integer userId, Integer projectId){
        Project project = projectRepository.findProjectById(projectId);
        if (project == null){
            throw new ApiException("project not found");
        }
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised to do this");
        }
        List<FilmCharacters> characters = characterRepository.findFilmCharactersByProject(project);
        List<CharactersDTOOUT> dto = new ArrayList<>();
        for (FilmCharacters filmCharacters : characters ){
            CharactersDTOOUT charactersDTOOUT = new CharactersDTOOUT(filmCharacters.getName(),filmCharacters.getAge(),filmCharacters.getBackground());
            dto.add(charactersDTOOUT);
        }
        return dto;
    }
}