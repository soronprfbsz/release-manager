package com.ts.rm.domain.engineer.controller;

import com.ts.rm.domain.engineer.dto.EngineerDto;
import com.ts.rm.domain.engineer.service.EngineerService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import com.ts.rm.global.security.TokenInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Engineer Controller
 *
 * <p>엔지니어 관리 REST API
 */
@Slf4j
@Tag(name = "엔지니어", description = "엔지니어 관리 API")
@RestController
@RequestMapping("/api/engineers")
@RequiredArgsConstructor
public class EngineerController {

    private final EngineerService engineerService;

    /**
     * 엔지니어 생성
     *
     * @param request 엔지니어 생성 요청
     * @return 생성된 엔지니어 정보
     */
    @Operation(summary = "엔지니어 생성",
            description = "새로운 엔지니어를 등록합니다. Authorization 헤더에 JWT 토큰 필수 (Bearer {token})")
    @PostMapping
    public ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> createEngineer(
            @Valid @RequestBody EngineerDto.CreateRequest request) {

        log.info("엔지니어 생성 요청 - name: {}, email: {}",
                request.engineerName(), request.engineerEmail());

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        log.info("엔지니어 생성자 정보: email={}, role={}", tokenInfo.email(), tokenInfo.role());

        EngineerDto.DetailResponse response = engineerService.createEngineer(request, tokenInfo.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 엔지니어 조회 (ID)
     *
     * @param id 엔지니어 ID
     * @return 엔지니어 상세 정보
     */
    @Operation(summary = "엔지니어 조회 (ID)", description = "ID로 엔지니어 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> getEngineerById(
            @Parameter(description = "엔지니어 ID", required = true) @PathVariable Long id) {
        EngineerDto.DetailResponse response = engineerService.getEngineerById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 엔지니어 목록 조회 (페이징)
     *
     * @param departmentId 부서 ID 필터 (optional)
     * @param keyword 이름 검색 키워드 (optional)
     * @param pageable 페이징 정보
     * @return 엔지니어 페이지
     */
    @Operation(summary = "엔지니어 목록 조회",
            description = "엔지니어 목록을 조회합니다. departmentId로 부서 필터링, keyword로 이름 검색 가능. page, size, sort 파라미터 사용 가능")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EngineerDto.DetailResponse>>> getEngineers(
            @Parameter(description = "부서 ID 필터")
            @RequestParam(required = false) Long departmentId,
            @Parameter(description = "이름 검색 키워드")
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<EngineerDto.DetailResponse> response = engineerService.getEngineers(departmentId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 엔지니어 정보 수정
     *
     * @param id 엔지니어 ID
     * @param request 수정 요청
     * @return 수정된 엔지니어 정보
     */
    @Operation(summary = "엔지니어 정보 수정",
            description = "엔지니어 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> updateEngineer(
            @Parameter(description = "엔지니어 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody EngineerDto.UpdateRequest request) {

        log.info("엔지니어 수정 요청 - id: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        log.info("엔지니어 수정자 정보: email={}, role={}", tokenInfo.email(), tokenInfo.role());

        EngineerDto.DetailResponse response = engineerService.updateEngineer(id, request, tokenInfo.email());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 엔지니어 삭제
     *
     * @param id 엔지니어 ID
     * @return 성공 응답
     */
    @Operation(summary = "엔지니어 삭제", description = "엔지니어를 삭제합니다")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEngineer(
            @Parameter(description = "엔지니어 ID", required = true) @PathVariable Long id) {
        engineerService.deleteEngineer(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
