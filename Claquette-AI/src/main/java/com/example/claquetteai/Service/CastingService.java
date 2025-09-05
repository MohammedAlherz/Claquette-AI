package com.example.claquetteai.Service;
import com.example.claquetteai.DTO.CastingRecommendationDTOOUT;
import com.example.claquetteai.Model.CastingRecommendation;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.CastingRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

//    public List<CastingRecommendationDTOOUT> castingRecommendations(Integer userId, Integer projectId){
//
//    }

}