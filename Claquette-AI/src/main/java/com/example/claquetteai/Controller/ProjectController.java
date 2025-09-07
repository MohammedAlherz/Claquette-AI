package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.CharactersDTOOUT;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllProjects() {
        return ResponseEntity.ok().body(projectService.getAllProjects());
    }

    @PostMapping("/add/company/{companyId}")
    public ResponseEntity<?> addProject(@RequestBody @Valid Project project,
                                                  @PathVariable Integer companyId) {
        projectService.addProject(project, companyId);
        return ResponseEntity.ok().body(new ApiResponse("Project has been added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Integer id,
                                                     @RequestBody @Valid Project updatedProject) {
        projectService.updateProject(id, updatedProject);
        return ResponseEntity.ok().body(new ApiResponse("Project has been updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Integer id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().body(new ApiResponse("Project has been deleted successfully"));
    }


    @GetMapping("/{userId}/my-projects")
    public ResponseEntity<?> myProjects(@PathVariable Integer userId){
        return ResponseEntity.ok(projectService.getProjectById(userId));
    }

    @GetMapping("/{userId}/project-count")
    public ResponseEntity<?> projectsCount(@PathVariable Integer userId){
        return ResponseEntity.ok(projectService.projectsCount(userId).toString());
    }

    @GetMapping("/{userId}/total-budget")
    public ResponseEntity<?> totalBudget(@PathVariable Integer userId){
        return ResponseEntity.ok(projectService.getTotalBudget(userId).toString());
    }

    @GetMapping("/{userId}/project-characters")
    public ResponseEntity<?> projectCharacters(@PathVariable Integer userId, @PathVariable Integer projectId){
        return ResponseEntity.ok(projectService.projectCharacters(userId, projectId));
    }

    @PostMapping("/{userId}/generate-poster/{projectId}")
    public ResponseEntity<?> generateAIPoster(@PathVariable Integer userId, @PathVariable Integer projectId) throws Exception {
        projectService.generateAndAttachPoster(userId, projectId);
        return ResponseEntity.ok(new ApiResponse("poster generated successfully"));
    }

    @GetMapping(value = "/{userId}/project/{projectId}/poster.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> getPosterPng(@PathVariable Integer userId,@PathVariable Integer projectId) {
        return projectService.getPosterPngResponse(userId, projectId);
    }

    @PutMapping("/{userId}/project/{projectId}/poster")
    public ResponseEntity<?> uploadPoster(@PathVariable Integer userId,@PathVariable Integer projectId, @RequestParam("file") MultipartFile file) {
        projectService.uploadPoster(userId,projectId, file);
        return ResponseEntity.ok(new ApiResponse("poster uploaded successfully"));
    }

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<?> getDashboardSummary(@PathVariable Integer userId) {
        Map<String, Object> dashboardData = projectService.getDashboardSummary(userId);
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/{userId}/content/statistics")
    public ResponseEntity<?> getContentStats(@PathVariable Integer userId) {
        Map<String, Object> contentStats = projectService.getContentStats(userId);
        return ResponseEntity.ok(contentStats);
    }
}
