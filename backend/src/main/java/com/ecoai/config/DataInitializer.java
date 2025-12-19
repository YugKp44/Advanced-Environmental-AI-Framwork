package com.ecoai.config;

import com.ecoai.entity.*;
import com.ecoai.repository.*;
import com.ecoai.service.AttributionEngineService;
import com.ecoai.service.CarbonCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

/**
 * Sample data initializer for development and demo purposes.
 * Creates a sample company with departments and historical energy data.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EnergyUsageRepository energyUsageRepository;
    private final AlertThresholdRepository alertThresholdRepository;
    private final AttributionEngineService attributionEngineService;
    private final CarbonCalculationService carbonCalculationService;

    @Bean
    @Profile("!test")
    public CommandLineRunner initSampleData() {
        return args -> {
            // Only initialize if no companies exist
            if (companyRepository.count() > 0) {
                log.info("Sample data already exists, skipping initialization");
                return;
            }

            log.info("Initializing sample data for demo...");

            // Create sample company
            Company company = Company.builder()
                    .name("TechCorp AI Solutions")
                    .industry("Technology")
                    .country("United States")
                    .region("US")
                    .baseAiPercentage(new BigDecimal("0.35"))
                    .electricityCostPerKwh(new BigDecimal("0.12"))
                    .currency("USD")
                    .build();
            company = companyRepository.save(company);
            log.info("Created company: {} with ID: {}", company.getName(), company.getId());

            // Create departments
            Department mlTeam = createDepartment(company, "Machine Learning", "ML Engineering", "AI Platform",
                    new BigDecimal("0.85"));
            Department dataTeam = createDepartment(company, "Data Science", "Analytics", "Insights Engine",
                    new BigDecimal("0.65"));
            Department devTeam = createDepartment(company, "Software Development", "Platform", "Core Product",
                    new BigDecimal("0.30"));
            Department opsTeam = createDepartment(company, "Operations", "Infrastructure", null,
                    new BigDecimal("0.20"));

            Department[] departments = { mlTeam, dataTeam, devTeam, opsTeam };

            // Create historical energy data for last 6 months
            Random random = new Random(42); // Fixed seed for reproducibility
            LocalDate today = LocalDate.now();

            for (int month = 5; month >= 0; month--) {
                LocalDate monthStart = today.minusMonths(month).withDayOfMonth(1);
                int daysInMonth = monthStart.lengthOfMonth();

                // Simulate slight growth trend
                double growthFactor = 1.0 + (5 - month) * 0.03;

                for (int day = 1; day <= daysInMonth
                        && monthStart.plusDays(day - 1).isBefore(today.plusDays(1)); day++) {
                    LocalDate usageDate = monthStart.plusDays(day - 1);

                    // Create energy usage for each department
                    for (Department dept : departments) {
                        // Base usage varies by department
                        double baseUsage = switch (dept.getName()) {
                            case "Machine Learning" -> 250 + random.nextDouble() * 100;
                            case "Data Science" -> 150 + random.nextDouble() * 60;
                            case "Software Development" -> 100 + random.nextDouble() * 40;
                            case "Operations" -> 80 + random.nextDouble() * 30;
                            default -> 50 + random.nextDouble() * 20;
                        };

                        // Apply growth and some randomness
                        double usage = baseUsage * growthFactor * (0.9 + random.nextDouble() * 0.2);
                        // Weekends have lower usage
                        if (usageDate.getDayOfWeek().getValue() >= 6) {
                            usage *= 0.4;
                        }

                        createEnergyUsage(company, dept, new BigDecimal(usage), usageDate);
                    }
                }
            }

            // Create alert thresholds
            createAlertThreshold(company, AlertThreshold.MetricType.AI_USAGE_KWH,
                    new BigDecimal("15000"), "Monthly AI energy usage approaching limit");
            createAlertThreshold(company, AlertThreshold.MetricType.CARBON_EMISSION_KG,
                    new BigDecimal("6000"), "Monthly carbon emissions near budget");

            log.info("Sample data initialization complete!");
            log.info("Company ID for testing: {}", company.getId());
        };
    }

    private Department createDepartment(Company company, String name, String team, String product,
            BigDecimal aiWeight) {
        Department dept = Department.builder()
                .company(company)
                .name(name)
                .team(team)
                .product(product)
                .aiUsageWeight(aiWeight)
                .employeeCount(10 + new Random().nextInt(40))
                .build();
        return departmentRepository.save(dept);
    }

    private void createEnergyUsage(Company company, Department department, BigDecimal totalKwh, LocalDate date) {
        BigDecimal aiKwh = attributionEngineService.calculateAiAttribution(totalKwh, company, department);
        BigDecimal cost = totalKwh.multiply(company.getElectricityCostPerKwh());

        EnergyUsage usage = EnergyUsage.builder()
                .company(company)
                .department(department)
                .totalKwh(totalKwh.setScale(2, java.math.RoundingMode.HALF_UP))
                .aiAttributedKwh(aiKwh)
                .cost(cost.setScale(2, java.math.RoundingMode.HALF_UP))
                .currency(company.getCurrency())
                .usageDate(date)
                .periodType(EnergyUsage.PeriodType.DAILY)
                .region(company.getRegion())
                .dataSource("SAMPLE_DATA")
                .build();

        usage = energyUsageRepository.save(usage);
        carbonCalculationService.calculateAndSaveEmission(usage);
    }

    private void createAlertThreshold(Company company, AlertThreshold.MetricType type,
            BigDecimal value, String message) {
        AlertThreshold threshold = AlertThreshold.builder()
                .company(company)
                .metricType(type)
                .thresholdValue(value)
                .alertMessage(message)
                .active(true)
                .build();
        alertThresholdRepository.save(threshold);
    }
}
