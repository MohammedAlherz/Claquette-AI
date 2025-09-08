package com.example.claquetteai.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Check(constraints = "role IN ('ADMIN', 'COMPANY')")
public class User implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty(message = "full name can not be null")
    @Size(min = 4, max = 50, message = "Full name should be between 4 and 50 characters")
    @Column(columnDefinition = "varchar(50) unique not null")
    private String fullName;

    @NotEmpty(message = "Email should not be null")
    @Email(message = "Email should be valid email")
    @Column(columnDefinition = "varchar(255) unique not null")
    private String email;

    @NotEmpty(message = "Password can not be null")
    @Size(min = 8, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.")
    @Column(columnDefinition = "varchar(255) not null")
    private String password;

    @Pattern(
            regexp = "^(ADMIN|COMPANY)$",
            message = "Role must be one of: ADMIN, USER, MODERATOR"
    )
    @Column(columnDefinition = "varchar(10) not null")
    private String role;


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

    @Column(nullable = false)
    private boolean activeAccount = false;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private Company company;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getUsername() {
        return email;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

