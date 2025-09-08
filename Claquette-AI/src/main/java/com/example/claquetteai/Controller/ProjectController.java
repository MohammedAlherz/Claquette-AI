package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.User;  // Your User entity
import com.example.claquetteai.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // ADMIN only - get all projects in system
    @GetMapping("/get")
    public ResponseEntity<?> getAllProjects() {
        return ResponseEntity.ok().body(projectService.getAllProjects());
    }

    // COMPANY only - add project for authenticated company
    @PostMapping("/add")
    public ResponseEntity<?> addProject(@AuthenticationPrincipal User user,
                                        @RequestBody @Valid Project project) {
        // Use authenticated user's company ID instead of path variable
        projectService.addProject(project, user.getCompany().getId());
        return ResponseEntity.ok().body(new ApiResponse("Project has been added successfully"));
    }

    // COMPANY only - update their own projects
    @PutMapping("/update/{projectId}")
    public ResponseEntity<?> updateProject(@AuthenticationPrincipal User user,
                                           @PathVariable Integer projectId,
                                           @RequestBody @Valid Project updatedProject) {
        projectService.updateProject(user.getId(), projectId,updatedProject);
        return ResponseEntity.ok().body(new ApiResponse("Project has been updated successfully"));
    }

    // COMPANY only - delete their own projects
    @DeleteMapping("/delete/{projectId}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal User user,
                                           @PathVariable Integer projectId) {
        projectService.deleteProject(user.getId(), projectId);
        return ResponseEntity.ok().body(new ApiResponse("Project has been deleted successfully"));
    }

    // COMPANY only - get authenticated user's projects
    @GetMapping("/my-projects")
    public ResponseEntity<?> myProjects(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(projectService.getProjectById(user.getId()));
    }

    // COMPANY only - get authenticated user's project count
    @GetMapping("/project-count")
    public ResponseEntity<?> projectsCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(projectService.projectsCount(user.getId()).toString());
    }

    // COMPANY only - get authenticated user's total budget
    @GetMapping("/total-budget")
    public ResponseEntity<?> totalBudget(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(projectService.getTotalBudget(user.getId()).toString());
    }

    // COMPANY only - get project characters (with authorization check)
    @GetMapping("/project/{projectId}/characters")
    public ResponseEntity<?> projectCharacters(@AuthenticationPrincipal User user,
                                               @PathVariable Integer projectId) {
        return ResponseEntity.ok(projectService.projectCharacters(user.getId(), projectId));
    }

    // COMPANY only - generate poster for their project
    @PostMapping("/generate-poster/{projectId}")
    public ResponseEntity<?> generateAIPoster(@AuthenticationPrincipal User user,
                                              @PathVariable Integer projectId) throws Exception {
        projectService.generateAndAttachPoster(user.getId(), projectId);
        return ResponseEntity.ok(new ApiResponse("poster generated successfully"));
    }

    // COMPANY only - get poster for their project
    @GetMapping(value = "/{projectId}/poster.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> getPosterPng(@AuthenticationPrincipal User user,
                                          @PathVariable Integer projectId) {
        return projectService.getPosterPngResponse(user.getId(), projectId);
    }

    // COMPANY only - upload poster for their project
    @PutMapping("/{projectId}/poster")
    public ResponseEntity<?> uploadPoster(@AuthenticationPrincipal User user,
                                          @PathVariable Integer projectId,
                                          @RequestParam("file") MultipartFile file) {
        projectService.uploadPoster(user.getId(), projectId, file);
        return ResponseEntity.ok(new ApiResponse("poster uploaded successfully"));
    }

    // COMPANY only - get dashboard for authenticated user
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardSummary(@AuthenticationPrincipal User user) {
        Map<String, Object> dashboardData = projectService.getDashboardSummary(user.getId());
        return ResponseEntity.ok(dashboardData);
    }

    // COMPANY only - get content stats for authenticated user
    @GetMapping("/content/statistics")
    public ResponseEntity<?> getContentStats(@AuthenticationPrincipal User user) {
        Map<String, Object> contentStats = projectService.getContentStats(user.getId());
        return ResponseEntity.ok(contentStats);
    }

    // COMPANY only - update status of their project
    @PutMapping("/{projectId}/status/{status}")
    public ResponseEntity<ApiResponse> updateProjectStatus(@AuthenticationPrincipal User user,
                                                           @PathVariable Integer projectId,
                                                           @PathVariable String status) {
        projectService.updateProjectStatus(user.getId(), projectId, status);
        return ResponseEntity.ok(new ApiResponse("Project status updated successfully"));
    }
}