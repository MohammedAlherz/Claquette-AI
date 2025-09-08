package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.Film;
import com.example.claquetteai.Model.Project;
import com.example.claquetteai.Model.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Integer> {
    List<Scene> findSceneByEpisode(Episode episode);

    List<Scene> findSceneByFilm_Project(Project filmProject);

    Scene findSceneById(Integer id);

    List<Scene> findScenesByEpisode(Episode episode);
}
