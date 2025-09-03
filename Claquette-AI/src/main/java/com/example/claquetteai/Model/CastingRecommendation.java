package com.example.claquetteai.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CastingRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Character name cannot be null")
    @Size(min = 1, max = 100, message = "Character name should be between 1 and 100 characters")
    @Column(columnDefinition = "varchar(100) not null")
    private String characterName;

    @NotEmpty(message = "Recommended actor name cannot be null")
    @Size(min = 1, max = 100, message = "Recommended actor name should be between 1 and 100 characters")
    @Column(columnDefinition = "varchar(100) not null")
    private String recommendedActorName;

    @Column(columnDefinition = "text")
    private String reasoning;

    @Size(max = 500, message = "Known for should not exceed 500 characters")
    @Column(columnDefinition = "varchar(500)")
    private String knownFor;

    @Column(columnDefinition = "decimal(3,2)")
    private Double matchScore; // How well the actor matches the character (0-1)

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @ManyToOne
    @JsonIgnore
    private Project project;
}
