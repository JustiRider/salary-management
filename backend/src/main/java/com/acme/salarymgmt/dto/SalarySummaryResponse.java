package com.acme.salarymgmt.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Response DTO for org-wide salary summary statistics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalarySummaryResponse {

    private long totalEmployees;
    private BigDecimal totalPayroll;
    private BigDecimal averageSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private BigDecimal medianSalary;
}
