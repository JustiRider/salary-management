package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.*;

import java.util.List;

/**
 * Service interface for salary analytics and reporting.
 */
public interface AnalyticsService {

    /**
     * Returns org-wide salary summary: total payroll, avg, min, max, median.
     */
    SalarySummaryResponse getSalarySummary();

    /**
     * Returns salary statistics grouped by department.
     */
    List<DepartmentSalaryResponse> getSalaryByDepartment();

    /**
     * Returns salary statistics grouped by country.
     */
    List<CountrySalaryResponse> getSalaryByCountry();

    /**
     * Returns salary distribution in predefined buckets.
     */
    List<SalaryDistributionResponse> getSalaryDistribution();

    /**
     * Returns headcount breakdown by department, country, and designation.
     */
    HeadcountResponse getHeadcountSummary();
}
