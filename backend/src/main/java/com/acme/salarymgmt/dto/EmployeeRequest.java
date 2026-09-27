package com.acme.salarymgmt.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating an employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @NotBlank(message = "Department is required")
    @Size(max = 50, message = "Department must be at most 50 characters")
    private String department;

    @NotBlank(message = "Designation is required")
    @Size(max = 50, message = "Designation must be at most 50 characters")
    private String designation;

    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country must be at most 50 characters")
    private String country;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    @Digits(integer = 12, fraction = 4, message = "Salary must have at most 12 integer digits and 4 decimal places")
    private BigDecimal salary;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;

    /**
     * Required when updating salary (reason for the change).
     * Optional on create.
     */
    private String salaryChangeReason;
}
