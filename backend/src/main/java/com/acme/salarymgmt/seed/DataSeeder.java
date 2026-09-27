package com.acme.salarymgmt.seed;

import com.acme.salarymgmt.model.Employee;
import com.acme.salarymgmt.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds the database with 10,000 realistic employee records on first startup.
 * Skips if employees already exist. Can be disabled via app.seeding.enabled=false.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int TOTAL_EMPLOYEES = 10_000;
    private static final int BATCH_SIZE = 500;

    @Value("${app.seeding.enabled:true}")
    private boolean seedingEnabled;

    private final EmployeeRepository employeeRepository;
    private final Random random = new Random(42); // Fixed seed for reproducibility

    public DataSeeder(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedingEnabled) {
            log.info("Data seeding is disabled");
            return;
        }

        long existingCount = employeeRepository.count();
        if (existingCount > 0) {
            log.info("Database already contains {} employees — skipping seed", existingCount);
            return;
        }

        log.info("Starting data seed: generating {} employees...", TOTAL_EMPLOYEES);
        long startTime = System.currentTimeMillis();

        List<Employee> batch = new ArrayList<>(BATCH_SIZE);
        for (int i = 1; i <= TOTAL_EMPLOYEES; i++) {
            batch.add(generateEmployee(i));
            if (batch.size() == BATCH_SIZE) {
                employeeRepository.saveAll(batch);
                batch.clear();
                if (i % 2000 == 0) {
                    log.info("  Seeded {}/{} employees...", i, TOTAL_EMPLOYEES);
                }
            }
        }
        // Save remaining
        if (!batch.isEmpty()) {
            employeeRepository.saveAll(batch);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Data seed complete: {} employees created in {}ms", TOTAL_EMPLOYEES, elapsed);
    }

    private Employee generateEmployee(int index) {
        String firstName = randomFrom(FIRST_NAMES);
        String lastName = randomFrom(LAST_NAMES);
        String department = randomFrom(DEPARTMENTS);
        String[] countryInfo = randomFrom(COUNTRIES); // [country, currency]
        String designation = weightedDesignation();
        BigDecimal salary = generateSalary(countryInfo[0], department, designation);
        LocalDate hireDate = randomHireDate();

        LocalDateTime now = LocalDateTime.now();
        return Employee.builder()
                .employeeCode(String.format("EMP-%05d", index))
                .firstName(firstName)
                .lastName(lastName)
                .email(String.format("%s.%s.%d@acme.com",
                        firstName.toLowerCase(), lastName.toLowerCase(), index))
                .department(department)
                .designation(designation)
                .country(countryInfo[0])
                .currency(countryInfo[1])
                .salary(salary)
                .hireDate(hireDate)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Generates a realistic salary based on country, department, and designation.
     * Base salary varies by country (cost-of-living adjusted), then department
     * multiplier is applied, then designation multiplier.
     */
    private BigDecimal generateSalary(String country, String department, String designation) {
        double baseSalary = getCountryBaseSalary(country);
        double deptMultiplier = getDepartmentMultiplier(department);
        double designationMultiplier = getDesignationMultiplier(designation);

        // Add some randomness (±20%)
        double variance = 0.8 + (random.nextDouble() * 0.4);

        double salary = baseSalary * deptMultiplier * designationMultiplier * variance;
        return BigDecimal.valueOf(salary).setScale(4, RoundingMode.HALF_UP);
    }

    private double getCountryBaseSalary(String country) {
        return switch (country) {
            case "USA" -> 75000;
            case "UK" -> 55000;
            case "Germany" -> 60000;
            case "Canada" -> 65000;
            case "Australia" -> 70000;
            case "Japan" -> 6000000;  // JPY
            case "India" -> 1200000;  // INR
            case "Brazil" -> 120000;  // BRL
            default -> 50000;
        };
    }

    private double getDepartmentMultiplier(String department) {
        return switch (department) {
            case "Engineering" -> 1.20;
            case "Product" -> 1.15;
            case "Design" -> 1.05;
            case "Finance" -> 1.10;
            case "Sales" -> 1.05;
            case "Marketing" -> 1.00;
            case "HR" -> 0.95;
            case "Legal" -> 1.10;
            case "Operations" -> 0.95;
            case "Support" -> 0.85;
            default -> 1.00;
        };
    }

    private double getDesignationMultiplier(String designation) {
        return switch (designation) {
            case "Junior" -> 0.70;
            case "Mid-Level" -> 1.00;
            case "Senior" -> 1.35;
            case "Lead" -> 1.60;
            case "Manager" -> 1.80;
            case "Director" -> 2.20;
            case "VP" -> 2.80;
            default -> 1.00;
        };
    }

    /**
     * Weighted designation distribution: more mid-level, fewer executives.
     */
    private String weightedDesignation() {
        int roll = random.nextInt(100);
        if (roll < 20) return "Junior";       // 20%
        if (roll < 50) return "Mid-Level";    // 30%
        if (roll < 75) return "Senior";       // 25%
        if (roll < 85) return "Lead";         // 10%
        if (roll < 93) return "Manager";      //  8%
        if (roll < 98) return "Director";     //  5%
        return "VP";                          //  2%
    }

    private LocalDate randomHireDate() {
        // Random date between 2010-01-01 and 2026-09-01
        long minDay = LocalDate.of(2010, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(2026, 9, 1).toEpochDay();
        long randomDay = minDay + (long) (random.nextDouble() * (maxDay - minDay));
        return LocalDate.ofEpochDay(randomDay);
    }

    private <T> T randomFrom(T[] array) {
        return array[random.nextInt(array.length)];
    }

    // ── Static Data ─────────────────────────────────────────────────────

    private static final String[] FIRST_NAMES = {
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
            "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Christopher", "Karen", "Charles", "Lisa", "Daniel", "Nancy",
            "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
            "Steven", "Kimberly", "Andrew", "Emily", "Paul", "Donna", "Joshua", "Michelle",
            "Kenneth", "Carol", "Kevin", "Amanda", "Brian", "Dorothy", "George", "Melissa",
            "Timothy", "Deborah", "Ronald", "Stephanie", "Edward", "Rebecca", "Jason", "Sharon",
            "Raj", "Priya", "Amit", "Sunita", "Hiroshi", "Yuki", "Takeshi", "Sakura",
            "Hans", "Anna", "Klaus", "Greta", "Pierre", "Marie", "Carlos", "Sofia",
            "Liam", "Emma", "Noah", "Olivia", "Aiden", "Ava", "Lucas", "Mia"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
            "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
            "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
            "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen",
            "Hill", "Flores", "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera",
            "Kumar", "Sharma", "Patel", "Singh", "Gupta", "Tanaka", "Yamamoto", "Suzuki",
            "Mueller", "Schmidt", "Fischer", "Weber", "Dubois", "Laurent", "Santos", "Silva",
            "O'Brien", "Murphy", "Chen", "Wang", "Kim", "Park", "Johansson", "Berg"
    };

    private static final String[] DEPARTMENTS = {
            "Engineering", "Sales", "Marketing", "Finance", "HR",
            "Operations", "Legal", "Support", "Product", "Design"
    };

    /**
     * Each entry: [country, currency]
     */
    private static final String[][] COUNTRIES = {
            {"USA", "USD"},
            {"UK", "GBP"},
            {"India", "INR"},
            {"Germany", "EUR"},
            {"Australia", "AUD"},
            {"Canada", "CAD"},
            {"Japan", "JPY"},
            {"Brazil", "BRL"}
    };
}
