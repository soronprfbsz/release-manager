package com.ts.rm.domain.customer.controller;

import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.service.CustomerService;
import com.ts.rm.global.common.response.ApiResponse;
import com.ts.rm.global.common.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer Controller
 *
 * <p>고객사 관리 REST API
 */
@Tag(name = "Customer", description = "고객사 관리 API")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@SwaggerResponse
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 고객사 생성
     *
     * @param request 고객사 생성 요청
     * @return 생성된 고객사 정보
     */
    @Operation(summary = "고객사 생성", description = "새로운 고객사를 생성합니다")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto.DetailResponse>> createCustomer(
            @Valid @RequestBody CustomerDto.CreateRequest request) {
        CustomerDto.DetailResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 고객사 조회 (ID)
     *
     * @param id 고객사 ID
     * @return 고객사 상세 정보
     */
    @Operation(summary = "고객사 조회 (ID)", description = "ID로 고객사 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto.DetailResponse>> getCustomerById(
            @Parameter(description = "고객사 ID", required = true) @PathVariable Long id) {
        CustomerDto.DetailResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 고객사 목록 조회
     *
     * @param isActive 활성화 여부 필터 (true: 활성화만, false: 비활성화만, null: 전체)
     * @param keyword  고객사명 검색 키워드 (optional)
     * @return 고객사 목록
     */
    @Operation(summary = "고객사 목록 조회",
            description = "고객사 목록을 조회합니다. isActive로 활성화 여부 필터링, keyword로 고객사명 검색 가능")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDto.DetailResponse>>> getCustomers(
            @Parameter(description = "활성화 여부 (true: 활성화만, false: 비활성화만, null: 전체)")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "고객사명 검색 키워드")
            @RequestParam(required = false) String keyword) {
        List<CustomerDto.DetailResponse> response = customerService.getCustomers(isActive,
                keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 고객사 정보 수정
     *
     * @param id      고객사 ID
     * @param request 수정 요청
     * @return 수정된 고객사 정보
     */
    @Operation(summary = "고객사 정보 수정", description = "고객사 정보를 수정합니다")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto.DetailResponse>> updateCustomer(
            @Parameter(description = "고객사 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CustomerDto.UpdateRequest request) {
        CustomerDto.DetailResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 고객사 삭제
     *
     * @param id 고객사 ID
     * @return 성공 응답
     */
    @Operation(summary = "고객사 삭제", description = "고객사를 삭제합니다")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "고객사 ID", required = true) @PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 고객사 활성화 상태 변경
     *
     * @param id       고객사 ID
     * @param isActive 활성화 여부 (true: 활성화, false: 비활성화)
     * @return 성공 응답
     */
    @Operation(summary = "고객사 활성화 상태 변경", description = "고객사의 활성화 상태를 변경합니다")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateCustomerStatus(
            @Parameter(description = "고객사 ID", required = true) @PathVariable Long id,
            @Parameter(description = "활성화 여부", required = true) @RequestParam Boolean isActive) {
        customerService.updateCustomerStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
