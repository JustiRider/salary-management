package com.acme.salarymgmt.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Response DTO for salary statistics grouped by department.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSalaryResponse {

    private String department;
    private BigDecimal averageSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private long employeeCount;
}
