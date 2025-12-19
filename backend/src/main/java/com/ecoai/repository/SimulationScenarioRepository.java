package com.ecoai.repository;

import com.ecoai.entity.SimulationScenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SimulationScenarioRepository extends JpaRepository<SimulationScenario, UUID> {
    List<SimulationScenario> findByCompanyId(UUID companyId);

    List<SimulationScenario> findByCompanyIdAndSimulationType(UUID companyId, SimulationScenario.SimulationType type);

    List<SimulationScenario> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
