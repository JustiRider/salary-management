package com.acme.salarymgmt.service;

import com.acme.salarymgmt.dto.EmployeeRequest;
import com.acme.salarymgmt.dto.EmployeeResponse;
import com.acme.salarymgmt.dto.SalaryHistoryResponse;
import com.acme.salarymgmt.exception.DuplicateResourceException;
import com.acme.salarymgmt.exception.ResourceNotFoundException;
import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.model.SalaryHistory;
import com.acme.salarymgmt.repository.EmployeeRepository;
import com.acme.salarymgmt.repository.SalaryHistoryRepository;
import com.acme.salarymgmt.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryHistoryRepository salaryHistoryRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee sampleEmployee;
    private EmployeeRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleEmployee = Employee.builder()
                .id(1L)
                .employeeCode("EMP-00001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@acme.com")
                .department("Engineering")
                .designation("Senior")
                .country("USA")
                .currency("USD")
                .salary(new BigDecimal("95000.0000"))
                .hireDate(LocalDate.of(2020, 3, 15))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = EmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@acme.com")
                .department("Engineering")
                .designation("Senior")
                .country("USA")
                .currency("USD")
                .salary(new BigDecimal("95000.0000"))
                .hireDate(LocalDate.of(2020, 3, 15))
                .build();
    }

    // ── GET EMPLOYEES ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getEmployees")
    class GetEmployees {

        @Test
        @DisplayName("should return paginated employees")
        void shouldReturnPaginatedEmployees() {
            Page<Employee> page = new PageImpl<>(List.of(sampleEmployee));
            when(employeeRepository.searchEmployees(any(), any(), any(), any(Pageable.class)))
                    .thenReturn(page);

            Page<EmployeeResponse> result = employeeService.getEmployees(
                    0, 20, null, null, null, "employeeCode", "asc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmployeeCode()).isEqualTo("EMP-00001");
            assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
            verify(employeeRepository).searchEmployees(isNull(), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("should pass search and filter parameters to repository")
        void shouldPassFiltersToRepository() {
            Page<Employee> page = new PageImpl<>(List.of());
            when(employeeRepository.searchEmployees(eq("john"), eq("Engineering"), eq("USA"), any(Pageable.class)))
                    .thenReturn(page);

            employeeService.getEmployees(0, 10, "john", "Engineering", "USA", "lastName", "desc");

            verify(employeeRepository).searchEmployees(eq("john"), eq("Engineering"), eq("USA"), any(Pageable.class));
        }
    }

    // ── GET EMPLOYEE BY ID ──────────────────────────────────────────────

    @Nested
    @DisplayName("getEmployeeById")
    class GetEmployeeById {

        @Test
        @DisplayName("should return employee when found and active")
        void shouldReturnEmployeeWhenFoundAndActive() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

            EmployeeResponse result = employeeService.getEmployeeById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getSalary()).isEqualByComparingTo(new BigDecimal("95000.0000"));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for inactive employee")
        void shouldThrowForInactiveEmployee() {
            sampleEmployee.setIsActive(false);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));

            assertThatThrownBy(() -> employeeService.getEmployeeById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── CREATE EMPLOYEE ─────────────────────────────────────────────────

    @Nested
    @DisplayName("createEmployee")
    class CreateEmployee {

        @Test
        @DisplayName("should create employee successfully")
        void shouldCreateEmployee() {
            when(employeeRepository.existsByEmail("john.doe@acme.com")).thenReturn(false);
            when(employeeRepository.count()).thenReturn(0L);
            when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

            EmployeeResponse result = employeeService.createEmployee(sampleRequest);

            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getEmail()).isEqualTo("john.doe@acme.com");
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException for duplicate email")
        void shouldThrowForDuplicateEmail() {
            when(employeeRepository.existsByEmail("john.doe@acme.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.createEmployee(sampleRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email")
                    .hasMessageContaining("john.doe@acme.com");

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should trim and normalize input fields")
        void shouldTrimAndNormalizeInputs() {
            sampleRequest.setFirstName("  John  ");
            sampleRequest.setLastName("  Doe  ");
            sampleRequest.setEmail("  JOHN.DOE@ACME.COM  ");
            sampleRequest.setCurrency("  usd  ");

            when(employeeRepository.existsByEmail("john.doe@acme.com")).thenReturn(false);
            when(employeeRepository.count()).thenReturn(0L);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
                Employee saved = inv.getArgument(0);
                saved.setId(1L);
                saved.setCreatedAt(LocalDateTime.now());
                saved.setUpdatedAt(LocalDateTime.now());
                return saved;
            });

            employeeService.createEmployee(sampleRequest);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());
            Employee saved = captor.getValue();

            assertThat(saved.getFirstName()).isEqualTo("John");
            assertThat(saved.getLastName()).isEqualTo("Doe");
            assertThat(saved.getEmail()).isEqualTo("john.doe@acme.com");
            assertThat(saved.getCurrency()).isEqualTo("USD");
        }
    }

    // ── UPDATE EMPLOYEE ─────────────────────────────────────────────────

    @Nested
    @DisplayName("updateEmployee")
    class UpdateEmployee {

        @Test
        @DisplayName("should update employee without salary history when salary unchanged")
        void shouldUpdateWithoutHistoryWhenSalarySame() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

            employeeService.updateEmployee(1L, sampleRequest);

            verify(salaryHistoryRepository, never()).save(any(SalaryHistory.class));
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("should create salary history record when salary changes")
        void shouldCreateHistoryWhenSalaryChanges() {
            BigDecimal newSalary = new BigDecimal("110000.0000");
            sampleRequest.setSalary(newSalary);
            sampleRequest.setSalaryChangeReason("Annual raise");

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

            employeeService.updateEmployee(1L, sampleRequest);

            ArgumentCaptor<SalaryHistory> historyCaptor = ArgumentCaptor.forClass(SalaryHistory.class);
            verify(salaryHistoryRepository).save(historyCaptor.capture());

            SalaryHistory history = historyCaptor.getValue();
            assertThat(history.getOldSalary()).isEqualByComparingTo(new BigDecimal("95000.0000"));
            assertThat(history.getNewSalary()).isEqualByComparingTo(newSalary);
            assertThat(history.getReason()).isEqualTo("Annual raise");
            assertThat(history.getEffectiveDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("should use default reason when salary change reason is null")
        void shouldUseDefaultReasonWhenNull() {
            sampleRequest.setSalary(new BigDecimal("120000.0000"));
            sampleRequest.setSalaryChangeReason(null);

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

            employeeService.updateEmployee(1L, sampleRequest);

            ArgumentCaptor<SalaryHistory> captor = ArgumentCaptor.forClass(SalaryHistory.class);
            verify(salaryHistoryRepository).save(captor.capture());
            assertThat(captor.getValue().getReason()).isEqualTo("Salary updated");
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when changing to existing email")
        void shouldThrowForDuplicateEmailOnUpdate() {
            sampleRequest.setEmail("existing@acme.com");
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.existsByEmail("existing@acme.com")).thenReturn(true);

            assertThatThrownBy(() -> employeeService.updateEmployee(1L, sampleRequest))
                    .isInstanceOf(DuplicateResourceException.class);

            verify(employeeRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when employee not found")
        void shouldThrowWhenEmployeeNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.updateEmployee(999L, sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── DELETE EMPLOYEE ─────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteEmployee")
    class DeleteEmployee {

        @Test
        @DisplayName("should soft-delete by setting isActive to false")
        void shouldSoftDelete() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(employeeRepository.save(any(Employee.class))).thenReturn(sampleEmployee);

            employeeService.deleteEmployee(1L);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());
            assertThat(captor.getValue().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ── SALARY HISTORY ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getSalaryHistory")
    class GetSalaryHistory {

        @Test
        @DisplayName("should return salary history for employee")
        void shouldReturnSalaryHistory() {
            SalaryHistory history = SalaryHistory.builder()
                    .id(1L)
                    .employee(sampleEmployee)
                    .oldSalary(new BigDecimal("85000.0000"))
                    .newSalary(new BigDecimal("95000.0000"))
                    .effectiveDate(LocalDate.of(2023, 1, 1))
                    .reason("Promotion")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(salaryHistoryRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(history));

            List<SalaryHistoryResponse> result = employeeService.getSalaryHistory(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOldSalary()).isEqualByComparingTo(new BigDecimal("85000.0000"));
            assertThat(result.get(0).getNewSalary()).isEqualByComparingTo(new BigDecimal("95000.0000"));
            assertThat(result.get(0).getReason()).isEqualTo("Promotion");
        }

        @Test
        @DisplayName("should return empty list when no history exists")
        void shouldReturnEmptyWhenNoHistory() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(sampleEmployee));
            when(salaryHistoryRepository.findByEmployeeIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of());

            List<SalaryHistoryResponse> result = employeeService.getSalaryHistory(1L);

            assertThat(result).isEmpty();
        }
    }
}
