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
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "full name can not be null")
    @Size(min = 4, max = 20, message = "full name should be between 4 and 10 character")
    @Column(columnDefinition = "varchar(10) unique not null")
    private String fullName;

    @NotEmpty(message = "Email should not be null")
    @Email(message = "Email should be valid email")
    @Column(columnDefinition = "varchar(255) unique not null")
    private String email;

    @NotEmpty(message = "Password can not be null")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    @Column(columnDefinition = "varchar(20) not null")
    private String password;

    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGTEXT")
    private String profileImageBase64;

    @Column(length = 64)
    private String profileImageContentType;

    private Integer useAI = 1;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private Company company;

}

