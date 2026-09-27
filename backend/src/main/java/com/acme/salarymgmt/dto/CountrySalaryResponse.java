package com.acme.salarymgmt.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * Response DTO for salary statistics grouped by country.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountrySalaryResponse {

    private String country;
    private String currency;
    private BigDecimal averageSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private long employeeCount;
}
