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

    @ManyToMany
    @JsonIgnore
    private Set<FilmCharacters> characters;
    @ManyToOne
    @JsonIgnore
    private Episode episode;

    @ManyToOne
    @JsonIgnore
    private Film film;  // Add this field
}