package com.acme.salarymgmt.controller;

import com.acme.salarymgmt.dto.EmployeeRequest;
import com.acme.salarymgmt.dto.EmployeeResponse;
import com.acme.salarymgmt.dto.SalaryHistoryResponse;
import com.acme.salarymgmt.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for employee CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * GET /api/v1/employees
     * Returns a paginated, searchable, filterable list of employees.
     */
    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "employeeCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<EmployeeResponse> employees = employeeService.getEmployees(
                page, size, search, department, country, sortBy, sortDir);
        return ResponseEntity.ok(employees);
    }

    /**
     * GET /api/v1/employees/{id}
     * Returns a single employee by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * POST /api/v1/employees
     * Creates a new employee.
     */
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/v1/employees/{id}
     * Updates an existing employee. Tracks salary changes automatically.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    /**
     * DELETE /api/v1/employees/{id}
     * Soft-deletes an employee (sets isActive = false).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/employees/{id}/salary-history
     * Returns the salary change history for a specific employee.
     */
    @GetMapping("/{id}/salary-history")
    public ResponseEntity<List<SalaryHistoryResponse>> getSalaryHistory(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getSalaryHistory(id));
    }

    /**
     * GET /api/v1/employees/departments
     * Returns distinct department names (for filter dropdowns).
     */
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(employeeService.getDepartments());
    }

    /**
     * GET /api/v1/employees/countries
     * Returns distinct country names (for filter dropdowns).
     */
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(employeeService.getCountries());
    }
}
