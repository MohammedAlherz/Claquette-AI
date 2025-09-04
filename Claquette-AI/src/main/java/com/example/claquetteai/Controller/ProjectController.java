package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
