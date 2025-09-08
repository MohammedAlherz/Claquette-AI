package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.Episode;
import com.example.claquetteai.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Integer> {
    List<Episode> findEpisodesByProject(Project project);

    Episode findEpisodeByProject(Project project);

    // Add this method to find episodes by project ID
    List<Episode> findByProjectId(Integer projectId);

    // Optional: Add method to find episodes by project ID and order by episode number
    List<Episode> findByProjectIdOrderByEpisodeNumber(Integer projectId);

    List<Episode> findEpisodesByProjectIn(Collection<Project> projects);
}
