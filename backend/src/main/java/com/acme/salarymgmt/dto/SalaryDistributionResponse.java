package com.acme.salarymgmt.dto;

import lombok.*;

/**
 * Response DTO for salary distribution buckets (histogram data).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryDistributionResponse {

    private String range;
    private long count;
}
