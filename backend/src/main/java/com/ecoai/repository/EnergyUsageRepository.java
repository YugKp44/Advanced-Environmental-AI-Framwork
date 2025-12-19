package com.ecoai.repository;

import com.ecoai.entity.EnergyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergyUsageRepository extends JpaRepository<EnergyUsage, UUID> {

    List<EnergyUsage> findByCompanyId(UUID companyId);

    List<EnergyUsage> findByCompanyIdAndUsageDateBetween(UUID companyId, LocalDate startDate, LocalDate endDate);

    List<EnergyUsage> findByCompanyIdAndRegion(UUID companyId, String region);

    List<EnergyUsage> findByDepartmentId(UUID departmentId);

    @Query("SELECT e FROM EnergyUsage e WHERE e.company.id = :companyId ORDER BY e.usageDate DESC")
    List<EnergyUsage> findByCompanyIdOrderByDateDesc(@Param("companyId") UUID companyId);

    @Query("SELECT e FROM EnergyUsage e WHERE e.company.id = :companyId AND e.usageDate >= :startDate ORDER BY e.usageDate ASC")
    List<EnergyUsage> findRecentByCompanyId(@Param("companyId") UUID companyId,
            @Param("startDate") LocalDate startDate);

    @Query("SELECT SUM(e.totalKwh) FROM EnergyUsage e WHERE e.company.id = :companyId AND e.usageDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumTotalKwhByCompanyAndDateRange(@Param("companyId") UUID companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(e.aiAttributedKwh) FROM EnergyUsage e WHERE e.company.id = :companyId AND e.usageDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAiKwhByCompanyAndDateRange(@Param("companyId") UUID companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e.department.id, SUM(e.aiAttributedKwh) FROM EnergyUsage e WHERE e.company.id = :companyId AND e.department IS NOT NULL GROUP BY e.department.id")
    List<Object[]> sumAiKwhByDepartment(@Param("companyId") UUID companyId);

    @Query("SELECT e.region, SUM(e.totalKwh), SUM(e.aiAttributedKwh) FROM EnergyUsage e WHERE e.company.id = :companyId GROUP BY e.region")
    List<Object[]> sumKwhByRegion(@Param("companyId") UUID companyId);
}
