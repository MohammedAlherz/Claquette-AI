package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FilmCharacters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "FilmCharacters name cannot be null")
    @Size(min = 1, max = 100, message = "FilmCharacters name should be between 1 and 100 characters")
    @Column(columnDefinition = "varchar(100) not null")
    private String name;

    @Min(value = 0, message = "Age must be non-negative")
    @Max(value = 200, message = "Age should not exceed 200")
    @Column(columnDefinition = "int")
    private Integer age;

    @Size(max = 200, message = "Role in story should not exceed 200 characters")
    @Column(columnDefinition = "varchar(200)")
    private String roleInStory;

    @Column(columnDefinition = "text")
    private String personalityTraits;

    @Column(columnDefinition = "text")
    private String background;

    @Size(max = 1000, message = "FilmCharacters arc should not exceed 1000 characters")
    @Column(columnDefinition = "text")
    private String characterArc;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "characters", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Scene> scenes;

    @ManyToOne
    @JsonIgnore
    private Project project;

    // CORRECTED: One-to-Many relationship with CastingRecommendation
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<CastingRecommendation> castingRecommendations;
}