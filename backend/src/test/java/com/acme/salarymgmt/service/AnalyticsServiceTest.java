package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.*;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    // ── SALARY SUMMARY ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getSalarySummary")
    class GetSalarySummary {

        @Test
        @DisplayName("should return correct summary with all metrics")
        void shouldReturnCorrectSummary() {
            when(employeeRepository.countByIsActiveTrue()).thenReturn(100L);
            when(employeeRepository.getTotalPayroll()).thenReturn(new BigDecimal("7500000"));
            when(employeeRepository.getAverageSalary()).thenReturn(new BigDecimal("75000"));
            when(employeeRepository.getMinSalary()).thenReturn(new BigDecimal("30000"));
            when(employeeRepository.getMaxSalary()).thenReturn(new BigDecimal("200000"));
            when(employeeRepository.getAllActiveSalariesSorted())
                    .thenReturn(List.of(
                            new BigDecimal("30000"),
                            new BigDecimal("50000"),
                            new BigDecimal("75000"),
                            new BigDecimal("100000")
                    ));

            SalarySummaryResponse result = analyticsService.getSalarySummary();

            assertThat(result.getTotalEmployees()).isEqualTo(100);
            assertThat(result.getTotalPayroll()).isEqualByComparingTo(new BigDecimal("7500000.0000"));
            assertThat(result.getAverageSalary()).isEqualByComparingTo(new BigDecimal("75000.0000"));
            assertThat(result.getMinSalary()).isEqualByComparingTo(new BigDecimal("30000"));
            assertThat(result.getMaxSalary()).isEqualByComparingTo(new BigDecimal("200000"));
            // Median of [30000, 50000, 75000, 100000] = (50000 + 75000) / 2 = 62500
            assertThat(result.getMedianSalary()).isEqualByComparingTo(new BigDecimal("62500.0000"));
        }

        @Test
        @DisplayName("should return zeros when no employees exist")
        void shouldReturnZerosWhenEmpty() {
            when(employeeRepository.countByIsActiveTrue()).thenReturn(0L);
            when(employeeRepository.getTotalPayroll()).thenReturn(null);
            when(employeeRepository.getAverageSalary()).thenReturn(null);
            when(employeeRepository.getMinSalary()).thenReturn(null);
            when(employeeRepository.getMaxSalary()).thenReturn(null);
            when(employeeRepository.getAllActiveSalariesSorted()).thenReturn(Collections.emptyList());

            SalarySummaryResponse result = analyticsService.getSalarySummary();

            assertThat(result.getTotalEmployees()).isZero();
            assertThat(result.getTotalPayroll()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getMedianSalary()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should calculate median correctly for odd-sized list")
        void shouldCalculateMedianForOddSizedList() {
            when(employeeRepository.countByIsActiveTrue()).thenReturn(3L);
            when(employeeRepository.getTotalPayroll()).thenReturn(new BigDecimal("180000"));
            when(employeeRepository.getAverageSalary()).thenReturn(new BigDecimal("60000"));
            when(employeeRepository.getMinSalary()).thenReturn(new BigDecimal("40000"));
            when(employeeRepository.getMaxSalary()).thenReturn(new BigDecimal("80000"));
            when(employeeRepository.getAllActiveSalariesSorted())
                    .thenReturn(List.of(
                            new BigDecimal("40000"),
                            new BigDecimal("60000"),
                            new BigDecimal("80000")
                    ));

            SalarySummaryResponse result = analyticsService.getSalarySummary();

            // Median of [40000, 60000, 80000] = 60000 (middle element)
            assertThat(result.getMedianSalary()).isEqualByComparingTo(new BigDecimal("60000"));
        }
    }

    // ── SALARY BY DEPARTMENT ────────────────────────────────────────────

    @Nested
    @DisplayName("getSalaryByDepartment")
    class GetSalaryByDepartment {

        @Test
        @DisplayName("should return salary stats per department")
        void shouldReturnSalaryStatsByDepartment() {
            List<Object[]> mockData = List.of(
                    new Object[]{"Engineering", 95000.0, 60000.0, 150000.0, 500L},
                    new Object[]{"Sales", 65000.0, 40000.0, 100000.0, 300L}
            );
            when(employeeRepository.getSalaryStatsByDepartment()).thenReturn(mockData);

            List<DepartmentSalaryResponse> result = analyticsService.getSalaryByDepartment();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDepartment()).isEqualTo("Engineering");
            assertThat(result.get(0).getEmployeeCount()).isEqualTo(500);
            assertThat(result.get(1).getDepartment()).isEqualTo("Sales");
        }

        @Test
        @DisplayName("should return empty list when no data")
        void shouldReturnEmptyWhenNoData() {
            when(employeeRepository.getSalaryStatsByDepartment()).thenReturn(Collections.emptyList());

            List<DepartmentSalaryResponse> result = analyticsService.getSalaryByDepartment();

            assertThat(result).isEmpty();
        }
    }

    // ── SALARY BY COUNTRY ───────────────────────────────────────────────

    @Nested
    @DisplayName("getSalaryByCountry")
    class GetSalaryByCountry {

        @Test
        @DisplayName("should return salary stats per country with currency")
        void shouldReturnSalaryStatsByCountry() {
            List<Object[]> mockData = List.of(
                    new Object[]{"USA", "USD", 85000.0, 40000.0, 200000.0, 2000L},
                    new Object[]{"India", "INR", 1500000.0, 600000.0, 4000000.0, 1500L}
            );
            when(employeeRepository.getSalaryStatsByCountry()).thenReturn(mockData);

            List<CountrySalaryResponse> result = analyticsService.getSalaryByCountry();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCountry()).isEqualTo("USA");
            assertThat(result.get(0).getCurrency()).isEqualTo("USD");
            assertThat(result.get(1).getCountry()).isEqualTo("India");
            assertThat(result.get(1).getCurrency()).isEqualTo("INR");
        }
    }

    // ── SALARY DISTRIBUTION ─────────────────────────────────────────────

    @Nested
    @DisplayName("getSalaryDistribution")
    class GetSalaryDistribution {

        @Test
        @DisplayName("should correctly bucket salaries into distribution ranges")
        void shouldBucketSalariesCorrectly() {
            List<BigDecimal> salaries = List.of(
                    new BigDecimal("25000"),   // Below 30K
                    new BigDecimal("29999"),   // Below 30K
                    new BigDecimal("45000"),   // 30K – 60K
                    new BigDecimal("55000"),   // 30K – 60K
                    new BigDecimal("75000"),   // 60K – 100K
                    new BigDecimal("120000"),  // 100K – 150K
                    new BigDecimal("200000")   // Above 150K
            );
            when(employeeRepository.getAllActiveSalariesSorted()).thenReturn(salaries);

            List<SalaryDistributionResponse> result = analyticsService.getSalaryDistribution();

            assertThat(result).hasSize(5);
            assertThat(result.get(0).getRange()).isEqualTo("Below 30K");
            assertThat(result.get(0).getCount()).isEqualTo(2);
            assertThat(result.get(1).getRange()).isEqualTo("30K – 60K");
            assertThat(result.get(1).getCount()).isEqualTo(2);
            assertThat(result.get(2).getRange()).isEqualTo("60K – 100K");
            assertThat(result.get(2).getCount()).isEqualTo(1);
            assertThat(result.get(3).getRange()).isEqualTo("100K – 150K");
            assertThat(result.get(3).getCount()).isEqualTo(1);
            assertThat(result.get(4).getRange()).isEqualTo("Above 150K");
            assertThat(result.get(4).getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return zero counts when no employees")
        void shouldReturnZeroCountsWhenEmpty() {
            when(employeeRepository.getAllActiveSalariesSorted()).thenReturn(Collections.emptyList());

            List<SalaryDistributionResponse> result = analyticsService.getSalaryDistribution();

            assertThat(result).hasSize(5);
            result.forEach(bucket -> assertThat(bucket.getCount()).isZero());
        }
    }

    // ── HEADCOUNT ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getHeadcountSummary")
    class GetHeadcountSummary {

        @Test
        @DisplayName("should return headcount by all three dimensions")
        void shouldReturnHeadcountByAllDimensions() {
            when(employeeRepository.getHeadcountByDepartment())
                    .thenReturn(List.of(new Object[]{"Engineering", 500L}));
            when(employeeRepository.getHeadcountByCountry())
                    .thenReturn(List.of(new Object[]{"USA", 2000L}));
            when(employeeRepository.getHeadcountByDesignation())
                    .thenReturn(List.of(new Object[]{"Senior", 2500L}));

            HeadcountResponse result = analyticsService.getHeadcountSummary();

            assertThat(result.getByDepartment()).hasSize(1);
            assertThat(result.getByDepartment().get(0).getCategory()).isEqualTo("Engineering");
            assertThat(result.getByDepartment().get(0).getCount()).isEqualTo(500);

            assertThat(result.getByCountry()).hasSize(1);
            assertThat(result.getByCountry().get(0).getCategory()).isEqualTo("USA");

            assertThat(result.getByDesignation()).hasSize(1);
            assertThat(result.getByDesignation().get(0).getCategory()).isEqualTo("Senior");
        }
    }
}
