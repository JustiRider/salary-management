package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.dto.EmployeeRequest;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryHistoryRepository salaryHistoryRepository;

    @BeforeEach
    void setUp() {
        salaryHistoryRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    private Employee createTestEmployee(String code, String firstName, String lastName,
                                         String email, String department, String country) {
        LocalDateTime now = LocalDateTime.now();
        return employeeRepository.save(Employee.builder()
                .employeeCode(code)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .department(department)
                .designation("Senior")
                .country(country)
                .currency("USD")
                .salary(new BigDecimal("90000.0000"))
                .hireDate(LocalDate.of(2020, 1, 15))
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    // ── GET EMPLOYEES ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/employees — should return paginated employees")
    void shouldReturnPaginatedEmployees() throws Exception {
        createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");
        createTestEmployee("EMP-00002", "Jane", "Smith", "jane@acme.com", "Sales", "UK");

        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].employeeCode").exists());
    }

    @Test
    @DisplayName("GET /api/v1/employees — should filter by search term")
    void shouldFilterBySearchTerm() throws Exception {
        createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");
        createTestEmployee("EMP-00002", "Jane", "Smith", "jane@acme.com", "Sales", "UK");

        mockMvc.perform(get("/api/v1/employees")
                        .param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName", is("John")));
    }

    @Test
    @DisplayName("GET /api/v1/employees — should filter by department")
    void shouldFilterByDepartment() throws Exception {
        createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");
        createTestEmployee("EMP-00002", "Jane", "Smith", "jane@acme.com", "Sales", "UK");

        mockMvc.perform(get("/api/v1/employees")
                        .param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].department", is("Engineering")));
    }

    @Test
    @DisplayName("GET /api/v1/employees — should filter by country")
    void shouldFilterByCountry() throws Exception {
        createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");
        createTestEmployee("EMP-00002", "Jane", "Smith", "jane@acme.com", "Sales", "UK");

        mockMvc.perform(get("/api/v1/employees")
                        .param("country", "UK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].country", is("UK")));
    }

    // ── GET EMPLOYEE BY ID ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/employees/{id} — should return employee by ID")
    void shouldReturnEmployeeById() throws Exception {
        Employee emp = createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");

        mockMvc.perform(get("/api/v1/employees/" + emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.fullName", is("John Doe")))
                .andExpect(jsonPath("$.employeeCode", is("EMP-00001")));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} — should return 404 for non-existent employee")
    void shouldReturn404ForNonExistentEmployee() throws Exception {
        mockMvc.perform(get("/api/v1/employees/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    // ── CREATE EMPLOYEE ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/employees — should create employee successfully")
    void shouldCreateEmployee() throws Exception {
        EmployeeRequest request = EmployeeRequest.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice@acme.com")
                .department("Engineering")
                .designation("Lead")
                .country("USA")
                .currency("USD")
                .salary(new BigDecimal("120000.0000"))
                .hireDate(LocalDate.of(2022, 6, 1))
                .build();

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.employeeCode").exists())
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    @DisplayName("POST /api/v1/employees — should return 400 for invalid request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        EmployeeRequest request = EmployeeRequest.builder()
                .firstName("")  // blank — should fail validation
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/employees — should return 409 for duplicate email")
    void shouldReturn409ForDuplicateEmail() throws Exception {
        createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");

        EmployeeRequest request = EmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("john@acme.com")  // duplicate
                .department("Sales")
                .designation("Mid-Level")
                .country("USA")
                .currency("USD")
                .salary(new BigDecimal("70000.0000"))
                .hireDate(LocalDate.of(2023, 1, 1))
                .build();

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("Conflict")));
    }

    // ── UPDATE EMPLOYEE ─────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/v1/employees/{id} — should update employee and track salary change")
    void shouldUpdateEmployeeAndTrackSalaryChange() throws Exception {
        Employee emp = createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");

        EmployeeRequest updateRequest = EmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@acme.com")
                .department("Engineering")
                .designation("Lead")
                .country("USA")
                .currency("USD")
                .salary(new BigDecimal("120000.0000"))  // salary changed
                .hireDate(LocalDate.of(2020, 1, 15))
                .salaryChangeReason("Promotion to Lead")
                .build();

        mockMvc.perform(put("/api/v1/employees/" + emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designation", is("Lead")));

        // Verify salary history was created
        mockMvc.perform(get("/api/v1/employees/" + emp.getId() + "/salary-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reason", is("Promotion to Lead")));
    }

    // ── DELETE EMPLOYEE ─────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} — should soft-delete employee")
    void shouldSoftDeleteEmployee() throws Exception {
        Employee emp = createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");

        mockMvc.perform(delete("/api/v1/employees/" + emp.getId()))
                .andExpect(status().isNoContent());

        // Verify the employee is no longer returned (filtered by isActive)
        mockMvc.perform(get("/api/v1/employees/" + emp.getId()))
                .andExpect(status().isNotFound());
    }

    // ── METADATA ENDPOINTS ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/employees/departments — should return distinct departments")
    void shouldReturnDistinctDepartments() throws Exception {
        createTestEmployee("EMP-00001", "John", "Doe", "john@acme.com", "Engineering", "USA");
        createTestEmployee("EMP-00002", "Jane", "Smith", "jane@acme.com", "Sales", "UK");

        mockMvc.perform(get("/api/v1/employees/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", containsInAnyOrder("Engineering", "Sales")));
    }
}
