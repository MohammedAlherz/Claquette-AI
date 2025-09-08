package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class Company {
    @Id
    private Integer id;


    @NotEmpty(message = "Name should not be empty")
    @Size(min = 2, max = 200, message = "Company name must be between 2 and 200 characters")
    @Column(columnDefinition = "varchar(200) not null")
    private String name;

    @NotEmpty(message = "Registration number should not be empty")
    @Size(min = 10, max = 10, message = "Commercial registration number must be exactly 10 digits")
    @Column(columnDefinition = "varchar(10) unique not null")
    private String commercialRegNo;

    private Boolean isSubscribed = false;

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
    private Set<Project> projects;
}