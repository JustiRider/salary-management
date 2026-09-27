package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryHistoryRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryHistoryRepository salaryHistoryRepository;

    @BeforeEach
    void setUp() {
        salaryHistoryRepository.deleteAll();
        employeeRepository.deleteAll();
        seedTestData();
    }

    /**
     * Seeds 10 test employees across 2 departments and 2 countries for predictable analytics.
     */
    private void seedTestData() {
        LocalDateTime now = LocalDateTime.now();
        List<Employee> employees = new ArrayList<>();

        // Engineering, USA — 4 employees
        employees.add(buildEmployee("EMP-00001", "John", "Doe", "john@acme.com",
                "Engineering", "Senior", "USA", "USD", "80000.0000", now));
        employees.add(buildEmployee("EMP-00002", "Jane", "Smith", "jane@acme.com",
                "Engineering", "Lead", "USA", "USD", "120000.0000", now));
        employees.add(buildEmployee("EMP-00003", "Bob", "Wilson", "bob@acme.com",
                "Engineering", "Junior", "USA", "USD", "60000.0000", now));
        employees.add(buildEmployee("EMP-00004", "Alice", "Brown", "alice@acme.com",
                "Engineering", "Mid-Level", "USA", "USD", "90000.0000", now));

        // Sales, USA — 3 employees
        employees.add(buildEmployee("EMP-00005", "Charlie", "Davis", "charlie@acme.com",
                "Sales", "Senior", "USA", "USD", "70000.0000", now));
        employees.add(buildEmployee("EMP-00006", "Diana", "Evans", "diana@acme.com",
                "Sales", "Mid-Level", "USA", "USD", "55000.0000", now));
        employees.add(buildEmployee("EMP-00007", "Eve", "Garcia", "eve@acme.com",
                "Sales", "Junior", "USA", "USD", "45000.0000", now));

        // Engineering, India — 3 employees
        employees.add(buildEmployee("EMP-00008", "Raj", "Kumar", "raj@acme.com",
                "Engineering", "Senior", "India", "INR", "1500000.0000", now));
        employees.add(buildEmployee("EMP-00009", "Priya", "Sharma", "priya@acme.com",
                "Engineering", "Mid-Level", "India", "INR", "1000000.0000", now));
        employees.add(buildEmployee("EMP-00010", "Amit", "Patel", "amit@acme.com",
                "Engineering", "Lead", "India", "INR", "2000000.0000", now));

        employeeRepository.saveAll(employees);
    }

    private Employee buildEmployee(String code, String firstName, String lastName, String email,
                                    String department, String designation, String country,
                                    String currency, String salary, LocalDateTime now) {
        return Employee.builder()
                .employeeCode(code)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .department(department)
                .designation(designation)
                .country(country)
                .currency(currency)
                .salary(new BigDecimal(salary))
                .hireDate(LocalDate.of(2021, 6, 1))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ── SUMMARY ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/analytics/summary — should return correct salary summary")
    void shouldReturnCorrectSalarySummary() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees", is(10)))
                .andExpect(jsonPath("$.totalPayroll").isNumber())
                .andExpect(jsonPath("$.averageSalary").isNumber())
                .andExpect(jsonPath("$.minSalary").isNumber())
                .andExpect(jsonPath("$.maxSalary").isNumber())
                .andExpect(jsonPath("$.medianSalary").isNumber());
    }

    // ── BY DEPARTMENT ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/analytics/by-department — should return stats per department")
    void shouldReturnStatsByDepartment() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/by-department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.department == 'Engineering')].employeeCount", contains(7)))
                .andExpect(jsonPath("$[?(@.department == 'Sales')].employeeCount", contains(3)));
    }

    // ── BY COUNTRY ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/analytics/by-country — should return stats per country")
    void shouldReturnStatsByCountry() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/by-country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.country == 'USA')].currency", contains("USD")))
                .andExpect(jsonPath("$[?(@.country == 'India')].currency", contains("INR")));
    }

    // ── DISTRIBUTION ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/analytics/distribution — should return 5 distribution buckets")
    void shouldReturnDistributionBuckets() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].range").exists())
                .andExpect(jsonPath("$[0].count").isNumber());
    }

    // ── HEADCOUNT ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/analytics/headcount — should return headcount by all dimensions")
    void shouldReturnHeadcountByAllDimensions() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/headcount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byDepartment").isArray())
                .andExpect(jsonPath("$.byCountry").isArray())
                .andExpect(jsonPath("$.byDesignation").isArray())
                .andExpect(jsonPath("$.byDepartment", hasSize(2)))
                .andExpect(jsonPath("$.byCountry", hasSize(2)));
    }
}
