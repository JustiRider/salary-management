package com.acme.salarymgmt.dto;

import lombok.*;
import java.util.List;

/**
 * Response DTO for headcount breakdown by category (department, country, or designation).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeadcountResponse {

    private List<HeadcountEntry> byDepartment;
    private List<HeadcountEntry> byCountry;
    private List<HeadcountEntry> byDesignation;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeadcountEntry {
        private String category;
        private long count;
    }
}
