package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "Company name cannot be null")
    @Size(min = 2, max = 200, message = "Company name should be between 2 and 200 characters")
    @Column(columnDefinition = "varchar(200) not null")
    private String name;

    @NotEmpty(message = "Commercial registration number cannot be null")
    @Size(max = 50, message = "Commercial registration number should not exceed 50 characters")
    @Column(columnDefinition = "varchar(50) unique not null")
    private String commercialRegNo;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;


    @OneToOne
    @MapsId
    @JsonIgnore
    private User user;

    // One-to-One: A company has one active subscription at a time
    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private CompanySubscription activeSubscription;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Project> projects;
}