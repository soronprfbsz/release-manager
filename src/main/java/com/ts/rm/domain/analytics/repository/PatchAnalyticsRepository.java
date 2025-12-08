package com.ts.rm.domain.analytics.repository;

import com.ts.rm.domain.analytics.dto.AnalyticsDto.CustomerPatchCount;
import com.ts.rm.domain.analytics.dto.AnalyticsDto.MonthlyCustomerPatchRaw;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 패치 분석 Repository
 *
 * <p>QueryDSL을 사용한 패치 분석 집계 쿼리
 */
public interface PatchAnalyticsRepository {

    /**
     * 프로젝트별 기간 내 고객사별 패치 건수 Top-N 조회
     *
     * @param projectId 프로젝트 ID
     * @param startDate 시작일시
     * @param topN      상위 N개
     * @return 고객사별 패치 건수 목록 (내림차순)
     */
    List<CustomerPatchCount> findTopCustomersByPatchCount(String projectId, LocalDateTime startDate, int topN);

    /**
     * 프로젝트별 기간 내 월별+고객별 패치 건수 조회
     *
     * @param projectId 프로젝트 ID
     * @param startDate 시작일시
     * @return 월별+고객별 패치 건수 목록 (연월 오름차순, 고객명 오름차순)
     */
    List<MonthlyCustomerPatchRaw> findMonthlyCustomerPatchCounts(String projectId, LocalDateTime startDate);
}
