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
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @NotEmpty(message = "Title cannot be null")
    @Size(min = 1, max = 200, message = "Title should be between 1 and 200 characters")
    @Column(columnDefinition = "varchar(200) not null")
    private String title;

    @Column(columnDefinition = "text")
    private String summary;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1000, message = "Duration should not exceed 1000 minutes")
    @Column(columnDefinition = "int")
    private Integer durationMinutes;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;


    @OneToOne
    @MapsId
    @JsonIgnore
    private Project project;

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Scene> scenes;
}
