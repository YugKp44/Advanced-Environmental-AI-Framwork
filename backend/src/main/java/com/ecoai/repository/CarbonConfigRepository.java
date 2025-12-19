package com.ecoai.repository;

import com.ecoai.entity.CarbonConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarbonConfigRepository extends JpaRepository<CarbonConfig, UUID> {
    List<CarbonConfig> findByCompanyId(UUID companyId);

    Optional<CarbonConfig> findByCompanyIdAndRegion(UUID companyId, String region);

    boolean existsByCompanyIdAndRegion(UUID companyId, String region);
}
