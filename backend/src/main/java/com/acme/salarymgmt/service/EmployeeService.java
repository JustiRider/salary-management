package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.EmployeeRequest;
import com.acme.salarymgmt.dto.EmployeeResponse;
import com.acme.salarymgmt.dto.SalaryHistoryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for employee CRUD operations.
 */
public interface EmployeeService {

    /**
     * Returns a paginated, filtered list of active employees.
     *
     * @param page       zero-based page index
     * @param size       page size
     * @param search     optional search term (matches name, email, employee code)
     * @param department optional department filter
     * @param country    optional country filter
     * @param sortBy     field to sort by (default: "employeeCode")
     * @param sortDir    sort direction ("asc" or "desc")
     */
    Page<EmployeeResponse> getEmployees(int page, int size, String search,
                                         String department, String country,
                                         String sortBy, String sortDir);

    /**
     * Returns a single employee by ID.
     */
    EmployeeResponse getEmployeeById(Long id);

    /**
     * Creates a new employee and returns the created record.
     */
    EmployeeResponse createEmployee(EmployeeRequest request);

    /**
     * Updates an existing employee. If salary changed, creates a salary history record.
     */
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    /**
     * Soft-deletes an employee (sets isActive = false).
     */
    void deleteEmployee(Long id);

    /**
     * Returns the salary change history for a specific employee.
     */
    List<SalaryHistoryResponse> getSalaryHistory(Long employeeId);

    /**
     * Returns all distinct department names.
     */
    List<String> getDepartments();

    /**
     * Returns all distinct country names.
     */
    List<String> getCountries();
}
