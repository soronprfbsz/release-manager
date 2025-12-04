package com.ts.rm.domain.dashboard.controller;

import com.ts.rm.domain.dashboard.dto.DashboardDto;
import com.ts.rm.domain.dashboard.service.DashboardService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "대시보드 API")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 최근 데이터 조회
     *
     * <p>다음 정보를 포함합니다:
     * <ul>
     *   <li>최신 설치본 1개 (STANDARD + INSTALL)</li>
     *   <li>최근 릴리즈 버전 N개 (STANDARD + PATCH, 기본값: 3)</li>
     *   <li>최근 생성 패치 N개 (STANDARD, 기본값: 3)</li>
     * </ul>
     *
     * @param versionLimit 최근 릴리즈 버전 조회 개수 (기본값: 3)
     * @param patchLimit   최근 생성 패치 조회 개수 (기본값: 3)
     * @return 대시보드 응답
     */
    @GetMapping("/recent")
    @Operation(
            summary = "대시보드 최근 데이터 조회",
            description = "최신 설치본 1개, 최근 릴리즈 버전 N개, 최근 생성 패치 N개를 조회합니다.\n\n"
                    + "**파라미터**:\n"
                    + "- `versionLimit`: 최근 릴리즈 버전 조회 개수 (기본값: 3)\n"
                    + "- `patchLimit`: 최근 생성 패치 조회 개수 (기본값: 3)"
    )
    public ResponseEntity<ApiResponse<DashboardDto.Response>> getRecentData(
            @Parameter(description = "최근 릴리즈 버전 조회 개수", example = "3")
            @RequestParam(defaultValue = "3") int versionLimit,

            @Parameter(description = "최근 생성 패치 조회 개수", example = "3")
            @RequestParam(defaultValue = "3") int patchLimit) {

        DashboardDto.Response response = dashboardService.getRecentData(versionLimit, patchLimit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
