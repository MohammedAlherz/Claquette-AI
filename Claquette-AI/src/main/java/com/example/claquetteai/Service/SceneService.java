package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.SceneDTOIN;
import com.example.claquetteai.DTO.SceneDTOOUT;
import com.example.claquetteai.Model.*;
import com.example.claquetteai.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** Read scenes for a project (film or series) as lightweight DTOs. */
    @Transactional(readOnly = true)
    public List<SceneDTOOUT> getScenes(Integer userId, Integer projectId) {
        // AuthZ + ownership
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("project not found");

        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("user not found");

        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("not authorised");
        }

        List<SceneDTOOUT> sceneDTOs = new ArrayList<>();

        if ("FILM".equals(project.getProjectType())) {
            Film film = filmRepository.findFilmByProject(project);
            if (film == null) throw new ApiException("Film not found");

            // Fetch scenes by project (your existing repo call)
            List<Scene> scenes = sceneRepository.findSceneByFilm_Project(project);
            for (Scene s : scenes) {
                // For films, episode info is null
                sceneDTOs.add(new SceneDTOOUT(safe(s.getDialogue()), null, null));
            }
        } else {
            // SERIES: gather across episodes
            List<Episode> episodes = episodeRepository.findEpisodesByProject(project);
            if (episodes == null || episodes.isEmpty()) {
                throw new ApiException("episode not found");
            }

            for (Episode ep : episodes) {
                List<Scene> episodeScenes = sceneRepository.findScenesByEpisode(ep);
                for (Scene s : episodeScenes) {
                    sceneDTOs.add(new SceneDTOOUT(
                            safe(s.getDialogue()),
                            ep.getEpisodeNumber(),
                            safe(ep.getTitle())
                    ));
                }
            }
        }

        return sceneDTOs;
    }

    /** Simple count of characters on a project (used by your dashboards). */
    @Transactional(readOnly = true)
    public Integer characterScene(Integer userId, Integer projectId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("project not found");

        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("user not found");

        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("not authorised");
        }

        List<FilmCharacters> characters = characterRepository.findFilmCharactersByProject(project);
        return characters == null ? 0 : characters.size();
    }

    /** Update a single scene (currently only dialogue in your DTO). */
    @Transactional
    public void updateScene(Integer userId, Integer projectId, Integer sceneId, SceneDTOIN sceneDTOIN) {
        // Validate inputs
        if (sceneDTOIN == null) throw new ApiException("invalid request");
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) throw new ApiException("project not found");

        User user = userRepository.findUserById(userId);
        if (user == null) throw new ApiException("user not found");

        if (!project.getCompany().getUser().equals(user)) {
            throw new ApiException("not authorised");
        }

        Scene oldScene = sceneRepository.findSceneById(sceneId);
        if (oldScene == null) throw new ApiException("scene not found");

        // Extra safety: ensure scene belongs to the same project
        if (!belongsToProject(oldScene, project)) {
            throw new ApiException("scene does not belong to this project");
        }

        // Apply updates (keep the scope to what your DTO currently carries)
        oldScene.setDialogue(sceneDTOIN.getDialogue());
        sceneRepository.save(oldScene);
    }

    // ====================== Helpers ======================

    private boolean belongsToProject(Scene s, Project p) {
        // Film scene
        if (s.getFilm() != null && s.getFilm().getProject() != null) {
            return p.equals(s.getFilm().getProject());
        }
        // Episode scene
        if (s.getEpisode() != null && s.getEpisode().getProject() != null) {
            return p.equals(s.getEpisode().getProject());
        }
        // If neither is set, it’s an inconsistent scene
        return false;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
