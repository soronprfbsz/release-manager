package com.ts.rm.domain.release.controller;

import com.ts.rm.domain.release.entity.VersionHistory;
import com.ts.rm.domain.release.service.VersionHistoryService;
import com.ts.rm.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * VersionHistory Controller
 *
 * <p>버전 이력 API
 */
@Tag(name = "VersionHistory", description = "버전 이력 API")
@Slf4j
@RestController
@RequestMapping("/api/version-history")
@RequiredArgsConstructor
public class VersionHistoryController {

    private final VersionHistoryService versionHistoryService;

    /**
     * 버전 이력 조회 (ID)
     *
     * @param versionId 버전 ID
     * @return 버전 이력
     */
    @Operation(summary = "버전 이력 조회", description = "버전 ID로 버전 이력을 조회합니다.")
    @GetMapping("/{versionId}")
    public ResponseEntity<ApiResponse<VersionHistory>> getVersionHistory(
            @PathVariable String versionId) {

        log.info("GET /api/version-history/{}", versionId);

        VersionHistory versionHistory = versionHistoryService.getVersionHistoryById(versionId);
        return ResponseEntity.ok(ApiResponse.success(versionHistory));
    }

    /**
     * 전체 버전 이력 조회
     *
     * @param appliedOnly true인 경우 적용된 버전만 조회
     * @return 버전 이력 목록
     */
    @Operation(summary = "전체 버전 이력 조회", description = "전체 버전 이력을 조회합니다. appliedOnly=true인 경우 적용된 버전만 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VersionHistory>>> getAllVersionHistory(
            @RequestParam(required = false, defaultValue = "false") boolean appliedOnly) {

        log.info("GET /api/version-history?appliedOnly={}", appliedOnly);

        List<VersionHistory> versionHistoryList = appliedOnly
                ? versionHistoryService.getAppliedVersionHistory()
                : versionHistoryService.getAllVersionHistory();

        return ResponseEntity.ok(ApiResponse.success(versionHistoryList));
    }

    /**
     * 표준 버전으로 조회
     *
     * @param standardVersion 표준 버전
     * @return 버전 이력 목록
     */
    @Operation(summary = "표준 버전으로 조회", description = "표준 버전으로 버전 이력을 조회합니다.")
    @GetMapping("/standard/{standardVersion}")
    public ResponseEntity<ApiResponse<List<VersionHistory>>> getVersionHistoryByStandardVersion(
            @PathVariable String standardVersion) {

        log.info("GET /api/version-history/standard/{}", standardVersion);

        List<VersionHistory> versionHistoryList = versionHistoryService.getVersionHistoryByStandardVersion(
                standardVersion);

        return ResponseEntity.ok(ApiResponse.success(versionHistoryList));
    }
}
