package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Integer> {
    List<Episode> findEpisodesByProject(Project project);

    Episode findEpisodeByProject(Project project);
}
