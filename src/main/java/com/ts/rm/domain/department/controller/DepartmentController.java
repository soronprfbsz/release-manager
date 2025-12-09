package com.ts.rm.domain.department.controller;

import com.ts.rm.domain.department.dto.DepartmentDto;
import com.ts.rm.domain.department.service.DepartmentService;
import com.ts.rm.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
