package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.AiInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiInteractionRepository extends JpaRepository<AiInteraction, Integer> {
}
