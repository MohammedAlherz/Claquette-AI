package com.example.claquetteai.Service;

import com.example.claquetteai.Api.ApiException;
import com.example.claquetteai.DTO.CastingContactDTOIN;
import com.example.claquetteai.DTO.CastingInfoDTOOUT;
import com.example.claquetteai.Model.CastingInfo;
import com.example.claquetteai.Model.CastingRecommendation;
import com.example.claquetteai.Model.User;
import com.example.claquetteai.Repository.CastingInfoRepository;
import com.example.claquetteai.Repository.CastingRecommendationRepository;
import com.example.claquetteai.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CastingInfoService {
    private final CastingInfoRepository castingInfoRepository;
    private final CastingRecommendationRepository castingRecommendationRepository;
    private final UserRepository userRepository;

    private final VerificationEmailService verificationEmailService;


    public CastingInfoDTOOUT showInfo(Integer userId, Integer castingRecommendationId){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("uer not found");
        }

        CastingRecommendation castingRecommendation = castingRecommendationRepository.findCastingRecommendationById(castingRecommendationId);
        if (castingRecommendation == null) {
            throw new ApiException("cast not found");
        }
        if (!user.getCompany().getProjects().contains(castingRecommendation.getProject())){
            throw new ApiException("not authorized");
        }

        List<CastingInfo> castingInfo = castingInfoRepository.findCastingInfosByCastingRecommendation(castingRecommendation);
        if (castingInfo.isEmpty()){
            throw new ApiException("information not found");
        }
        List<String> work = new ArrayList<>();
        for (CastingInfo c : castingInfo){
//            work.add(c.getPreviousWork());
        }
        return new CastingInfoDTOOUT(castingRecommendation.getRecommendedActorName(), castingRecommendation.getProfile(), castingRecommendation.getAge(), work);
    }

    public void contactCast(Integer userId, Integer castingRecommendationId, CastingContactDTOIN castingContactDTOIN){
        User user = userRepository.findUserById(userId);
        if (user == null){
            throw new ApiException("uer not found");
        }

        CastingRecommendation castingRecommendation = castingRecommendationRepository.findCastingRecommendationById(castingRecommendationId);
        if (castingRecommendation == null) {
            throw new ApiException("cast not found");
        }
        if (!user.getCompany().getProjects().contains(castingRecommendation.getProject())){
            throw new ApiException("not authorized");
        }

        CastingInfo castingInfo = castingInfoRepository.findCastingInfoByCastingRecommendation(castingRecommendation);
        if (castingInfo == null){
            throw new ApiException("information not found");
        }

        //send email
        String fullName = castingContactDTOIN.getFullName();
        String senderEmail = castingContactDTOIN.getEmail();
        String phoneNumber = castingContactDTOIN.getPhoneNumber();
        String message = castingContactDTOIN.getMessage();
        // to
        String toEmail = castingInfo.getEmail();

        verificationEmailService.sendContactEmail(toEmail, fullName, senderEmail, phoneNumber, message);
    }


}
