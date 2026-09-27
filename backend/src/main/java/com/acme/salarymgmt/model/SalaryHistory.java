package com.acme.salarymgmt.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_history", indexes = {
        @Index(name = "idx_salary_history_employee", columnList = "employee_id"),
        @Index(name = "idx_salary_history_effective_date", columnList = "effectiveDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal oldSalary;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal newSalary;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
