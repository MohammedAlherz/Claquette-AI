package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.SceneDTOIN;
import com.example.claquetteai.DTO.SceneDTOOUT;
import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.Scene;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.EpisodeRepository;
import com.example.claquetteai.Repository.ProjectRepository;
import com.example.claquetteai.Repository.SceneRepository;
import com.example.claquetteai.Repository.UserRepository;
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

    public List<SceneDTOOUT> scenes(Integer userId, Integer projectId){
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
        Episode episode = episodeRepository.findEpisodeByProject(project);
        if (episode == null){
            throw new ApiException("episode not found");
        }
        List<Scene> scenes = sceneRepository.findSceneByFilm_Project(project);
        List<SceneDTOOUT> sceneDTOOUTS = new ArrayList<>();
        for (Scene s : scenes){
            SceneDTOOUT dto = new SceneDTOOUT(s.getDialogue());
            sceneDTOOUTS.add(dto);
        }
        return sceneDTOOUTS;
    }

    public Integer characterScene(Integer userId, Integer projectId){
        return scenes(userId,projectId).size();
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
