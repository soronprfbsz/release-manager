package com.ts.rm.domain.statistics.controller;

import com.ts.rm.domain.statistics.dto.StatisticsDto.MonthlyPatchResponse;
import com.ts.rm.domain.statistics.dto.StatisticsDto.TopCustomersResponse;
import com.ts.rm.domain.statistics.service.StatisticsService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통계 API Controller
 *
 * <p>패치 관련 통계 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "통계", description = "패치 통계 조회 API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 고객사별 패치 Top-N 조회
     *
     * <p>최근 n개월간 패치가 가장 많이 나간 고객사 Top-N을 조회합니다.
     */
    @GetMapping("/patches/top-customers")
    @Operation(
            summary = "고객사별 패치 Top-N 조회",
            description = "최근 n개월간 패치가 가장 많이 나간 고객사 Top-N을 조회합니다.\n\n"
                    + "**파라미터**:\n"
                    + "- `months`: 조회 기간 (개월, 기본값: 6)\n"
                    + "- `topN`: 상위 N개 (기본값: 5)\n\n"
                    + "**참고**:\n"
                    + "- CUSTOM 타입 패치만 집계됩니다 (STANDARD는 고객사 정보 없음)\n"
                    + "- 패치 건수 기준 내림차순 정렬"
    )
    public ApiResponse<TopCustomersResponse> getTopCustomersByPatchCount(
            @Parameter(description = "조회 기간 (개월)", example = "6")
            @RequestParam(defaultValue = "6") int months,

            @Parameter(description = "상위 N개", example = "5")
            @RequestParam(defaultValue = "5") int topN) {

        log.info("고객사별 패치 Top-{} 조회 요청 - 최근 {}개월", topN, months);

        TopCustomersResponse response = statisticsService.getTopCustomersByPatchCount(months, topN);

        return ApiResponse.success(response);
    }

    /**
     * 월별 패치 통계 조회
     *
     * <p>최근 n개월간 월별 패치 생성 건수를 조회합니다.
     */
    @GetMapping("/patches/monthly")
    @Operation(
            summary = "월별 패치 통계 조회",
            description = "최근 n개월간 월별 패치 생성 건수를 조회합니다.\n\n"
                    + "**파라미터**:\n"
                    + "- `months`: 조회 기간 (개월, 기본값: 6)\n\n"
                    + "**참고**:\n"
                    + "- 모든 타입의 패치 집계 (STANDARD + CUSTOM)\n"
                    + "- 연월(YYYY-MM) 기준 오름차순 정렬"
    )
    public ApiResponse<MonthlyPatchResponse> getMonthlyPatchCounts(
            @Parameter(description = "조회 기간 (개월)", example = "6")
            @RequestParam(defaultValue = "6") int months) {

        log.info("월별 패치 통계 조회 요청 - 최근 {}개월", months);

        MonthlyPatchResponse response = statisticsService.getMonthlyPatchCounts(months);

        return ApiResponse.success(response);
    }
}
