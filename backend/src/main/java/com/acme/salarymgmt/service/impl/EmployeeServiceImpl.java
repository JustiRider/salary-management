package com.acme.salarymgmt.service.impl;

import com.acme.salarymgmt.dto.EmployeeRequest;
import com.acme.salarymgmt.dto.EmployeeResponse;
import com.acme.salarymgmt.dto.SalaryHistoryResponse;
import com.acme.salarymgmt.exception.DuplicateResourceException;
import com.acme.salarymgmt.exception.ResourceNotFoundException;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.model.SalaryHistory;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryHistoryRepository;
import com.acme.salarymgmt.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;

    /**
     * Thread-safe counter for generating employee codes.
     * Initialized lazily from the database on first use.
     */
    private final AtomicLong employeeCodeCounter = new AtomicLong(0);
    private volatile boolean counterInitialized = false;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                                SalaryHistoryRepository salaryHistoryRepository) {
        this.employeeRepository = employeeRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getEmployees(int page, int size, String search,
                                                String department, String country,
                                                String sortBy, String sortDir) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null && !sortBy.isBlank() ? sortBy : "employeeCode"
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Employee> employees = employeeRepository.searchEmployees(search, department, country, pageable);
        return employees.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = findEmployeeOrThrow(id);
        return toResponse(employee);
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        // Check for duplicate email
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        Employee employee = Employee.builder()
                .employeeCode(generateEmployeeCode())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .department(request.getDepartment().trim())
                .designation(request.getDesignation().trim())
                .country(request.getCountry().trim())
                .currency(request.getCurrency().trim().toUpperCase())
                .salary(request.getSalary())
                .hireDate(request.getHireDate())
                .isActive(true)
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("Created employee: {} ({})", saved.getFullName(), saved.getEmployeeCode());
        return toResponse(saved);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = findEmployeeOrThrow(id);

        // Check for duplicate email (if email is being changed)
        if (!employee.getEmail().equalsIgnoreCase(request.getEmail().trim()) &&
                employeeRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        // Track salary change
        boolean salaryChanged = employee.getSalary().compareTo(request.getSalary()) != 0;
        if (salaryChanged) {
            SalaryHistory history = SalaryHistory.builder()
                    .employee(employee)
                    .oldSalary(employee.getSalary())
                    .newSalary(request.getSalary())
                    .effectiveDate(LocalDate.now())
                    .reason(request.getSalaryChangeReason() != null ?
                            request.getSalaryChangeReason() : "Salary updated")
                    .build();
            salaryHistoryRepository.save(history);
            log.info("Salary changed for {} ({}): {} → {}",
                    employee.getFullName(), employee.getEmployeeCode(),
                    employee.getSalary(), request.getSalary());
        }

        // Update fields
        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().trim().toLowerCase());
        employee.setDepartment(request.getDepartment().trim());
        employee.setDesignation(request.getDesignation().trim());
        employee.setCountry(request.getCountry().trim());
        employee.setCurrency(request.getCurrency().trim().toUpperCase());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());

        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeOrThrow(id);
        employee.setIsActive(false);
        employeeRepository.save(employee);
        log.info("Soft-deleted employee: {} ({})", employee.getFullName(), employee.getEmployeeCode());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryHistoryResponse> getSalaryHistory(Long employeeId) {
        // Verify employee exists
        findEmployeeOrThrow(employeeId);
        List<SalaryHistory> history = salaryHistoryRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        return history.stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDepartments() {
        return employeeRepository.findDistinctDepartments();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCountries() {
        return employeeRepository.findDistinctCountries();
    }

    // ── Helper Methods ──────────────────────────────────────────────────

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .filter(Employee::getIsActive)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    /**
     * Generates a unique employee code in the format EMP-XXXXX.
     * Thread-safe via AtomicLong counter initialized from the DB.
     */
    private synchronized String generateEmployeeCode() {
        if (!counterInitialized) {
            long maxId = employeeRepository.count();
            employeeCodeCounter.set(maxId);
            counterInitialized = true;
        }
        long next = employeeCodeCounter.incrementAndGet();
        return String.format("EMP-%05d", next);
    }

    /**
     * Maps an Employee entity to an EmployeeResponse DTO.
     */
    EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .country(employee.getCountry())
                .currency(employee.getCurrency())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate())
                .isActive(employee.getIsActive())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    /**
     * Maps a SalaryHistory entity to a SalaryHistoryResponse DTO.
     */
    private SalaryHistoryResponse toHistoryResponse(SalaryHistory history) {
        return SalaryHistoryResponse.builder()
                .id(history.getId())
                .oldSalary(history.getOldSalary())
                .newSalary(history.getNewSalary())
                .effectiveDate(history.getEffectiveDate())
                .reason(history.getReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
