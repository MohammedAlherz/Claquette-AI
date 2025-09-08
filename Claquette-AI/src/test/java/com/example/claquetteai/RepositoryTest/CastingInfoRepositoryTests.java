package com.example.claquetteai.RepositoryTest;

import com.example.claquetteai.Model.CastingInfo;
import com.example.claquetteai.Model.CastingRecommendation;
import com.example.claquetteai.Repository.CastingInfoRepository;
import com.example.claquetteai.Repository.CastingRecommendationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CastingInfoRepositoryTests {

    @Autowired
    private CastingInfoRepository castingInfoRepository;

    @Autowired
    private CastingRecommendationRepository castingRecommendationRepository;

    private CastingRecommendation recommendation;

    @BeforeEach
    void setUp() {
        recommendation = new CastingRecommendation();
        recommendation.setAge(30);
        recommendation.setRecommendedActorName("John Doe");
        recommendation.setReasoning("Great actor for action roles");
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setUpdatedAt(LocalDateTime.now());

        // Save parent entity first (needed because CastingInfo maps its ID to this one)
        recommendation = castingRecommendationRepository.save(recommendation);
    }

    @Test
    void testFindCastingInfoByCastingRecommendation() {
        // given
        CastingInfo info = new CastingInfo();
        info.setRole("Hero");
        info.setEmail("hero@example.com");
        info.setTypeOfWork("Film");
        info.setPreviousWork(Collections.singletonList("Action Movie"));
        info.setCastingRecommendation(recommendation);

        castingInfoRepository.save(info);

        // when
        CastingInfo found = castingInfoRepository.findCastingInfoByCastingRecommendation(recommendation);

        // then
        Assertions.assertThat(found).isNotNull();
        Assertions.assertThat(found.getEmail()).isEqualTo("hero@example.com");
        Assertions.assertThat(found.getPreviousWork()).contains("Action Movie");
    }


}
