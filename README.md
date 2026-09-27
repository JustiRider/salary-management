# ACME Corp — Employee Salary Management Software

A centralized salary management and analytics platform built for the **HR Manager** of ACME Corp to manage salary data for **10,000 employees** across multiple global offices, replacing legacy spreadsheet workflows.

---

## 📋 Requirements & Design Document

The complete requirements specification, problem statement, scope, architecture, data model, and deliberate exclusions are detailed in:
👉 [**REQUIREMENTS.md**](REQUIREMENTS.md)

---

## 🛠 Tech Stack

- **Backend**: Java 17+ (Java 21 supported), Spring Boot 3.2.5
- **ORM & Data**: Spring Data JPA, Hibernate 6
- **Database**:
  - **H2 In-Memory Database** (default for instant zero-config startup & testing)
  - **MySQL 8.x** (production-ready configuration in `application.yml`)
- **Seeding**: Custom batch `CommandLineRunner` populating **10,000 realistic employee records** in under 3 seconds
- **Testing**: JUnit 5, Mockito, Spring Boot MockMvc, H2 test database
- **Frontend** *(Upcoming Phase)*: React JS, Recharts, Vite

---

## 📂 Project Structure

```text
Project/
├── REQUIREMENTS.md         # Full requirements specification document
├── README.md               # Project documentation & setup guide
├── .gitignore              # Git ignore rules (build targets, logs, etc.)
└── backend/
    ├── pom.xml             # Maven dependencies & build configuration
    ├── run.ps1             # Convenience PowerShell runner script
    └── src/
        ├── main/
        │   ├── java/com/acme/salarymgmt/
        │   │   ├── SalaryManagementApplication.java  # Main Spring Boot entrypoint
        │   │   ├── config/WebConfig.java             # CORS & Web configuration
        │   │   ├── controller/
        │   │   │   ├── EmployeeController.java       # CRUD & pagination REST APIs
        │   │   │   └── AnalyticsController.java      # Aggregation & metrics REST APIs
        │   │   ├── dto/                              # Request/Response data transfer objects
        │   │   ├── exception/                        # Global exception handling & error responses
        │   │   ├── model/
        │   │   │   ├── Employee.java                 # Core Employee JPA Entity
        │   │   │   └── SalaryHistory.java            # Salary audit trail JPA Entity
        │   │   ├── repository/
        │   │   │   ├── EmployeeRepository.java       # JPA repo with custom analytics queries
        │   │   │   └── SalaryHistoryRepository.java  # Audit history repository
        │   │   ├── seed/DataSeeder.java              # 10,000 employee data seeder
        │   │   └── service/                          # Business logic & implementations
        │   └── resources/
        │       └── application.yml                   # Database & application config
        └── test/
            ├── java/com/acme/salarymgmt/
            │   ├── controller/                       # MockMvc Integration tests
            │   └── service/                          # Service layer Unit tests
            └── resources/application-test.yml        # Test-specific H2 configuration
```

---

## 🚀 Quick Start (Running Backend on H2 In-Memory DB)

### Prerequisites
- Java 17 or higher (`java -version`)

### Option 1: Using the PowerShell Run Script
```powershell
cd backend
.\run.ps1
```

### Option 2: Using VS Code / Antigravity IDE
Open the project in VS Code / Antigravity IDE, press `F5` or navigate to **Run and Debug** and select:
`Spring Boot-SalaryManagementApplication<salary-management>`

The server will start at:
- **API Base URL**: `http://localhost:8080`
- **H2 Web Console**: `http://localhost:8080/h2-console`
  - **JDBC URL**: `jdbc:h2:mem:testdb`
  - **User Name**: `sa`
  - **Password**: *(leave blank)*

On startup, `DataSeeder` automatically populates **10,000 employees** with realistic names, departments, country-calibrated salary scales, and currencies.

---

## 📊 REST API Endpoints

### 1. Employee Management (`/api/v1/employees`)

| Method | Endpoint | Query Parameters | Description |
|---|---|---|---|
| `GET` | `/api/v1/employees` | `page`, `size`, `search`, `department`, `country` | Paginated search and filtering |
| `GET` | `/api/v1/employees/{id}` | — | Single employee details |
| `POST` | `/api/v1/employees` | Body: JSON | Create a new employee |
| `PUT` | `/api/v1/employees/{id}` | Body: JSON | Update employee (creates salary audit trail if salary changed) |
| `DELETE` | `/api/v1/employees/{id}` | — | Soft-delete employee (`isActive = false`) |
| `GET` | `/api/v1/employees/{id}/salary-history` | — | View audit history of all salary revisions |

### 2. Salary Analytics (`/api/v1/analytics`)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/analytics/summary` | Org-wide salary metrics (total payroll, average, min, max, median, active headcount) |
| `GET` | `/api/v1/analytics/by-department` | Average, min, max salary and employee count per department |
| `GET` | `/api/v1/analytics/by-country` | Average, min, max salary, currency, and count per country |
| `GET` | `/api/v1/analytics/distribution` | Salary distribution across defined compensation brackets |
| `GET` | `/api/v1/analytics/headcount` | Headcount breakdowns by department, country, and seniority level |

---

## 🧪 Testing

The test suite covers:
- **Unit Tests**:
  - `EmployeeServiceTest`: Validation, CRUD, duplicate detection, salary audit logging, soft deletes.
  - `AnalyticsServiceTest`: Aggregation math, median calculation, bucket distributions.
- **Integration Tests**:
  - `EmployeeControllerIntegrationTest`: Full Spring Boot MockMvc tests against H2 test database.
  - `AnalyticsControllerIntegrationTest`: Analytics API endpoints tested against seeded data.
