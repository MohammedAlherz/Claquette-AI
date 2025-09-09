package com.example.claquetteai.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// Model
@Entity
@Getter
@Setter
public class ProjectGenerationJob {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable=false) private Integer projectId;
    @Column(nullable=false) private Integer userId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private JobStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private JobStep currentStep;
    @Column(nullable=false) private int progress;
    @Column(columnDefinition="text") private String info;
    @Column(columnDefinition="text") private String errorMessage;
    @CreationTimestamp private java.time.LocalDateTime createdAt;
    @UpdateTimestamp private java.time.LocalDateTime updatedAt;
    // getters/setters…
}
