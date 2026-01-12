package com.ts.rm.domain.department.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ts.rm.domain.department.dto.DepartmentDto;
import com.ts.rm.domain.department.service.DepartmentService;
import com.ts.rm.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Department Controller
 *
 * <p>부서 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController implements DepartmentControllerDocs {

    private final DepartmentService departmentService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> getAllDepartments() {
        List<DepartmentDto.Response> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @Override
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<DepartmentDto.TreeResponse>>> getDepartmentTree() {
        List<DepartmentDto.TreeResponse> tree = departmentService.getDepartmentTree();
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDto.DetailResponse>> getDepartmentById(
            @PathVariable Long id) {
        DepartmentDto.DetailResponse department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @Override
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> getChildDepartments(
            @PathVariable Long id) {
        List<DepartmentDto.Response> children = departmentService.getChildDepartments(id);
        return ResponseEntity.ok(ApiResponse.success(children));
    }

    @Override
    @GetMapping("/{id}/descendants")
    public ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> getDescendantDepartments(
            @PathVariable Long id) {
        List<DepartmentDto.Response> descendants = departmentService.getDescendantDepartments(id);
        return ResponseEntity.ok(ApiResponse.success(descendants));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> createDepartment(
            @Valid @RequestBody DepartmentDto.CreateRequest request) {
        DepartmentDto.Response created = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDto.UpdateRequest request) {
        DepartmentDto.Response updated = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @Override
    @PutMapping("/{id}/move")
    public ResponseEntity<ApiResponse<DepartmentDto.Response>> moveDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDto.MoveRequest request) {
        DepartmentDto.Response moved = departmentService.moveDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success(moved));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
