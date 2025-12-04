package com.ts.rm.domain.statistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/**
 * 통계 API DTO
 */
public final class StatisticsDto {

    private StatisticsDto() {
    }

    /**
     * 고객사별 패치 통계 응답
     *
     * @param customerId   고객사 ID
     * @param customerCode 고객사 코드
     * @param customerName 고객사명
     * @param patchCount   패치 건수
     */
    @Schema(description = "고객사별 패치 통계")
    public record CustomerPatchCount(
            @Schema(description = "고객사 ID", example = "1")
            Long customerId,

            @Schema(description = "고객사 코드", example = "CUSTOMER_A")
            String customerCode,

            @Schema(description = "고객사명", example = "A사")
            String customerName,

            @Schema(description = "패치 건수", example = "15")
            Long patchCount
    ) {
    }

    /**
     * 고객사별 패치 Top-N 응답
     *
     * @param months    조회 기간 (개월)
     * @param topN      상위 N개
     * @param customers 고객사별 패치 통계 목록
     */
    @Schema(description = "고객사별 패치 Top-N 응답")
    public record TopCustomersResponse(
            @Schema(description = "조회 기간 (개월)", example = "6")
            int months,

            @Schema(description = "상위 N개", example = "5")
            int topN,

            @Schema(description = "고객사별 패치 통계 목록")
            List<CustomerPatchCount> customers
    ) {
    }

    /**
     * 월별+고객별 패치 원본 데이터 (내부 사용)
     *
     * @param yearMonth    연월 (YYYY-MM)
     * @param customerName 고객사명
     * @param patchCount   패치 건수
     */
    public record MonthlyCustomerPatchRaw(
            String yearMonth,
            String customerName,
            Long patchCount
    ) {
    }

    /**
     * 월별+고객별 패치 통계
     *
     * @param yearMonth      연월 (YYYY-MM)
     * @param customerCounts 고객사별 패치 건수 (고객사명 -> 패치 건수)
     */
    @Schema(description = "월별+고객별 패치 통계")
    public record MonthlyCustomerPatchCount(
            @Schema(description = "연월 (YYYY-MM)", example = "2025-06")
            String yearMonth,

            @Schema(description = "고객사별 패치 건수", example = "{\"A회사\": 3, \"B회사\": 2}")
            Map<String, Long> customerCounts
    ) {
    }

    /**
     * 월별+고객별 패치 통계 응답
     *
     * @param months    조회 기간 (개월)
     * @param customers 고객사 목록
     * @param monthly   월별+고객별 패치 통계 목록
     */
    @Schema(description = "월별+고객별 패치 통계 응답")
    public record MonthlyPatchResponse(
            @Schema(description = "조회 기간 (개월)", example = "6")
            int months,

            @Schema(description = "고객사 목록", example = "[\"A회사\", \"B회사\", \"C회사\"]")
            List<String> customers,

            @Schema(description = "월별+고객별 패치 통계 목록")
            List<MonthlyCustomerPatchCount> monthly
    ) {
    }
}
