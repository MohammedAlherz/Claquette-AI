package com.example.claquetteai.Service;
import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CastingRecommendationDTOOUT;
import com.example.claquetteai.Model.CastingRecommendation;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CastingRecommendationRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class CastingService {
    private final CastingRecommendationRepository castingRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // Regular CRUD operations
    public CastingRecommendation createCasting(CastingRecommendation casting) {
        return castingRepository.save(casting);
    }

    // AI Generation method
    public Set<CastingRecommendation> generateCasting(Project project) throws Exception {
        // Build simple project context string
        String projectInfo = String.format(
                "Project: %s, Type: %s, Description: %s",
                project.getTitle(),
                project.getProjectType(),
                project.getDescription()
        );

        // Build casting prompt with project context
        String prompt = promptBuilderService.castingPrompt(projectInfo);

        // Get AI response and extract casting recommendations
        String json = aiClientService.askModel(prompt);
        Set<CastingRecommendation> casting = jsonExtractor.extractCasting(json, project);

        // Save all casting recommendations
        return new HashSet<>(castingRepository.saveAll(casting));
    }

    public List<CastingRecommendationDTOOUT> castingRecommendations(Integer userId, Integer projectId){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        Project project = projectRepository.findProjectById(projectId);
        if (project==null){
            throw new ApiException("project not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }
        List<CastingRecommendation> castingRecommendations = castingRepository.findCastingRecommendationByProject(project);
        List<CastingRecommendationDTOOUT> castingRecommendationDTOOUTS = new ArrayList<>();
        for (CastingRecommendation c : castingRecommendations){
            CastingRecommendationDTOOUT dto = new CastingRecommendationDTOOUT();
            dto.setName(c.getRecommendedActorName());
            dto.setAge(c.getAge());
            dto.setMatchScore(c.getMatchScore());
            dto.setProfile(c.getCharacterName());
            castingRecommendationDTOOUTS.add(dto);
        }
        return castingRecommendationDTOOUTS;
    }

    public CastingRecommendationDTOOUT personDetails(Integer userId,Integer projectId, Integer personId){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        Project project = projectRepository.findProjectById(projectId);
        if (project==null){
            throw new ApiException("project not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }
        CastingRecommendation castingRecommendations = castingRepository.findCastingRecommendationByProjectAndId(project, personId);
        if (castingRecommendations == null){
            throw new ApiException("not found");
        }
        return new CastingRecommendationDTOOUT(castingRecommendations.getRecommendedActorName(), castingRecommendations.getAge(), castingRecommendations.getMatchScore(), castingRecommendations.getProfile());
    }

}