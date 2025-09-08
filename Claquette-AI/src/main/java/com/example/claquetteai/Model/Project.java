package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;
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
@Check(name = "budget_positive", constraints = "budget > 0")
@Check(name = "valid_date_range", constraints = "start_project_date < end_project_date")
@Check(name = "valid_project_type", constraints = "project_type IN ('FILM', 'SERIES')")
@Check(name = "valid_status", constraints = "status IN ('IN_DEVELOPMENT', 'IN_PRODUCTION', 'PRE_PRODUCTION', 'COMPLETED', 'DELETED')")
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

    @NotEmpty(message = "Project type cannot be null")
    @Pattern(regexp = "FILM|SERIES",
            message = "Project type must be: FILM or SERIES")
    @Column(columnDefinition = "varchar(10) not null")
    private String projectType;

    @NotEmpty(message = "Genre can not be null")
    @Size(max = 50, message = "Genre should not exceed 50 characters")
    @Column(columnDefinition = "varchar(50)")
    private String genre;

    @NotNull(message = "budget can not be null")
    @Column(columnDefinition = "double")
    private Double budget;

    @NotEmpty(message = "Location cannot be null")
    @Size(min = 3, max = 20, message = "location should be between 3 and 20 characters")
    @Column(columnDefinition = "varchar(20) not null")
    private String location;

    @NotEmpty(message = "Target Audience can not be null")
    @Size(max = 100, message = "Target audience should not exceed 100 characters")
    @Column(columnDefinition = "varchar(100)")
    private String targetAudience;

    @Pattern(regexp = "^$|IN_DEVELOPMENT|IN_PRODUCTION|PRE_PRODUCTION|COMPLETED|DELETED",
            message = "Status must be: IN_DEVELOPMENT, IN_PRODUCTION, PRE_PRODUCTION, COMPLETED, or DELETED")
    @Column(columnDefinition = "varchar(20) not null")
    private String status;


    @NotNull(message = "Start project date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime not null")
    private LocalDateTime startProjectDate;

    @NotNull(message = "End project date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(columnDefinition = "datetime not null")
    private LocalDateTime endProjectDate;

    @Column(columnDefinition = "int not null")
    private Integer episodeCount;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "longtext")
    private String posterImageBase64;

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
    private Set<Episode> episodes;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<FilmCharacters> characters;


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<CastingRecommendation> castingRecommendations;
}