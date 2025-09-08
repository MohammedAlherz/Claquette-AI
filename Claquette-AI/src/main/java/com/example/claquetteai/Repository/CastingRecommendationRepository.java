package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.CastingRecommendation;
import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CastingRecommendationRepository extends JpaRepository<CastingRecommendation, Integer> {
    List<CastingRecommendation> findCastingRecommendationByProject(Project project);

    CastingRecommendation findCastingRecommendationByProjectAndId(Project project, Integer id);

    List<CastingRecommendation> findCastingRecommendationByCharacter(FilmCharacters character);

    CastingRecommendation findCastingRecommendationById(Integer id);
}
