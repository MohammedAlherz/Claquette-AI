package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.CharactersDTOOUT;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponse> projectsCount(@PathVariable Integer userId){
        return ResponseEntity.ok(new ApiResponse(projectService.projectsCount(userId).toString()));
    }

    @GetMapping("/{userId}/total-budget")
    public ResponseEntity<ApiResponse> totalBudget(@PathVariable Integer userId){
        return ResponseEntity.ok(new ApiResponse(projectService.getTotalBudget(userId).toString()));
    }

    @GetMapping("/{userId}/project-characters")
    public ResponseEntity<List<CharactersDTOOUT>> projectCharacters(@PathVariable Integer userId, @PathVariable Integer projectId){
        return ResponseEntity.ok(projectService.projectCharacters(userId, projectId));
    }

}
