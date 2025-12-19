package com.ecoai.service;

import com.ecoai.dto.DepartmentDTO;
import com.ecoai.entity.Company;
import com.ecoai.entity.Department;
import com.ecoai.repository.CompanyRepository;
import com.ecoai.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Department management operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;

    public DepartmentDTO createDepartment(UUID companyId, DepartmentDTO dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        Department department = Department.builder()
                .company(company)
                .name(dto.getName())
                .team(dto.getTeam())
                .product(dto.getProduct())
                .description(dto.getDescription())
                .aiUsageWeight(dto.getAiUsageWeight())
                .employeeCount(dto.getEmployeeCount())
                .build();

        Department saved = departmentRepository.save(department);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartmentsByCompany(UUID companyId) {
        return departmentRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDTO getDepartment(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));
        return mapToDTO(department);
    }

    public DepartmentDTO updateDepartment(UUID id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));

        if (dto.getName() != null)
            department.setName(dto.getName());
        if (dto.getTeam() != null)
            department.setTeam(dto.getTeam());
        if (dto.getProduct() != null)
            department.setProduct(dto.getProduct());
        if (dto.getDescription() != null)
            department.setDescription(dto.getDescription());
        if (dto.getAiUsageWeight() != null)
            department.setAiUsageWeight(dto.getAiUsageWeight());
        if (dto.getEmployeeCount() != null)
            department.setEmployeeCount(dto.getEmployeeCount());

        Department saved = departmentRepository.save(department);
        return mapToDTO(saved);
    }

    public void deleteDepartment(UUID id) {
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO mapToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .companyId(department.getCompany().getId())
                .name(department.getName())
                .team(department.getTeam())
                .product(department.getProduct())
                .description(department.getDescription())
                .aiUsageWeight(department.getAiUsageWeight())
                .employeeCount(department.getEmployeeCount())
                .build();
    }
}
