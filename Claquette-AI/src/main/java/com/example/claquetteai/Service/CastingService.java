package com.example.claquetteai.Service;
import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CastingRecommendationDTOOUT;
import com.example.claquetteai.Model.CastingRecommendation;
import com.example.claquetteai.Model.FilmCharacters;
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

    // UPDATED: AI Generation method for One-to-Many relationship
    public Set<CastingRecommendation> generateCasting(Project project) throws Exception {
        // Check if casting already exists for this project
        List<CastingRecommendation> existingCasting = castingRepository.findCastingRecommendationByProject(project);
        if (!existingCasting.isEmpty()) {
            System.out.println("WARNING: Casting recommendations already exist for project " + project.getId());
            System.out.println("Existing casting count: " + existingCasting.size());

            // Delete existing casting to regenerate
            castingRepository.deleteAll(existingCasting);
            System.out.println("Deleted existing casting recommendations for regeneration");
        }

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

        System.out.println("Generated casting recommendations: " + casting.size());

        // Save all casting recommendations
        return new HashSet<>(castingRepository.saveAll(casting));
    }
    // NEW: Generate casting recommendations with character validation (for controller use)
    public void generateCastingRecommendations(Integer userId, Integer projectId) throws Exception {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }

        if (!user.getCompany().getIsSubscribed()) {
            throw new ApiException("you must subscribe to generate casting recommendations");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("project not found");
        }

        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("not authorized");
        }

        // CRITICAL: Check if characters exist first
        if (project.getCharacters() == null || project.getCharacters().isEmpty()) {
            throw new ApiException("No characters found for this project. Please generate characters first before creating recommendations.");
        }

        System.out.println("=== GENERATING CASTING RECOMMENDATIONS ===");
        System.out.println("Project: " + project.getTitle());
        System.out.println("Available Characters: " + project.getCharacters().size());

        // Generate casting recommendations
        generateCasting(project);

        System.out.println("=== CASTING GENERATION COMPLETE ===");
    }
    // UPDATED: Get casting recommendations using existing DTO structure
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
            dto.setProfile(c.getProfile());
            dto.setCharacterName(c.getCharacter() != null ? c.getCharacter().getName() : "Unknown Character");
            dto.setReasoning(c.getReasoning());
            castingRecommendationDTOOUTS.add(dto);
        }
        return castingRecommendationDTOOUTS;
    }

    // UPDATED: Get person details using existing DTO structure
    public CastingRecommendationDTOOUT personDetails(Integer userId, Integer projectId, Integer castingId){
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

        CastingRecommendation castingRecommendation = castingRepository.findCastingRecommendationByProjectAndId(project, castingId);
        if (castingRecommendation == null){
            throw new ApiException("casting recommendation not found");
        }

        CastingRecommendationDTOOUT dto = new CastingRecommendationDTOOUT();
        dto.setName(castingRecommendation.getRecommendedActorName());
        dto.setAge(castingRecommendation.getAge());
        dto.setMatchScore(castingRecommendation.getMatchScore());
        dto.setProfile(castingRecommendation.getProfile());
        dto.setCharacterName(castingRecommendation.getCharacter() != null ? castingRecommendation.getCharacter().getName() : "Unknown Character");
        dto.setReasoning(castingRecommendation.getReasoning());

        return dto;
    }

    // NEW: Get all casting recommendations for a specific character using existing DTO
    public List<CastingRecommendationDTOOUT> getCastingByCharacter(Integer userId, Integer projectId, Integer characterId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("project not found");
        }

        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("not authorised");
        }

        // Find character first
        FilmCharacters character = project.getCharacters().stream()
                .filter(c -> c.getId().equals(characterId))
                .findFirst()
                .orElseThrow(() -> new ApiException("character not found"));

        // Get all casting recommendations for this character
        List<CastingRecommendation> castingList = castingRepository.findCastingRecommendationByCharacter(character);

        List<CastingRecommendationDTOOUT> result = new ArrayList<>();
        for (CastingRecommendation casting : castingList) {
            CastingRecommendationDTOOUT dto = new CastingRecommendationDTOOUT();
            dto.setName(casting.getRecommendedActorName());
            dto.setAge(casting.getAge());
            dto.setMatchScore(casting.getMatchScore());
            dto.setProfile(casting.getProfile());
            dto.setCharacterName(character.getName());
            dto.setReasoning(casting.getReasoning());
            result.add(dto);
        }

        return result;
    }

}