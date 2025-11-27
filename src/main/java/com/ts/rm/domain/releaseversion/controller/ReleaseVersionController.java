package com.ts.rm.domain.releaseversion.controller;

import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionService;
import com.ts.rm.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReleaseVersion Controller
 *
 * <p>릴리즈 버전 관리 REST API
 */
@Tag(name = "릴리즈 버전", description = "릴리즈 버전 관리 API")
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseVersionController {

    private final ReleaseVersionService releaseVersionService;

    /**
     * 릴리즈 버전 조회 (ID)
     *
     * @param id 버전 ID
     * @return 버전 상세 정보
     */
    @Operation(summary = "릴리즈 버전 조회 (ID)", description = "ID로 릴리즈 버전 정보를 조회합니다")
    @GetMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> getVersionById(
            @Parameter(description = "버전 ID", required = true) @PathVariable Long id) {
        ReleaseVersionDto.DetailResponse response = releaseVersionService.getVersionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 표준 릴리즈 버전 트리 조회
     *
     * @return 릴리즈 버전 트리 (계층 구조)
     */
    @Operation(summary = "표준 릴리즈 버전 트리 조회",
               description = "표준 릴리즈 버전들을 계층 구조로 조회합니다 (프론트엔드 트리 렌더링용)")
    @GetMapping("/standard/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getStandardReleaseTree() {
        ReleaseVersionDto.TreeResponse response = releaseVersionService.getStandardReleaseTree();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 커스텀 릴리즈 버전 트리 조회
     *
     * @param customerCode 고객사 코드
     * @return 릴리즈 버전 트리 (계층 구조)
     */
    @Operation(summary = "커스텀 릴리즈 버전 트리 조회",
               description = "특정 고객사의 커스텀 릴리즈 버전들을 계층 구조로 조회합니다 (프론트엔드 트리 렌더링용)")
    @GetMapping("/custom/{customer-code}/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getCustomReleaseTree(
            @Parameter(description = "고객사 코드", required = true, example = "company_a")
            @PathVariable("customer-code") String customerCode) {
        ReleaseVersionDto.TreeResponse response = releaseVersionService.getCustomReleaseTree(customerCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
