package com.acme.salarymgmt.service.impl;

import com.acme.salarymgmt.dto.*;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final EmployeeRepository employeeRepository;

    public AnalyticsServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public SalarySummaryResponse getSalarySummary() {
        long totalEmployees = employeeRepository.countByIsActiveTrue();
        BigDecimal totalPayroll = employeeRepository.getTotalPayroll();
        BigDecimal avgSalary = employeeRepository.getAverageSalary();
        BigDecimal minSalary = employeeRepository.getMinSalary();
        BigDecimal maxSalary = employeeRepository.getMaxSalary();
        BigDecimal medianSalary = calculateMedian();

        return SalarySummaryResponse.builder()
                .totalEmployees(totalEmployees)
                .totalPayroll(totalPayroll != null ? totalPayroll.setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .averageSalary(avgSalary != null ? avgSalary.setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .minSalary(minSalary != null ? minSalary : BigDecimal.ZERO)
                .maxSalary(maxSalary != null ? maxSalary : BigDecimal.ZERO)
                .medianSalary(medianSalary)
                .build();
    }

    @Override
    public List<DepartmentSalaryResponse> getSalaryByDepartment() {
        List<Object[]> results = employeeRepository.getSalaryStatsByDepartment();
        return results.stream()
                .map(row -> DepartmentSalaryResponse.builder()
                        .department((String) row[0])
                        .averageSalary(toBigDecimal(row[1]).setScale(4, RoundingMode.HALF_UP))
                        .minSalary(toBigDecimal(row[2]))
                        .maxSalary(toBigDecimal(row[3]))
                        .employeeCount(((Number) row[4]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CountrySalaryResponse> getSalaryByCountry() {
        List<Object[]> results = employeeRepository.getSalaryStatsByCountry();
        return results.stream()
                .map(row -> CountrySalaryResponse.builder()
                        .country((String) row[0])
                        .currency((String) row[1])
                        .averageSalary(toBigDecimal(row[2]).setScale(4, RoundingMode.HALF_UP))
                        .minSalary(toBigDecimal(row[3]))
                        .maxSalary(toBigDecimal(row[4]))
                        .employeeCount(((Number) row[5]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SalaryDistributionResponse> getSalaryDistribution() {
        List<BigDecimal> salaries = employeeRepository.getAllActiveSalariesSorted();

        // Define distribution buckets
        BigDecimal[] thresholds = {
                new BigDecimal("30000"),
                new BigDecimal("60000"),
                new BigDecimal("100000"),
                new BigDecimal("150000")
        };
        String[] labels = {
                "Below 30K",
                "30K – 60K",
                "60K – 100K",
                "100K – 150K",
                "Above 150K"
        };

        long[] counts = new long[labels.length];
        for (BigDecimal salary : salaries) {
            if (salary.compareTo(thresholds[0]) < 0) {
                counts[0]++;
            } else if (salary.compareTo(thresholds[1]) < 0) {
                counts[1]++;
            } else if (salary.compareTo(thresholds[2]) < 0) {
                counts[2]++;
            } else if (salary.compareTo(thresholds[3]) < 0) {
                counts[3]++;
            } else {
                counts[4]++;
            }
        }

        List<SalaryDistributionResponse> distribution = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            distribution.add(SalaryDistributionResponse.builder()
                    .range(labels[i])
                    .count(counts[i])
                    .build());
        }
        return distribution;
    }

    @Override
    public HeadcountResponse getHeadcountSummary() {
        List<HeadcountResponse.HeadcountEntry> byDepartment = employeeRepository.getHeadcountByDepartment()
                .stream()
                .map(row -> HeadcountResponse.HeadcountEntry.builder()
                        .category((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<HeadcountResponse.HeadcountEntry> byCountry = employeeRepository.getHeadcountByCountry()
                .stream()
                .map(row -> HeadcountResponse.HeadcountEntry.builder()
                        .category((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<HeadcountResponse.HeadcountEntry> byDesignation = employeeRepository.getHeadcountByDesignation()
                .stream()
                .map(row -> HeadcountResponse.HeadcountEntry.builder()
                        .category((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        return HeadcountResponse.builder()
                .byDepartment(byDepartment)
                .byCountry(byCountry)
                .byDesignation(byDesignation)
                .build();
    }

    // ── Helper Methods ──────────────────────────────────────────────────

    /**
     * Calculates the median salary from all active employees.
     * Fetches sorted salaries and picks the middle value(s).
     */
    private BigDecimal calculateMedian() {
        List<BigDecimal> salaries = employeeRepository.getAllActiveSalariesSorted();
        if (salaries.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int size = salaries.size();
        if (size % 2 == 1) {
            return salaries.get(size / 2);
        } else {
            BigDecimal lower = salaries.get(size / 2 - 1);
            BigDecimal upper = salaries.get(size / 2);
            return lower.add(upper).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        }
    }

    /**
     * Safely converts an Object (typically Double from JPQL AVG) to BigDecimal.
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}
