package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.FilmCharacters;
import com.example.claquetteai.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<FilmCharacters, Integer> {
    Integer countFilmCharactersByProject_Company_User_Id(Integer projectCompanyUserId);

    List<FilmCharacters> findFilmCharactersByProject(Project project);
}
