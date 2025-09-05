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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final AiClientService aiClientService;

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


    @Transactional
    public Project generateAndAttachPoster(Integer userId,Integer projectId) throws Exception {
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new IllegalArgumentException("Project not found: " + projectId);

        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }

        String b64 = aiClientService.generatePhoto(project.getDescription()); // you can pass nulls; defaults handled inside
        project.setPosterImageBase64(b64);
        return projectRepository.save(project);
    }

    public ResponseEntity<byte[]> getPosterPngResponse(Integer userId,Integer projectId) {
        Project p = projectRepository.findProjectById(projectId);

        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }

        if (!p.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }

        String b64 = p.getPosterImageBase64();
        if (b64 == null || b64.isBlank()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(b64);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Invalid poster base64 for project " + projectId)
                            .getBytes(StandardCharsets.UTF_8));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(bytes.length);
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"project-" + projectId + "-poster.png\"");
        headers.setETag("\"" + Integer.toHexString(Arrays.hashCode(bytes)) + "\"");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }


    @Transactional
    public void uploadPoster(Integer userId, Integer projectId, MultipartFile file) {
        long MAX_BYTES = 5L * 1024 * 1024; // 5 MB
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("No file uploaded");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("File too large (max 5MB)");

        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        Project project = projectRepository.findProjectById(projectId);
        if (project == null){
            throw new ApiException("project not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }

        byte[] bytes;
        try { bytes = file.getBytes(); }
        catch (IOException e) { throw new RuntimeException("Failed to read upload", e); }

        // quick sanity: ensure decodable image
        try (var in = new ByteArrayInputStream(bytes)) {
            var img = javax.imageio.ImageIO.read(in);
            if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0)
                throw new IllegalArgumentException("Invalid image");
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid image", e);
        }

        String b64 = java.util.Base64.getEncoder().encodeToString(bytes);

        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        p.setPosterImageBase64(b64);      // LONGTEXT Base64 string

        projectRepository.save(p);
    }

    public Project get(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));
    }
}