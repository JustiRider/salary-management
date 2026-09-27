package com.acme.salarymgmt.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for a single salary history entry.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryHistoryResponse {

    private Long id;
    private BigDecimal oldSalary;
    private BigDecimal newSalary;
    private LocalDate effectiveDate;
    private String reason;
    private LocalDateTime createdAt;
}
