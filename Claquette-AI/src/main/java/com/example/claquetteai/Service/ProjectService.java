package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CharactersDTOOUT;
import com.example.claquetteai.DTO.ProjectDTOOUT;
import com.example.claquetteai.Model.Company;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.*;
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
    private final SceneRepository sceneRepository;
    private final AiInteractionService aiInteractionService;

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
        if(project.getProjectType().equals("FILM")){
            project.setEpisodeCount(1);
        }
        project.setStatus("IN_DEVELOPMENT");
        projectRepository.save(project);
    }

    public void updateProject(Integer userId,Integer projectId,Project updatedProject) {
        User user = userRepository.findUserById(userId);
        if(user == null){
            throw new ApiException("User not found");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("Project not found");
        }
        if(!project.getCompany().getUser().equals(user)){
            throw new ApiException("Not Authorized");
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
        if(project.getProjectType().equals("FILM")){
            project.setEpisodeCount(0);
        }else{
            project.setEpisodeCount(updatedProject.getEpisodeCount());
        }
        projectRepository.save(project);
    }

    public void deleteProject(Integer userId,Integer projectId) {
        User user = userRepository.findUserById(userId);
        if(user == null){
            throw new ApiException("User not found");
        }
        Project project = projectRepository.findProjectById(projectId);

        if (project == null) {
            throw new ApiException("Project not found");
        }

        if(!project.getCompany().getUser().equals(user)){
            throw new ApiException("Not Authorized");
        }
        projectRepository.delete(project);
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


    public Map<String, Object> getDashboardSummary(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        // Get all projects for the user
        List<Project> projects = projectRepository.findProjectsByCompany_User_Id(userId);

        // Calculate statistics
        Integer totalProjects = projects.size();
        Double totalBudget = projects.stream()
                .mapToDouble(Project::getBudget)
                .sum();

        // Get project status breakdown
        Map<String, Long> projectStatusCounts = projects.stream()
                .collect(Collectors.groupingBy(
                        Project::getStatus,
                        Collectors.counting()
                ));

        // Get characters count across all projects
        Integer totalCharacters = projects.stream()
                .mapToInt(project -> characterRepository.findFilmCharactersByProject(project).size())
                .sum();

        // Get projects
        List<ProjectDTOOUT> recentProjects = projects.stream()
                .sorted((p1, p2) -> {
                    // Sort by startProjectDate descending (most recent first)
                    if (p1.getStartProjectDate() == null && p2.getStartProjectDate() == null) return 0;
                    if (p1.getStartProjectDate() == null) return 1;
                    if (p2.getStartProjectDate() == null) return -1;
                    return p2.getStartProjectDate().compareTo(p1.getStartProjectDate());
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Count different project types
        Map<String, Long> projectTypeCounts = projects.stream()
                .collect(Collectors.groupingBy(
                        Project::getProjectType,
                        Collectors.counting()
                ));

        // Count projects depends on the status
        long activeProjects = projects.stream()
                .filter(p -> "IN_DEVELOPMENT".equals(p.getStatus()) ||
                        "IN_PRODUCTION".equals(p.getStatus()) ||
                        "PRE_PRODUCTION".equals(p.getStatus()))
                .count();

        // Build dashboard summary response
        Map<String, Object> dashboardSummary = new HashMap<>();

        // Main statistics
        dashboardSummary.put("totalProjects", totalProjects);
        dashboardSummary.put("totalBudget", totalBudget);
        dashboardSummary.put("totalCharacters", totalCharacters);
        dashboardSummary.put("activeProjects", activeProjects);

        // Detailed breakdowns
        dashboardSummary.put("projectStatusBreakdown", projectStatusCounts);
        dashboardSummary.put("projectTypeBreakdown", projectTypeCounts);

        // Recent data
        dashboardSummary.put("recentProjects", recentProjects);

        // Content statistics for the "المحتوى المنشئ" section
        Map<String, Object> contentStats = new HashMap<>();
        contentStats.put("seriesCount", projectTypeCounts.getOrDefault("SERIES", 0L));
        contentStats.put("movieCount", projectTypeCounts.getOrDefault("MOVIE", 0L));
        contentStats.put("totalCharacters", totalCharacters);
        dashboardSummary.put("contentStatistics", contentStats);

        return dashboardSummary;
    }


    // Updated getContentStats method with proper scene counting
    public Map<String, Object> getContentStats(Integer userId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found with id " + userId);
        }

        List<Project> projects = projectRepository.findProjectsByCompany_User_Id(userId);

        Map<String, Long> projectTypeCounts = projects.stream()
                .collect(Collectors.groupingBy(
                        Project::getProjectType,
                        Collectors.counting()
                ));

        Integer totalCharacters = projects.stream()
                .mapToInt(project -> characterRepository.findFilmCharactersByProject(project).size())
                .sum();

        Integer totalEpisodes = projects.stream()
                .mapToInt(Project::getEpisodeCount)
                .sum();

        // Count scenes across all projects (through episodes)
        Integer totalScenes = projects.stream()
                .flatMap(project -> project.getEpisodes().stream())
                .mapToInt(episode -> sceneRepository.findSceneByEpisode(episode).size())
                .sum();


        Map<String, Object> contentStats = new HashMap<>();

        contentStats.put("seriesCount", projectTypeCounts.getOrDefault("SERIES", 0L));
        contentStats.put("filmCount", projectTypeCounts.getOrDefault("FILM", 0L));
        contentStats.put("totalCharacters", totalCharacters);
        contentStats.put("totalEpisodes", totalEpisodes);
        contentStats.put("totalScenes", totalScenes);

        Map<String, Long> contentByStatus = projects.stream()
                .collect(Collectors.groupingBy(
                        Project::getStatus,
                        Collectors.counting()
                ));
        contentStats.put("contentByStatus", contentByStatus);

        Map<String, Long> genreCounts = projects.stream()
                .filter(p -> p.getGenre() != null && !p.getGenre().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Project::getGenre,
                        Collectors.counting()
                ));
        contentStats.put("genreBreakdown", genreCounts);

        Map<Integer, Long> projectsByYear = projects.stream()
                .filter(p -> p.getStartProjectDate() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getStartProjectDate().getYear(),
                        Collectors.counting()
                ));
        contentStats.put("projectsByYear", projectsByYear);

        Double totalContentBudget = projects.stream()
                .mapToDouble(Project::getBudget)
                .sum();
        contentStats.put("totalContentBudget", totalContentBudget);

        Map<String, Double> avgBudgetByType = projects.stream()
                .collect(Collectors.groupingBy(
                        Project::getProjectType,
                        Collectors.averagingDouble(Project::getBudget)
                ));
        contentStats.put("averageBudgetByType", avgBudgetByType);

        return contentStats;
    }

    public void updateProjectStatus(Integer userId, Integer projectId, String status) {
        // Validate user exists
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("User not found");
        }

        // Validate project exists
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("Project not found");
        }

        // Check authorization
        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("Not authorized to update this project");
        }

        // Update project status
        project.setStatus(status);
        projectRepository.save(project);
    }
}