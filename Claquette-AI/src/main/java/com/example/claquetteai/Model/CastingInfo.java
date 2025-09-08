package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CastingInfo {

    @Id
    private Integer id;

    //ايميل التواصل
    private String email;
    //اسم العمل
    private List<String> previousWork;
    //فلم او مسلسل
    private String typeOfWork;
    //البطل او شخصية مساندة
    private String role;

    @OneToOne
    @MapsId
    @JsonIgnore
    private CastingRecommendation castingRecommendation;
}
