package com.example.claquetteai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CastingInfoDTOOUT {

    private String name;
    private String profile;
    private Integer age;
    private List<String> previousWork;


}
