package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Title cannot be null")
    @Size(min = 1, max = 200, message = "Title should be between 1 and 200 characters")
    @Column(columnDefinition = "varchar(200) not null")
    private String title;

    @Size(max = 1000, message = "Description should not exceed 1000 characters")
    @Column(columnDefinition = "varchar(1000)")
    private String description;

    @Column(columnDefinition = "text")
    private String scenario;

    @NotEmpty(message = "Project type cannot be null")
    @Pattern(regexp = "FILM|SERIES",
            message = "Project type must be: FILM or SERIES")
    @Column(columnDefinition = "varchar(10) not null")
    private String projectType;

    @Size(max = 50, message = "Genre should not exceed 50 characters")
    @Column(columnDefinition = "varchar(50)")
    private String genre;

    @NotNull(message = "budget can not be null")
    @Column(columnDefinition = "double")
    private Double budget;

    @Size(max = 200, message = "Location should not exceed 200 characters")
    @Column(columnDefinition = "varchar(200)")
    private String location;

    @Size(max = 100, message = "Target audience should not exceed 100 characters")
    @Column(columnDefinition = "varchar(100)")
    private String targetAudience;

    @NotEmpty(message = "Status cannot be null")
    @Pattern(regexp = "IN_DEVELOPMENT|IN_PRODUCTION|PRE_PRODUCTION|COMPLETED|DELETED",
            message = "Status must be: IN_DEVELOPMENT, IN_PRODUCTION, PRE_PRODUCTION, COMPLETED, or DELETED")
    @Column(columnDefinition = "varchar(20) not null")
    private String status;

    @NotNull(message = "Start project date cannot be null")
    @Column(columnDefinition = "datetime not null")
    private LocalDateTime startProjectDate;

    @NotNull(message = "End project date cannot be null")
    @Column(columnDefinition = "datetime not null")
    private LocalDateTime endProjectDate;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @ManyToOne
    @JsonInclude
    private Company company;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private Film films;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Episode> episodes;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Character> characters;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<AiInteraction> aiInteractions;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<CastingRecommendation> castingRecommendations;
}