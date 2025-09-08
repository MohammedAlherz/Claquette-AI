package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.Film;
import com.example.claquetteai.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FilmRepository extends JpaRepository<Film, Integer> {
    Film findFilmByProject(Project project);

    List<Film> findFilmsByProjectIn(Collection<Project> projects);
}
