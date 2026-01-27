package com.ts.rm.global.logging.controller;

import com.ts.rm.global.logging.dto.ApiLogDto;
import com.ts.rm.global.logging.service.ApiLogService;
import com.ts.rm.global.response.ApiResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ApiLog Controller
 *
 * <p>API 로그 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api/api-logs")
@RequiredArgsConstructor
public class ApiLogController implements ApiLogControllerDocs {

    private final ApiLogService apiLogService;

    /**
     * API 로그 목록 조회 (페이징)
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApiLogDto.ListResponse>>> searchLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) Integer responseStatus,
            @RequestParam(required = false) String clientIp,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        ApiLogDto.SearchCondition condition = new ApiLogDto.SearchCondition(
                keyword, httpMethod, responseStatus, clientIp, startDate, endDate);

        Page<ApiLogDto.ListResponse> logs = apiLogService.searchLogs(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * API 로그 상세 조회
     */
    @Override
    @GetMapping("/{logId}")
    public ResponseEntity<ApiResponse<ApiLogDto.Response>> getLog(@PathVariable Long logId) {
        ApiLogDto.Response apiLog = apiLogService.getLog(logId);
        return ResponseEntity.ok(ApiResponse.success(apiLog));
    }
}
