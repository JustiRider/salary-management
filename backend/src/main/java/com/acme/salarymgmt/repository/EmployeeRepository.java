package com.acme.salarymgmt.repository;

import com.acme.salarymgmt.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    /**
     * Paginated search with optional filters for department and country.
     * The search term matches against firstName, lastName, employeeCode, or email.
     */
    @Query("SELECT e FROM Employee e WHERE e.isActive = true " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:department IS NULL OR :department = '' OR e.department = :department) " +
            "AND (:country IS NULL OR :country = '' OR e.country = :country)")
    Page<Employee> searchEmployees(
            @Param("search") String search,
            @Param("department") String department,
            @Param("country") String country,
            Pageable pageable);

    long countByIsActiveTrue();

    // ── Analytics Queries ──────────────────────────────────────────────

    @Query("SELECT SUM(e.salary) FROM Employee e WHERE e.isActive = true")
    BigDecimal getTotalPayroll();

    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.isActive = true")
    BigDecimal getAverageSalary();

    @Query("SELECT MIN(e.salary) FROM Employee e WHERE e.isActive = true")
    BigDecimal getMinSalary();

    @Query("SELECT MAX(e.salary) FROM Employee e WHERE e.isActive = true")
    BigDecimal getMaxSalary();

    /**
     * Returns [department, avgSalary, minSalary, maxSalary, count] per department.
     */
    @Query("SELECT e.department, AVG(e.salary), MIN(e.salary), MAX(e.salary), COUNT(e) " +
            "FROM Employee e WHERE e.isActive = true GROUP BY e.department ORDER BY e.department")
    List<Object[]> getSalaryStatsByDepartment();

    /**
     * Returns [country, currency, avgSalary, minSalary, maxSalary, count] per country.
     */
    @Query("SELECT e.country, e.currency, AVG(e.salary), MIN(e.salary), MAX(e.salary), COUNT(e) " +
            "FROM Employee e WHERE e.isActive = true GROUP BY e.country, e.currency ORDER BY e.country")
    List<Object[]> getSalaryStatsByCountry();

    /**
     * Returns [department, count] per department for active employees.
     */
    @Query("SELECT e.department, COUNT(e) FROM Employee e WHERE e.isActive = true GROUP BY e.department ORDER BY e.department")
    List<Object[]> getHeadcountByDepartment();

    /**
     * Returns [country, count] per country for active employees.
     */
    @Query("SELECT e.country, COUNT(e) FROM Employee e WHERE e.isActive = true GROUP BY e.country ORDER BY e.country")
    List<Object[]> getHeadcountByCountry();

    /**
     * Returns [designation, count] per designation for active employees.
     */
    @Query("SELECT e.designation, COUNT(e) FROM Employee e WHERE e.isActive = true GROUP BY e.designation ORDER BY e.designation")
    List<Object[]> getHeadcountByDesignation();

    /**
     * Returns all active salaries for distribution calculation (done in service layer for flexibility).
     */
    @Query("SELECT e.salary FROM Employee e WHERE e.isActive = true ORDER BY e.salary")
    List<BigDecimal> getAllActiveSalariesSorted();

    /**
     * Returns distinct department names.
     */
    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.isActive = true ORDER BY e.department")
    List<String> findDistinctDepartments();

    /**
     * Returns distinct country names.
     */
    @Query("SELECT DISTINCT e.country FROM Employee e WHERE e.isActive = true ORDER BY e.country")
    List<String> findDistinctCountries();
}
