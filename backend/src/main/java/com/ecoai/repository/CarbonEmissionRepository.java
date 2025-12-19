package com.ecoai.repository;

import com.ecoai.entity.CarbonEmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarbonEmissionRepository extends JpaRepository<CarbonEmission, UUID> {

    Optional<CarbonEmission> findByEnergyUsageId(UUID energyUsageId);

    @Query("SELECT c FROM CarbonEmission c WHERE c.energyUsage.company.id = :companyId")
    List<CarbonEmission> findByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT SUM(c.co2eKg) FROM CarbonEmission c WHERE c.energyUsage.company.id = :companyId " +
            "AND c.energyUsage.usageDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCo2eKgByCompanyAndDateRange(@Param("companyId") UUID companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT c.regionUsed, SUM(c.co2eKg) FROM CarbonEmission c WHERE c.energyUsage.company.id = :companyId GROUP BY c.regionUsed")
    List<Object[]> sumCo2eByRegion(@Param("companyId") UUID companyId);
}
