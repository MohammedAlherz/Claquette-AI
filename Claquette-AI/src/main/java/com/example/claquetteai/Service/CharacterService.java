package com.example.claquetteai.Service;
import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.FilmCharactersDTOOUT;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CharacterRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import com.example.claquetteai.Repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // AI Generation method
    public Set<FilmCharacters> generateCharacters(Project project, String storyDescription) throws Exception {
        // Build character generation prompt
        String prompt = promptBuilderService.charactersPrompt(storyDescription);

        // Get AI response and extract characters
        String json = aiClientService.askModel(prompt);
        JsonNode root = new ObjectMapper().readTree(json);
        Set<FilmCharacters> characters = jsonExtractor.extractCharacters(root, project);

        // Save all characters
        return new HashSet<>(characterRepository.saveAll(characters));
    }

    public Integer charactersCount(Integer userId){
        return characterRepository.countFilmCharactersByProject_Company_User_Id(userId);
    }


    public void generateCharacterOnly(Integer userId, Integer projectId) throws Exception {
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        if (!user.getCompany().getIsSubscribed()){
            throw new ApiException("you must subscribe to generate one by one");
        }
        Project project = projectRepository.findProjectById(projectId);
        if (project == null){
            throw new ApiException("project not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorized");
        }
        generateCharacters(project, project.getDescription());
    }

    public List<FilmCharactersDTOOUT> getProjectCharacters(Integer userId, Integer projectId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new ApiException("user not found");
        }

        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ApiException("project not found");
        }

        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("not authorized");
        }

        List<FilmCharacters> characters = characterRepository.findFilmCharactersByProject(project);

        // Convert to DTOs
        return characters.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private FilmCharactersDTOOUT toDTO(FilmCharacters character) {
        FilmCharactersDTOOUT dto = new FilmCharactersDTOOUT();
        dto.setName(character.getName());
        dto.setAge(character.getAge());
        dto.setRoleInStory(character.getRoleInStory());
        dto.setPersonalityTraits(character.getPersonalityTraits());
        dto.setBackground(character.getBackground());
        dto.setCharacterArc(character.getCharacterArc());
        return dto;
    }
}
