package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "int")
    private Integer sceneNumber;

    @Column(columnDefinition = "text")
    private String setting;

    @Column(columnDefinition = "text")
    private String actions;

    @Column(columnDefinition = "text")
    private String dialogue;

    @Column(columnDefinition = "text")
    private String departmentNotes;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @ManyToOne
    @JsonIgnore
    private Episode episode;

    // CRITICAL FIX: Add proper cascade and fetch configuration
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "scene_characters",
            joinColumns = @JoinColumn(name = "scenes_id"), // This should match your actual column name
            inverseJoinColumns = @JoinColumn(name = "characters_id") // This should match your actual column name
    )
    @JsonIgnore
    private Set<FilmCharacters> characters;

    @ManyToOne
    @JsonIgnore
    private Film film;
}