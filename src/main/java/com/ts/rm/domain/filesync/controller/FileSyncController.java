package com.ts.rm.domain.filesync.controller;

import com.ts.rm.domain.filesync.service.FileSyncService;
import com.ts.rm.domain.filesync.dto.FileSyncDto;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 파일 동기화 API 컨트롤러
 *
 * <p>파일시스템과 DB 메타데이터 간의 동기화를 관리합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/file-sync")
@RequiredArgsConstructor
@Tag(name = "파일 동기화", description = "파일시스템과 DB 메타데이터 동기화 API")
public class FileSyncController {

    private final FileSyncService fileSyncService;

    /**
     * 파일 동기화 분석
     *
     * <p>파일시스템과 DB 메타데이터를 비교하여 불일치 항목을 반환합니다.
     *
     * @param request 분석 요청 (대상, 경로 등)
     * @return 분석 결과 (불일치 목록)
     */
    @PostMapping("/analyze")
    @Operation(
            summary = "파일 동기화 분석",
            description = "파일시스템과 DB 메타데이터를 비교하여 불일치 항목을 분석합니다."
    )
    public ResponseEntity<ApiResponse<FileSyncDto.AnalyzeResponse>> analyze(
            @RequestBody(required = false) FileSyncDto.AnalyzeRequest request) {

        if (request == null) {
            request = FileSyncDto.AnalyzeRequest.builder().build();
        }

        log.info("파일 동기화 분석 요청 - targets: {}, basePath: {}",
                request.getTargets(), request.getBasePath());

        FileSyncDto.AnalyzeResponse response = fileSyncService.analyze(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 동기화 액션 적용
     *
     * <p>분석 결과에서 선택한 액션들을 일괄 적용합니다.
     *
     * @param request 적용 요청 (액션 목록)
     * @return 적용 결과
     */
    @PostMapping("/apply")
    @Operation(
            summary = "동기화 액션 적용",
            description = "분석된 불일치 항목에 대해 선택한 액션을 적용합니다."
    )
    public ResponseEntity<ApiResponse<FileSyncDto.ApplyResponse>> apply(
            @Valid @RequestBody FileSyncDto.ApplyRequest request) {

        log.info("파일 동기화 적용 요청 - {}건", request.getActions().size());

        FileSyncDto.ApplyResponse response = fileSyncService.apply(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 무시된 파일 목록 조회
     *
     * @param targetType 대상 유형 필터 (선택)
     * @return 무시된 파일 목록
     */
    @GetMapping("/ignores")
    @Operation(
            summary = "무시된 파일 목록 조회",
            description = "파일 동기화 분석에서 제외된 무시 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<FileSyncDto.IgnoredFile>>> getIgnoredFiles(
            @Parameter(description = "대상 유형 필터 (RELEASE_FILE, RESOURCE_FILE, BACKUP_FILE)")
            @RequestParam(required = false) FileSyncTarget targetType) {

        log.info("무시된 파일 목록 조회 요청 - targetType: {}", targetType);

        List<FileSyncDto.IgnoredFile> ignoredFiles = fileSyncService.getIgnoredFiles(targetType);

        return ResponseEntity.ok(ApiResponse.success(ignoredFiles));
    }

    /**
     * 무시 목록에서 제거
     *
     * @param ignoreId 무시 항목 ID
     * @return 성공 응답
     */
    @DeleteMapping("/ignores/{ignoreId}")
    @Operation(
            summary = "무시 목록에서 제거",
            description = "무시 목록에서 항목을 제거합니다. 제거된 항목은 다음 분석 시 다시 나타납니다."
    )
    public ResponseEntity<ApiResponse<Void>> removeFromIgnoreList(
            @Parameter(description = "무시 항목 ID")
            @PathVariable Long ignoreId) {

        log.info("무시 목록 제거 요청 - ignoreId: {}", ignoreId);

        fileSyncService.removeFromIgnoreList(ignoreId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
