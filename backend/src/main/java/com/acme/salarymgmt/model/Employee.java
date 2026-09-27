package com.acme.salarymgmt.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_code", columnList = "employeeCode", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_department", columnList = "department"),
        @Index(name = "idx_country", columnList = "country"),
        @Index(name = "idx_is_active", columnList = "isActive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String employeeCode;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String department;

    @Column(nullable = false, length = 50)
    private String designation;

    @Column(nullable = false, length = 50)
    private String country;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal salary;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<SalaryHistory> salaryHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the employee's full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
