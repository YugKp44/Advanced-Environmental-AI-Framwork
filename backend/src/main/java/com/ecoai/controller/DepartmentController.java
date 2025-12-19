package com.ecoai.controller;

import com.ecoai.dto.DepartmentDTO;
import com.ecoai.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Department management.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping("/companies/{companyId}/departments")
    public ResponseEntity<DepartmentDTO> createDepartment(
            @PathVariable UUID companyId,
            @RequestBody DepartmentDTO dto) {
        DepartmentDTO created = departmentService.createDepartment(companyId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/companies/{companyId}/departments")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(departmentService.getDepartmentsByCompany(companyId));
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentDTO> getDepartment(@PathVariable UUID id) {
        return ResponseEntity.ok(departmentService.getDepartment(id));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable UUID id,
            @RequestBody DepartmentDTO dto) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
