package com.ts.rm.domain.analytics.controller;

import com.ts.rm.domain.analytics.dto.AnalyticsDto.MonthlyPatchResponse;
import com.ts.rm.domain.analytics.dto.AnalyticsDto.TopCustomersResponse;
import com.ts.rm.domain.analytics.service.AnalyticsService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 분석 API Controller
 *
 * <p>패치 관련 분석 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/analytics")
@RequiredArgsConstructor
@Tag(name = "데이터 분석", description = "데이터 분석 API")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * 프로젝트별 고객사별 패치 Top-N 조회
     *
     * <p>최근 n개월간 패치가 가장 많이 나간 고객사 Top-N을 조회합니다.
     */
    @GetMapping("/patches/top-customers")
    @Operation(
            summary = "고객사별 패치 Top-N 조회",
            description = "프로젝트별 최근 n개월간 패치가 가장 많이 나간 고객사 Top-N을 조회합니다.\n\n"
                    + "**파라미터**:\n"
                    + "- `months`: 조회 기간 (개월, 기본값: 6)\n"
                    + "- `topN`: 상위 N개 (기본값: 5)\n\n"
                    + "**참고**:\n"
                    + "- CUSTOM 타입 패치만 집계됩니다 (STANDARD는 고객사 정보 없음)\n"
                    + "- 패치 건수 기준 내림차순 정렬"
    )
    public ApiResponse<TopCustomersResponse> getTopCustomersByPatchCount(
            @Parameter(description = "프로젝트 ID", required = true, example = "infraeye2")
            @PathVariable String projectId,

            @Parameter(description = "조회 기간 (개월)", example = "6")
            @RequestParam(defaultValue = "6") int months,

            @Parameter(description = "상위 N개", example = "5")
            @RequestParam(defaultValue = "5") int topN) {

        log.info("프로젝트별 고객사별 패치 Top-{} 조회 요청 - projectId: {}, 최근 {}개월", topN, projectId, months);

        TopCustomersResponse response = analyticsService.getTopCustomersByPatchCount(projectId, months, topN);

        return ApiResponse.success(response);
    }

    /**
     * 프로젝트별 월별+고객별 패치 통계 조회
     *
     * <p>최근 n개월간 월별+고객별 패치 생성 건수를 조회합니다.
     */
    @GetMapping("/patches/monthly")
    @Operation(
            summary = "월별+고객별 패치 통계 조회",
            description = "프로젝트별 최근 n개월간 월별+고객별 패치 생성 건수를 조회합니다.\n\n"
                    + "**파라미터**:\n"
                    + "- `months`: 조회 기간 (개월, 기본값: 6)\n\n"
                    + "**응답 형식**:\n"
                    + "```json\n"
                    + "{\n"
                    + "  \"months\": 6,\n"
                    + "  \"customers\": [\"A회사\", \"B회사\", \"C회사\"],\n"
                    + "  \"monthly\": [\n"
                    + "    { \"yearMonth\": \"2025-07\", \"customerCounts\": {\"A회사\": 3, \"B회사\": 2, \"C회사\": 0} },\n"
                    + "    { \"yearMonth\": \"2025-08\", \"customerCounts\": {\"A회사\": 2, \"B회사\": 0, \"C회사\": 1} }\n"
                    + "  ]\n"
                    + "}\n"
                    + "```\n\n"
                    + "**참고**:\n"
                    + "- CUSTOM 타입 패치만 집계 (고객사별 통계)\n"
                    + "- 연월(YYYY-MM) 기준 오름차순 정렬\n"
                    + "- 패치가 없는 월/고객은 0으로 표시"
    )
    public ApiResponse<MonthlyPatchResponse> getMonthlyPatchCounts(
            @Parameter(description = "프로젝트 ID", required = true, example = "infraeye2")
            @PathVariable String projectId,

            @Parameter(description = "조회 기간 (개월)", example = "6")
            @RequestParam(defaultValue = "6") int months) {

        log.info("프로젝트별 월별+고객별 패치 통계 조회 요청 - projectId: {}, 최근 {}개월", projectId, months);

        MonthlyPatchResponse response = analyticsService.getMonthlyPatchCounts(projectId, months);

        return ApiResponse.success(response);
    }
}
