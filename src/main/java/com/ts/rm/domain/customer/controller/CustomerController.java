package com.ts.rm.domain.customer.controller;

import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.service.CustomerService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer Controller
 *
 * <p>고객사 관리 REST API
 */
@Slf4j
@Tag(name = "고객사", description = "고객사 관리 API")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 고객사 생성
     *
     * @param request       고객사 생성 요청
     * @param authorization JWT 토큰 (Bearer {token})
     * @return 생성된 고객사 정보
     */
    @Operation(summary = "고객사 생성",
            description = "새로운 고객사를 생성합니다. Authorization 헤더에 JWT 토큰 필수 (Bearer {token})")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto.DetailResponse>> createCustomer(
            @Valid @RequestBody CustomerDto.CreateRequest request,
            @Parameter(description = "JWT 토큰 (Bearer {token})", required = true)
            @RequestHeader("Authorization") String authorization) {

        log.info("고객사 생성 요청 - customerCode: {}, customerName: {}",
                request.customerCode(), request.customerName());

        // JWT 토큰에서 이메일 추출
        String token = extractToken(authorization);
        String createdBy = jwtTokenProvider.getEmail(token);

        log.info("고객사 생성자: {}", createdBy);

        // createdBy를 포함한 새로운 request 생성
        CustomerDto.CreateRequest requestWithCreatedBy = CustomerDto.CreateRequest.builder()
                .customerCode(request.customerCode())
                .customerName(request.customerName())
                .description(request.description())
                .isActive(request.isActive())
                .createdBy(createdBy)
                .build();

        CustomerDto.DetailResponse response = customerService.createCustomer(requestWithCreatedBy);
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
     * 고객사 목록 조회 (페이징)
     *
     * @param isActive 활성화 여부 필터 (true: 활성화만, false: 비활성화만, null: 전체)
     * @param keyword  고객사명 검색 키워드 (optional)
     * @param pageable 페이징 정보
     * @return 고객사 페이지
     */
    @Operation(summary = "고객사 목록 조회",
            description = "고객사 목록 조회합니다. isActive로 활성화 여부 필터링, keyword로 고객사명 검색 가능. page, size, sort 파라미터 사용 가능")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerDto.DetailResponse>>> getCustomers(
            @Parameter(description = "활성화 여부 (true: 활성화만, false: 비활성화만, null: 전체)")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "고객사명 검색 키워드")
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<CustomerDto.DetailResponse> response = customerService.getCustomersWithPaging(isActive, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 고객사 정보 수정
     *
     * @param id            고객사 ID
     * @param request       수정 요청
     * @param authorization JWT 토큰 (Bearer {token})
     * @return 수정된 고객사 정보
     */
    @Operation(summary = "고객사 정보 수정",
            description = "고객사 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto.DetailResponse>> updateCustomer(
            @Parameter(description = "고객사 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CustomerDto.UpdateRequest request,
            @Parameter(description = "JWT 토큰 (Bearer {token})", required = true)
            @RequestHeader("Authorization") String authorization) {

        log.info("고객사 수정 요청 - id: {}", id);

        // JWT 토큰에서 이메일 추출
        String token = extractToken(authorization);
        String updatedBy = jwtTokenProvider.getEmail(token);

        log.info("고객사 수정자: {}", updatedBy);

        // updatedBy를 포함한 새로운 request 생성
        CustomerDto.UpdateRequest requestWithUpdatedBy = CustomerDto.UpdateRequest.builder()
                .customerName(request.customerName())
                .description(request.description())
                .isActive(request.isActive())
                .updatedBy(updatedBy)
                .build();

        CustomerDto.DetailResponse response = customerService.updateCustomer(id, requestWithUpdatedBy);
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

    /**
     * Authorization 헤더에서 JWT 토큰 추출
     *
     * @param authorization "Bearer {token}" 형식
     * @return JWT 토큰
     */
    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new com.ts.rm.global.exception.BusinessException(
                    com.ts.rm.global.exception.ErrorCode.INVALID_CREDENTIALS,
                    "유효하지 않은 Authorization 헤더입니다");
        }
        return authorization.substring(7);
    }
}
