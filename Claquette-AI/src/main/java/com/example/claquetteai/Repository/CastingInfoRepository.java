package com.example.claquetteai.Repository;

import com.example.claquetteai.Model.CastingInfo;
import com.example.claquetteai.Model.CastingRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CastingInfoRepository extends JpaRepository<CastingInfo, Integer> {
    CastingInfo findCastingInfoByCastingRecommendation(CastingRecommendation castingRecommendation);

    List<CastingInfo> findCastingInfosByCastingRecommendation(CastingRecommendation castingRecommendation);
}
