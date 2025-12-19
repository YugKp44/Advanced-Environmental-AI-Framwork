package com.ecoai.repository;

import com.ecoai.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByCompanyId(UUID companyId);

    @Query("SELECT d FROM Department d WHERE d.company.id = :companyId ORDER BY d.aiUsageWeight DESC")
    List<Department> findByCompanyIdOrderByAiWeight(UUID companyId);

    boolean existsByCompanyIdAndName(UUID companyId, String name);
}
