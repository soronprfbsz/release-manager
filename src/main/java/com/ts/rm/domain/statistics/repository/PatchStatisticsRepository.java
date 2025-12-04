package com.ts.rm.domain.statistics.repository;

import com.ts.rm.domain.statistics.dto.StatisticsDto.CustomerPatchCount;
import com.ts.rm.domain.statistics.dto.StatisticsDto.MonthlyPatchCount;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 패치 통계 Repository
 *
 * <p>QueryDSL을 사용한 패치 통계 집계 쿼리
 */
public interface PatchStatisticsRepository {

    /**
     * 기간 내 고객사별 패치 건수 Top-N 조회
     *
     * @param startDate 시작일시
     * @param topN      상위 N개
     * @return 고객사별 패치 건수 목록 (내림차순)
     */
    List<CustomerPatchCount> findTopCustomersByPatchCount(LocalDateTime startDate, int topN);

    /**
     * 기간 내 월별 패치 건수 조회
     *
     * @param startDate 시작일시
     * @return 월별 패치 건수 목록 (오름차순)
     */
    List<MonthlyPatchCount> findMonthlyPatchCounts(LocalDateTime startDate);
}
