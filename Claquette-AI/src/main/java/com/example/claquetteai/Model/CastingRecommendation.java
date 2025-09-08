package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CastingRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Recommended actor name cannot be null")
    @Size(min = 1, max = 100, message = "Recommended actor name should be between 1 and 100 characters")
    @Column(columnDefinition = "varchar(100) not null")
    private String recommendedActorName;

    @Column(columnDefinition = "text")
    private String reasoning;

    @Size(max = 500, message = "profile should not exceed 500 characters")
    @Column(columnDefinition = "varchar(500)")
    private String profile;

    @Column(columnDefinition = "decimal(3,2)")
    private Double matchScore; // How well the actor matches the character (0-1)

    @Column(columnDefinition = "int not null")
    private Integer age;

    // NEW: Add priority/ranking for multiple recommendations
    @Column(columnDefinition = "int default 1")
    private Integer priority = 1; // 1 = highest priority

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    // CORRECTED: Many-to-One relationship with FilmCharacters
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", referencedColumnName = "id")
    @JsonIgnore
    private FilmCharacters character;

    @ManyToOne
    @JsonIgnore
    private Project project;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "castingRecommendation")
    @JoinColumn
    private CastingInfo castingInfo;
}