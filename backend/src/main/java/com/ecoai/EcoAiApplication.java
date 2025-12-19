package com.ecoai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Energy & Carbon Management Framework
 * 
 * A company-level framework for tracking, attributing, forecasting,
 * and optimizing electricity usage and carbon emissions caused by AI workloads.
 * 
 * This enables data-driven sustainability and ESG decisions.
 * 
 * @author EcoAI Team
 * @version 1.0.0
 */
@SpringBootApplication
public class EcoAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoAiApplication.class, args);
        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════════════╗\n" +
                "║     🌱 EcoAI Energy & Carbon Management Framework 🌱          ║\n" +
                "║                                                               ║\n" +
                "║     Server started successfully!                              ║\n" +
                "║     API Base URL: http://localhost:8080/api                   ║\n" +
                "║     H2 Console: http://localhost:8080/h2-console              ║\n" +
                "╚═══════════════════════════════════════════════════════════════╝\n");
    }
}
