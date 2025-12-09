package com.ts.rm.domain.engineer.controller;

import com.ts.rm.domain.engineer.dto.EngineerDto;
import com.ts.rm.domain.engineer.service.EngineerService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import com.ts.rm.global.security.TokenInfo;
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
@RestController
@RequestMapping("/api/engineers")
@RequiredArgsConstructor
public class EngineerController implements EngineerControllerDocs {

    private final EngineerService engineerService;

    /**
     * 엔지니어 생성
     *
     * @param request 엔지니어 생성 요청
     * @return 생성된 엔지니어 정보
     */
    @Override
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
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> getEngineerById(@PathVariable Long id) {
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
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EngineerDto.ListResponse>>> getEngineers(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable) {
        Page<EngineerDto.ListResponse> response = engineerService.getEngineers(departmentId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 엔지니어 정보 수정
     *
     * @param id 엔지니어 ID
     * @param request 수정 요청
     * @return 수정된 엔지니어 정보
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> updateEngineer(
            @PathVariable Long id,
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
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEngineer(@PathVariable Long id) {
        engineerService.deleteEngineer(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
