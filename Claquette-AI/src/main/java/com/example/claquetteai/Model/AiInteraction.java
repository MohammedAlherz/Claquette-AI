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
public class AiInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(columnDefinition = "text")
    private String projectContext;

    @NotNull(message = "AI response cannot be null")
    @Column(columnDefinition = "json not null")
    private String aiResponse;

    @Column(columnDefinition = "varchar(50)")
    private String interactionType; // e.g., "SCRIPT_GENERATION", "CHARACTER_DEVELOPMENT", "SCENE_ANALYSIS"

    @Column(columnDefinition = "int default 0")
    private Integer creditsUsed; // How many AI credits this interaction consumed

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
}
