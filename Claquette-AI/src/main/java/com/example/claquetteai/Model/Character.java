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

@Table(name = "characters") // Use plural or different name
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;



    @NotEmpty(message = "Character name cannot be null")
    @Size(min = 1, max = 100, message = "Character name should be between 1 and 100 characters")
    @Column(columnDefinition = "varchar(100) not null")
    private String name;

    @Min(value = 0, message = "Age must be non-negative")
    @Max(value = 200, message = "Age should not exceed 200")
    @Column(columnDefinition = "int")
    private Integer age;

    @Size(max = 200, message = "Role in story should not exceed 200 characters")
    @Column(columnDefinition = "varchar(200)")
    private String roleInStory;

    @Size(max = 500, message = "Personality traits should not exceed 500 characters")
    @Column(columnDefinition = "varchar(500)")
    private String personalityTraits;

    @Size(max = 1000, message = "Background should not exceed 1000 characters")
    @Column(columnDefinition = "varchar(1000)")
    private String background;

    @Size(max = 1000, message = "Character arc should not exceed 1000 characters")
    @Column(columnDefinition = "varchar(1000)")
    private String characterArc;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @ManyToOne
    @JsonIgnore
    private Project project;
}
