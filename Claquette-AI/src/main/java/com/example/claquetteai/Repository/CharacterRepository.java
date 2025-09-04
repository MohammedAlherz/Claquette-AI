package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.FilmCharacters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterRepository extends JpaRepository<FilmCharacters, Integer> {
}
