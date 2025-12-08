package com.ts.rm.domain.analytics.service;

import com.ts.rm.domain.analytics.dto.AnalyticsDto.CustomerPatchCount;
import com.ts.rm.domain.analytics.dto.AnalyticsDto.MonthlyCustomerPatchCount;
import com.ts.rm.domain.analytics.dto.AnalyticsDto.MonthlyCustomerPatchRaw;
import com.ts.rm.domain.analytics.dto.AnalyticsDto.MonthlyPatchResponse;
import com.ts.rm.domain.analytics.dto.AnalyticsDto.TopCustomersResponse;
import com.ts.rm.domain.analytics.repository.PatchAnalyticsRepository;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 분석 서비스
 *
 * <p>패치 관련 분석 조회 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final PatchAnalyticsRepository patchAnalyticsRepository;
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 프로젝트별 고객사별 패치 Top-N 조회
     *
     * <p>최근 n개월간 패치가 가장 많이 나간 고객사 Top-N을 조회합니다.
     *
     * @param projectId 프로젝트 ID
     * @param months    조회 기간 (개월)
     * @param topN      상위 N개
     * @return 고객사별 패치 통계 응답
     */
    public TopCustomersResponse getTopCustomersByPatchCount(String projectId, int months, int topN) {
        log.info("프로젝트별 고객사별 패치 Top-{} 조회 - projectId: {}, 최근 {}개월", topN, projectId, months);

        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);

        List<CustomerPatchCount> customers =
                patchAnalyticsRepository.findTopCustomersByPatchCount(projectId, startDate, topN);

        log.info("고객사별 패치 통계 조회 완료 - 결과 건수: {}", customers.size());

        return new TopCustomersResponse(months, topN, customers);
    }

    /**
     * 프로젝트별 월별+고객별 패치 통계 조회
     *
     * <p>최근 n개월간 월별+고객별 패치 생성 건수를 조회합니다.
     *
     * @param projectId 프로젝트 ID
     * @param months    조회 기간 (개월)
     * @return 월별+고객별 패치 통계 응답
     */
    public MonthlyPatchResponse getMonthlyPatchCounts(String projectId, int months) {
        log.info("프로젝트별 월별+고객별 패치 통계 조회 - projectId: {}, 최근 {}개월", projectId, months);

        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);

        // 원본 데이터 조회
        List<MonthlyCustomerPatchRaw> rawData =
                patchAnalyticsRepository.findMonthlyCustomerPatchCounts(projectId, startDate);

        // 데이터가 없으면 빈 응답 반환 (프론트엔드에서 nodata 처리 가능)
        if (rawData.isEmpty()) {
            log.info("월별+고객별 패치 통계 조회 완료 - 데이터 없음");
            return new MonthlyPatchResponse(months, List.of(), List.of());
        }

        // 고객사 목록 추출 (중복 제거, 순서 유지)
        Set<String> customerSet = new LinkedHashSet<>();
        for (MonthlyCustomerPatchRaw raw : rawData) {
            customerSet.add(raw.customerName());
        }
        List<String> customers = new ArrayList<>(customerSet);

        // 조회 기간의 모든 월 생성
        List<String> allMonths = generateAllMonths(months);

        // 월별+고객별 데이터를 Map으로 변환 (yearMonth -> customerName -> count)
        Map<String, Map<String, Long>> monthlyDataMap = new LinkedHashMap<>();
        for (String yearMonth : allMonths) {
            monthlyDataMap.put(yearMonth, new LinkedHashMap<>());
        }

        for (MonthlyCustomerPatchRaw raw : rawData) {
            monthlyDataMap
                    .computeIfAbsent(raw.yearMonth(), k -> new LinkedHashMap<>())
                    .put(raw.customerName(), raw.patchCount());
        }

        // 응답 형식으로 변환 (없는 고객은 0으로 채움)
        List<MonthlyCustomerPatchCount> monthly = new ArrayList<>();
        for (String yearMonth : allMonths) {
            Map<String, Long> customerCounts = new LinkedHashMap<>();
            for (String customer : customers) {
                customerCounts.put(customer, monthlyDataMap.get(yearMonth).getOrDefault(customer, 0L));
            }
            monthly.add(new MonthlyCustomerPatchCount(yearMonth, customerCounts));
        }

        log.info("월별+고객별 패치 통계 조회 완료 - 월수: {}, 고객수: {}", monthly.size(), customers.size());

        return new MonthlyPatchResponse(months, customers, monthly);
    }

    /**
     * 조회 기간의 모든 월 목록 생성
     *
     * @param months 조회 기간 (개월)
     * @return 연월 목록 (YYYY-MM 형식)
     */
    private List<String> generateAllMonths(int months) {
        List<String> allMonths = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            allMonths.add(current.minusMonths(i).format(YEAR_MONTH_FORMATTER));
        }

        return allMonths;
    }
}
