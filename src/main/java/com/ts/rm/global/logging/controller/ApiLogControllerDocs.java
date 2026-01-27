package com.ts.rm.global.logging.controller;

import com.ts.rm.global.logging.dto.ApiLogDto;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * ApiLog Controller Swagger Documentation
 */
@Tag(name = "API Log", description = "API 로그 조회")
public interface ApiLogControllerDocs {

    @Operation(summary = "API 로그 목록 조회 (페이징)",
            description = "검색 조건에 따라 API 로그 목록을 페이징하여 조회합니다.")
    ResponseEntity<ApiResponse<Page<ApiLogDto.ListResponse>>> searchLogs(
            @Parameter(description = "통합 검색 키워드 (요청 URI, 계정 이메일, 계정 이름 OR 검색)", example = "admin") String keyword,
            @Parameter(description = "HTTP 메서드", example = "GET") String httpMethod,
            @Parameter(description = "응답 상태 코드", example = "200") Integer responseStatus,
            @Parameter(description = "클라이언트 IP", example = "192.168.1.1") String clientIp,
            @Parameter(description = "시작일시", example = "2024-01-01T00:00:00") LocalDateTime startDate,
            @Parameter(description = "종료일시", example = "2024-12-31T23:59:59") LocalDateTime endDate,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "API 로그 상세 조회",
            description = "특정 API 로그의 상세 정보를 조회합니다.")
    ResponseEntity<ApiResponse<ApiLogDto.Response>> getLog(
            @Parameter(description = "로그 ID", example = "1", required = true) Long logId);
}
