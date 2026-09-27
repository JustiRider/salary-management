package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.dto.*;
import com.acme.salarymgmt.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for salary analytics and reporting endpoints.
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/v1/analytics/summary
     * Returns org-wide salary statistics: total payroll, avg, min, max, median.
     */
    @GetMapping("/summary")
    public ResponseEntity<SalarySummaryResponse> getSalarySummary() {
        return ResponseEntity.ok(analyticsService.getSalarySummary());
    }

    /**
     * GET /api/v1/analytics/by-department
     * Returns salary statistics grouped by department.
     */
    @GetMapping("/by-department")
    public ResponseEntity<List<DepartmentSalaryResponse>> getSalaryByDepartment() {
        return ResponseEntity.ok(analyticsService.getSalaryByDepartment());
    }

    /**
     * GET /api/v1/analytics/by-country
     * Returns salary statistics grouped by country.
     */
    @GetMapping("/by-country")
    public ResponseEntity<List<CountrySalaryResponse>> getSalaryByCountry() {
        return ResponseEntity.ok(analyticsService.getSalaryByCountry());
    }

    /**
     * GET /api/v1/analytics/distribution
     * Returns salary distribution in predefined buckets.
     */
    @GetMapping("/distribution")
    public ResponseEntity<List<SalaryDistributionResponse>> getSalaryDistribution() {
        return ResponseEntity.ok(analyticsService.getSalaryDistribution());
    }

    /**
     * GET /api/v1/analytics/headcount
     * Returns employee headcount breakdown by department, country, and designation.
     */
    @GetMapping("/headcount")
    public ResponseEntity<HeadcountResponse> getHeadcountSummary() {
        return ResponseEntity.ok(analyticsService.getHeadcountSummary());
    }
}
