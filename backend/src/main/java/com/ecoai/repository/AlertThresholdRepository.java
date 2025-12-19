package com.ecoai.repository;

import com.ecoai.entity.AlertThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, UUID> {
    List<AlertThreshold> findByCompanyId(UUID companyId);

    List<AlertThreshold> findByCompanyIdAndActive(UUID companyId, Boolean active);

    List<AlertThreshold> findByCompanyIdAndMetricType(UUID companyId, AlertThreshold.MetricType metricType);
}
