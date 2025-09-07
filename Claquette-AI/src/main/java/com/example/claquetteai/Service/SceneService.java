package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.SceneDTOIN;
import com.example.claquetteai.DTO.SceneDTOOUT;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SceneService {
    private final SceneRepository sceneRepository;
    private final EpisodeRepository episodeRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final CharacterRepository characterRepository;

    public List<SceneDTOOUT> getScenes(Integer userId, Integer projectId){
        Project project = projectRepository.findProjectById(projectId);
        List<Scene> scenes = new ArrayList<>();
        if (project == null){
            throw new ApiException("project not found");
        }
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }

        List<SceneDTOOUT> sceneDTOOUTS = new ArrayList<>();

        if(project.getProjectType().equals("FILM")){
            Film film = filmRepository.findFilmByProject(project);
            if(film == null){
                throw new ApiException("Film not found");
            }
            scenes = sceneRepository.findSceneByFilm_Project(project);

            // For film projects, episode info will be null
            for (Scene s : scenes){
                SceneDTOOUT dto = new SceneDTOOUT(s.getDialogue(), null, null);
                sceneDTOOUTS.add(dto);
            }
        }else{
            // Handle TV series with episodes
            List<Episode> episodes = episodeRepository.findEpisodesByProject(project);
            if (episodes.isEmpty()){
                throw new ApiException("episode not found");
            }

            // Get scenes from all episodes with episode information
            for(Episode episode : episodes) {
                List<Scene> episodeScenes = sceneRepository.findScenesByEpisode(episode);
                for (Scene s : episodeScenes){
                    SceneDTOOUT dto = new SceneDTOOUT(
                            s.getDialogue(),
                            episode.getEpisodeNumber(),
                            episode.getTitle()
                    );
                    sceneDTOOUTS.add(dto);
                }
            }
        }

        return sceneDTOOUTS;
    }

    public Integer characterScene(Integer userId, Integer projectId){
        Project project = projectRepository.findProjectById(projectId);
        if (project == null){
            throw new ApiException("project not found");
        }
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }
        List<FilmCharacters> characters = characterRepository.findFilmCharactersByProject(project);
        return characters.size();
    }

    public void updateScene(Integer userId, Integer projectId, Integer sceneId, SceneDTOIN sceneDTOIN){
        Project project = projectRepository.findProjectById(projectId);
        if (project == null){
            throw new ApiException("project not found");
        }
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("user not found");
        }
        if (!project.getCompany().getUser().equals(user)){
            throw new ApiException("not authorised");
        }
        Scene oldScene = sceneRepository.findSceneById(sceneId);
        if (oldScene == null){
            throw new ApiException("scene not found");
        }

        oldScene.setDialogue(sceneDTOIN.getDialogue());
        sceneRepository.save(oldScene);
    }


}
