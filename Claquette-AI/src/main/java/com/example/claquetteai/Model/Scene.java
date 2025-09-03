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
@Table(name = "scenes")
public class Scene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;



    @NotNull(message = "Scene number cannot be null")
    @Min(value = 1, message = "Scene number must be at least 1")
    @Column(columnDefinition = "int not null")
    private Integer sceneNumber;

    @Size(max = 200, message = "Setting should not exceed 200 characters")
    @Column(columnDefinition = "varchar(200)")
    private String setting;

    @Column(columnDefinition = "text")
    private String actions;

    @Column(columnDefinition = "json")
    private String dialogue;

    @Column(columnDefinition = "json")
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

    @ManyToOne
    @JsonIgnore
    private Film film;
}