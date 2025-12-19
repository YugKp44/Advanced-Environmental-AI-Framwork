package com.ecoai.service;

import com.ecoai.dto.ForecastDTO;
import com.ecoai.dto.TrendDataPointDTO;
import com.ecoai.entity.EnergyUsage;
import com.ecoai.repository.CarbonEmissionRepository;
import com.ecoai.repository.CompanyRepository;
import com.ecoai.repository.EnergyUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics & Forecasting Service.
 * Feature 6: Historical Trends and Simple Forecasting
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsService {

    private final EnergyUsageRepository energyUsageRepository;
    private final CarbonEmissionRepository carbonEmissionRepository;
    private final CompanyRepository companyRepository;

    /**
     * Get historical trend data for charts.
     */
    public List<TrendDataPointDTO> getHistoricalTrends(UUID companyId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        List<EnergyUsage> usages = energyUsageRepository.findByCompanyIdAndUsageDateBetween(
                companyId, startDate, endDate);

        // Group by month
        Map<String, List<EnergyUsage>> byMonth = usages.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getUsageDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        List<TrendDataPointDTO> trends = new ArrayList<>();
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("MMM yyyy");

        // Generate data points for each month
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = endDate.minusMonths(i).withDayOfMonth(1);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            List<EnergyUsage> monthUsages = byMonth.getOrDefault(monthKey, Collections.emptyList());

            BigDecimal totalEnergy = monthUsages.stream()
                    .map(EnergyUsage::getTotalKwh)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal aiEnergy = monthUsages.stream()
                    .map(u -> u.getAiAttributedKwh() != null ? u.getAiAttributedKwh() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = monthUsages.stream()
                    .map(u -> u.getCost() != null ? u.getCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCo2e = monthUsages.stream()
                    .filter(u -> u.getCarbonEmission() != null)
                    .map(u -> u.getCarbonEmission().getCo2eKg())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trends.add(TrendDataPointDTO.builder()
                    .date(monthDate)
                    .period(monthDate.format(displayFormat))
                    .totalEnergyKwh(totalEnergy)
                    .aiEnergyKwh(aiEnergy)
                    .co2eKg(totalCo2e)
                    .cost(totalCost)
                    .build());
        }

        return trends;
    }

    /**
     * Simple linear forecasting for next N months.
     */
    public List<ForecastDTO> forecastUsage(UUID companyId, int monthsAhead) {
        // Get last 6 months of data for trend calculation
        List<TrendDataPointDTO> historicalData = getHistoricalTrends(companyId, 6);

        if (historicalData.isEmpty()) {
            return Collections.emptyList();
        }

        // Calculate average monthly growth rate
        BigDecimal avgAiKwh = historicalData.stream()
                .map(TrendDataPointDTO::getAiEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(historicalData.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgCo2e = historicalData.stream()
                .map(TrendDataPointDTO::getCo2eKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(historicalData.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgCost = historicalData.stream()
                .map(TrendDataPointDTO::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(historicalData.size()), 2, RoundingMode.HALF_UP);

        // Calculate simple growth rate (compare first half to second half)
        int midpoint = historicalData.size() / 2;
        BigDecimal firstHalfAvg = historicalData.subList(0, midpoint).stream()
                .map(TrendDataPointDTO::getAiEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(midpoint > 0 ? midpoint : 1), 4, RoundingMode.HALF_UP);

        BigDecimal secondHalfAvg = historicalData.subList(midpoint, historicalData.size()).stream()
                .map(TrendDataPointDTO::getAiEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(historicalData.size() - midpoint), 4, RoundingMode.HALF_UP);

        BigDecimal growthRate = firstHalfAvg.compareTo(BigDecimal.ZERO) > 0
                ? secondHalfAvg.subtract(firstHalfAvg).divide(firstHalfAvg, 4, RoundingMode.HALF_UP)
                : new BigDecimal("0.05"); // Default 5% growth

        // Generate forecasts
        List<ForecastDTO> forecasts = new ArrayList<>();
        LocalDate forecastStart = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("MMM yyyy");

        BigDecimal currentKwh = avgAiKwh;
        BigDecimal currentCo2e = avgCo2e;
        BigDecimal currentCost = avgCost;

        for (int i = 0; i < monthsAhead; i++) {
            LocalDate forecastDate = forecastStart.plusMonths(i);

            // Apply growth
            currentKwh = currentKwh.multiply(BigDecimal.ONE.add(growthRate))
                    .setScale(2, RoundingMode.HALF_UP);
            currentCo2e = currentCo2e.multiply(BigDecimal.ONE.add(growthRate))
                    .setScale(2, RoundingMode.HALF_UP);
            currentCost = currentCost.multiply(BigDecimal.ONE.add(growthRate))
                    .setScale(2, RoundingMode.HALF_UP);

            // Confidence interval (±15%)
            BigDecimal confidenceLow = currentKwh.multiply(new BigDecimal("0.85"))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal confidenceHigh = currentKwh.multiply(new BigDecimal("1.15"))
                    .setScale(2, RoundingMode.HALF_UP);

            forecasts.add(ForecastDTO.builder()
                    .date(forecastDate)
                    .period(forecastDate.format(displayFormat))
                    .predictedAiKwh(currentKwh)
                    .predictedCo2eKg(currentCo2e)
                    .predictedCost(currentCost)
                    .confidenceLow(confidenceLow)
                    .confidenceHigh(confidenceHigh)
                    .isProjection(true)
                    .build());
        }

        return forecasts;
    }

    /**
     * Year-over-year comparison.
     */
    public Map<String, Object> getYearOverYearComparison(UUID companyId) {
        LocalDate now = LocalDate.now();
        LocalDate thisYearStart = now.withDayOfYear(1);
        LocalDate lastYearStart = thisYearStart.minusYears(1);
        LocalDate lastYearEnd = thisYearStart.minusDays(1);

        // This year totals
        BigDecimal thisYearAiKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, thisYearStart, now);
        BigDecimal thisYearTotalKwh = energyUsageRepository.sumTotalKwhByCompanyAndDateRange(
                companyId, thisYearStart, now);

        // Last year same period
        LocalDate lastYearSamePeriodEnd = now.minusYears(1);
        BigDecimal lastYearAiKwh = energyUsageRepository.sumAiKwhByCompanyAndDateRange(
                companyId, lastYearStart, lastYearSamePeriodEnd);
        BigDecimal lastYearTotalKwh = energyUsageRepository.sumTotalKwhByCompanyAndDateRange(
                companyId, lastYearStart, lastYearSamePeriodEnd);

        // Calculate changes
        BigDecimal aiKwhChange = calculatePercentChange(lastYearAiKwh, thisYearAiKwh);
        BigDecimal totalKwhChange = calculatePercentChange(lastYearTotalKwh, thisYearTotalKwh);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("thisYearAiKwh", thisYearAiKwh != null ? thisYearAiKwh : BigDecimal.ZERO);
        comparison.put("thisYearTotalKwh", thisYearTotalKwh != null ? thisYearTotalKwh : BigDecimal.ZERO);
        comparison.put("lastYearAiKwh", lastYearAiKwh != null ? lastYearAiKwh : BigDecimal.ZERO);
        comparison.put("lastYearTotalKwh", lastYearTotalKwh != null ? lastYearTotalKwh : BigDecimal.ZERO);
        comparison.put("aiKwhChangePercent", aiKwhChange);
        comparison.put("totalKwhChangePercent", totalKwhChange);
        comparison.put("period", thisYearStart.getYear() + " vs " + lastYearStart.getYear());

        return comparison;
    }

    private BigDecimal calculatePercentChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue == null || oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return newValue != null && newValue.compareTo(BigDecimal.ZERO) > 0
                    ? new BigDecimal("100")
                    : BigDecimal.ZERO;
        }
        if (newValue == null) {
            return new BigDecimal("-100");
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
