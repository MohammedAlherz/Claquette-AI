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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //ايميل التواصل
    private String email;
    //اسم العمل
    private String previousWork;
    //فلم او مسلسل
    private String typeOfWork;
    //البطل او شخصية مساندة
    private String role;

    @ManyToOne
    @JsonIgnore
    private CastingRecommendation castingRecommendation;
}
