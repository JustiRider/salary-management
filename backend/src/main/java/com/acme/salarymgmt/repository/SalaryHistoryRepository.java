package com.acme.salarymgmt.repository;

import com.acme.salarymgmt.model.SalaryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Long> {

    List<SalaryHistory> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    long countByEmployeeId(Long employeeId);
}
