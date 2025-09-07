package com.example.claquetteai.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CompanySubscription {
    @Id
    private Integer id;

    @NotEmpty(message = "Plan type cannot be null")
    @Pattern(regexp = "FREE|ADVANCED", message = "Plan type must be: FREE or ADVANCED")
    @Column(columnDefinition = "varchar(20) not null")
    private String planType;

    @NotNull(message = "Start date cannot be null")
    @Column(columnDefinition = "datetime not null")
    private LocalDateTime startDate;

    @Column(columnDefinition = "datetime")
    private LocalDateTime endDate;

    @NotEmpty(message = "Status cannot be null")
    @Pattern(regexp = "ACTIVE|EXPIRED|CANCELLED|PENDING|FREE_PLAN",
            message = "Status must be: ACTIVE, EXPIRED, CANCELLED, or PENDING")
    @Column(columnDefinition = "varchar(20) not null")
    private String status;

    @Column(columnDefinition = "datetime")
    private LocalDateTime nextBillingDate;

    @Column(columnDefinition = "decimal(10,2)")
    private Double monthlyPrice; // Can be null for FREE plan


    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;


    @OneToOne
    @MapsId
    @JsonIgnore
    private Company company;

    @OneToOne(mappedBy = "companySubscription", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private Payment payment;
}