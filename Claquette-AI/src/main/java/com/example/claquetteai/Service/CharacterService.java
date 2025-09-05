package com.example.claquetteai.Service;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Repository.CharacterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CharacterService {
    private final CharacterRepository characterRepository;
    private final JsonExtractor jsonExtractor;
    private final PromptBuilderService promptBuilderService;
    private final AiClientService aiClientService;

    // Regular CRUD operations
    public FilmCharacters createCharacter(FilmCharacters character) {
        return characterRepository.save(character);
    }

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

}
