# Employee Salary Management Software — Requirements Document

**Organization:** ACME Corp  
**Target User:** HR Manager  
**Date:** September 2026  
**Version:** 1.0

---

## 1. Goal

Replace ACME Corp's error-prone, Excel-based salary management workflow with a centralized, web-based application that allows the HR Manager to:

- View, search, filter, and manage salary data for **10,000 employees** across multiple countries.
- Gain instant insights into how the organization pays people through **analytics dashboards** (averages, distributions, comparisons by department/country/role).
- Perform salary updates with an auditable trail.

---

## 2. Scope & Features

### 2.1 Employee Directory (CRUD)

| Capability | Detail |
|---|---|
| **List / Search** | Paginated, server-side filtered table of all employees. Search by name, employee ID, department, country. |
| **View** | Detail view showing full profile: name, email, employee ID, department, designation, country, hire date, current salary, currency. |
| **Create** | Add a new employee with all required fields. |
| **Update** | Edit employee profile and salary. Salary changes are recorded in a salary history table. |
| **Delete** | Soft-delete an employee (mark inactive). |

### 2.2 Salary Analytics Dashboard

| Metric | Description |
|---|---|
| **Total Payroll** | Sum of all active employee salaries. |
| **Average Salary** | Org-wide and grouped by department, country, designation. |
| **Min / Max / Median** | Salary range statistics. |
| **Salary Distribution** | Histogram buckets (e.g., <30k, 30–60k, 60–100k, 100k+). |
| **Department Comparison** | Bar chart comparing average salary across departments. |
| **Country Comparison** | Average salary by country. |
| **Headcount Summary** | Employee count by department, country, designation. |

### 2.3 Salary History

- Every salary change for an employee is logged with: old salary, new salary, effective date, and change reason.
- Viewable on the employee detail page.

### 2.4 Data Seeding

- A seed script that generates **10,000 realistic employee records** covering:
  - 8–10 departments (Engineering, Sales, Marketing, Finance, HR, Operations, Legal, Support, Product, Design)
  - 6–8 countries (USA, UK, India, Germany, Australia, Canada, Japan, Brazil)
  - Realistic salary ranges per country/department
  - Hire dates spanning the last 15 years

### 2.5 Non-Functional Requirements

| Requirement | Target |
|---|---|
| **Performance** | Employee list page loads in < 2s for 10k rows (server-side pagination). |
| **Code Quality** | Clean layered architecture; service, controller, repository separation. |
| **Test Coverage** | Unit tests for service layer; integration tests for repository/controller layers. |
| **API Design** | RESTful, versioned (`/api/v1/...`), consistent error responses. |

---

## 3. Deliberately Out of Scope (v1)

| Feature | Reasoning |
|---|---|
| **Authentication & Authorization** | Adds significant complexity (OAuth2, JWT, roles). The v1 focus is on core salary management value. Can be layered on later without changing the data model. |
| **Role-Based Access Control** | Coupled with auth — deferred to v2. |
| **Payroll Processing / Payslip Generation** | This is a salary *data management* tool, not a payroll engine. Payroll integrations (tax calculations, bank transfers) are a separate product concern. |
| **Multi-Currency Normalization** | Salaries are stored in their local currency. Cross-currency comparison (PPP-adjusted, exchange-rate-normalized) is complex and deferred. Analytics show raw values grouped by country. |
| **Bulk CSV Import / Export** | Useful but not core. Seed script demonstrates data loading. Export can be added in v2. |
| **Email Notifications** | No automated alerts for salary changes in v1. |
| **Approval Workflows** | Salary change approvals (manager → director → HR VP) add workflow engine complexity. Deferred. |
| **Audit Log UI** | Salary history is tracked in the DB, but a full audit log UI (who changed what, when) is deferred. |
| **Internationalization (i18n)** | English-only UI for v1. |
| **Mobile-Responsive Design** | Desktop-first for the HR Manager use case. Basic responsiveness only. |
| **Deployment / CI/CD** | Out of scope; the focus is on application code and local development. |

---

## 4. Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.x |
| **Frontend** | React (Vite), Recharts for charts |
| **Database** | MySQL 8.x |
| **ORM** | Spring Data JPA / Hibernate |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |
| **Build** | Maven (backend), npm (frontend) |

---

## 5. Data Model (Summary)

```
Employee
├── id (BIGINT, PK)
├── employee_code (VARCHAR, unique, e.g. "EMP-00001")
├── first_name, last_name
├── email (unique)
├── department (VARCHAR)
├── designation (VARCHAR)
├── country (VARCHAR)
├── currency (VARCHAR, e.g. "USD", "INR")
├── salary (DECIMAL 12,4)
├── hire_date (DATE)
├── is_active (BOOLEAN, default true)
├── created_at, updated_at (TIMESTAMP)

SalaryHistory
├── id (BIGINT, PK)
├── employee_id (FK → Employee)
├── old_salary (DECIMAL 12,4)
├── new_salary (DECIMAL 12,4)
├── effective_date (DATE)
├── reason (VARCHAR)
├── created_at (TIMESTAMP)
```

---

## 6. Success Criteria

1. HR Manager can search and find any employee in < 5 seconds.
2. Dashboard loads org-wide salary analytics with correct aggregations.
3. Salary updates are reflected immediately and tracked in history.
4. Seed script successfully populates 10,000 employees with realistic data.
5. Unit & integration tests pass with meaningful coverage of core business logic.
