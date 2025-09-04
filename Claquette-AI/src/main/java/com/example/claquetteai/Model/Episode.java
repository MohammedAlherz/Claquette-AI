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
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Episode number cannot be null")
    @Min(value = 1, message = "Episode number must be at least 1")
    @Column(columnDefinition = "int not null")
    private Integer episodeNumber;

    @NotEmpty(message = "Title cannot be null")
    @Size(min = 1, max = 200, message = "Title should be between 1 and 200 characters")
    @Column(columnDefinition = "varchar(200) not null")
    private String title;

    @Column(columnDefinition = "text")
    private String summary;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 500, message = "Duration should not exceed 500 minutes")
    @Column(columnDefinition = "int")
    private Integer durationMinutes;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;


    @ManyToOne
    @JsonIgnore
    private Project project;

    // 🟢 Add OneToMany link with Scene
    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Scene> scenes;
}