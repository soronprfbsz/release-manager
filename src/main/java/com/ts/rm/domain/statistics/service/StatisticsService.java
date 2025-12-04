package com.ts.rm.domain.statistics.service;

import com.ts.rm.domain.statistics.dto.StatisticsDto.CustomerPatchCount;
import com.ts.rm.domain.statistics.dto.StatisticsDto.MonthlyPatchCount;
import com.ts.rm.domain.statistics.dto.StatisticsDto.MonthlyPatchResponse;
import com.ts.rm.domain.statistics.dto.StatisticsDto.TopCustomersResponse;
import com.ts.rm.domain.statistics.repository.PatchStatisticsRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계 서비스
 *
 * <p>패치 관련 통계 조회 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final PatchStatisticsRepository patchStatisticsRepository;

    /**
     * 고객사별 패치 Top-N 조회
     *
     * <p>최근 n개월간 패치가 가장 많이 나간 고객사 Top-N을 조회합니다.
     *
     * @param months 조회 기간 (개월)
     * @param topN   상위 N개
     * @return 고객사별 패치 통계 응답
     */
    public TopCustomersResponse getTopCustomersByPatchCount(int months, int topN) {
        log.info("고객사별 패치 Top-{} 조회 - 최근 {}개월", topN, months);

        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);

        List<CustomerPatchCount> customers =
                patchStatisticsRepository.findTopCustomersByPatchCount(startDate, topN);

        log.info("고객사별 패치 통계 조회 완료 - 결과 건수: {}", customers.size());

        return new TopCustomersResponse(months, topN, customers);
    }

    /**
     * 월별 패치 통계 조회
     *
     * <p>최근 n개월간 월별 패치 생성 건수를 조회합니다.
     *
     * @param months 조회 기간 (개월)
     * @return 월별 패치 통계 응답
     */
    public MonthlyPatchResponse getMonthlyPatchCounts(int months) {
        log.info("월별 패치 통계 조회 - 최근 {}개월", months);

        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);

        List<MonthlyPatchCount> monthly =
                patchStatisticsRepository.findMonthlyPatchCounts(startDate);

        log.info("월별 패치 통계 조회 완료 - 결과 건수: {}", monthly.size());

        return new MonthlyPatchResponse(months, monthly);
    }
}
