package com.ts.rm.domain.releaseversion.controller;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHistory;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionHistoryService;
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
 * ReleaseVersionHistory Controller
 *
 * <p>릴리즈 버전 이력 API
 */
@Tag(name = "ReleaseVersionHistory", description = "릴리즈 버전 이력 API")
@Slf4j
@RestController
@RequestMapping("/api/release-version-history")
@RequiredArgsConstructor
public class ReleaseVersionHistoryController {

    private final ReleaseVersionHistoryService releaseVersionHistoryService;

    /**
     * 릴리즈 버전 이력 조회 (ID)
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @return 릴리즈 버전 이력
     */
    @Operation(summary = "릴리즈 버전 이력 조회", description = "릴리즈 버전 ID로 이력을 조회합니다.")
    @GetMapping("/{releaseVersionId}")
    public ResponseEntity<ApiResponse<ReleaseVersionHistory>> getReleaseVersionHistory(
            @PathVariable String releaseVersionId) {

        log.info("GET /api/release-version-history/{}", releaseVersionId);

        ReleaseVersionHistory releaseVersionHistory = releaseVersionHistoryService.getReleaseVersionHistoryById(releaseVersionId);
        return ResponseEntity.ok(ApiResponse.success(releaseVersionHistory));
    }

    /**
     * 전체 릴리즈 버전 이력 조회
     *
     * @param appliedOnly true인 경우 적용된 버전만 조회
     * @return 릴리즈 버전 이력 목록
     */
    @Operation(summary = "전체 릴리즈 버전 이력 조회", description = "전체 릴리즈 버전 이력을 조회합니다. appliedOnly=true인 경우 적용된 버전만 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReleaseVersionHistory>>> getAllReleaseVersionHistory(
            @RequestParam(required = false, defaultValue = "false") boolean appliedOnly) {

        log.info("GET /api/release-version-history?appliedOnly={}", appliedOnly);

        List<ReleaseVersionHistory> releaseVersionHistoryList = appliedOnly
                ? releaseVersionHistoryService.getAppliedReleaseVersionHistory()
                : releaseVersionHistoryService.getAllReleaseVersionHistory();

        return ResponseEntity.ok(ApiResponse.success(releaseVersionHistoryList));
    }

    /**
     * 표준 버전으로 조회
     *
     * @param standardVersion 표준 버전
     * @return 릴리즈 버전 이력 목록
     */
    @Operation(summary = "표준 버전으로 조회", description = "표준 버전으로 릴리즈 버전 이력을 조회합니다.")
    @GetMapping("/standard/{standardVersion}")
    public ResponseEntity<ApiResponse<List<ReleaseVersionHistory>>> getReleaseVersionHistoryByStandardVersion(
            @PathVariable String standardVersion) {

        log.info("GET /api/release-version-history/standard/{}", standardVersion);

        List<ReleaseVersionHistory> releaseVersionHistoryList = releaseVersionHistoryService.getReleaseVersionHistoryByStandardVersion(
                standardVersion);

        return ResponseEntity.ok(ApiResponse.success(releaseVersionHistoryList));
    }
}
