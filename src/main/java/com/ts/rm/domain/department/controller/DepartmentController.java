package com.ts.rm.domain.department.controller;

import com.ts.rm.domain.department.dto.DepartmentDto;
import com.ts.rm.domain.department.service.DepartmentService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "부서", description = "부서 관리 API")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 전체 부서 목록 조회
     *
     * @return 부서 목록
     */
    @Operation(summary = "부서 목록 조회",
            description = "전체 부서 목록을 조회합니다. 셀렉트 박스 등에서 활용 가능합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> getAllDepartments() {
        List<DepartmentDto.Response> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
}
