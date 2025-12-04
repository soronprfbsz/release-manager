package com.ts.rm.domain.statistics.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.customer.entity.QCustomer;
import com.ts.rm.domain.patch.entity.QPatch;
import com.ts.rm.domain.statistics.dto.StatisticsDto.CustomerPatchCount;
import com.ts.rm.domain.statistics.dto.StatisticsDto.MonthlyPatchCount;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 패치 통계 Repository 구현체
 *
 * <p>QueryDSL을 사용한 패치 통계 집계 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class PatchStatisticsRepositoryImpl implements PatchStatisticsRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 기간 내 고객사별 패치 건수 Top-N 조회
     *
     * <p>CUSTOM 타입 패치만 집계 (STANDARD는 고객사가 없음)
     *
     * @param startDate 시작일시
     * @param topN      상위 N개
     * @return 고객사별 패치 건수 목록 (내림차순)
     */
    @Override
    public List<CustomerPatchCount> findTopCustomersByPatchCount(LocalDateTime startDate, int topN) {
        QPatch patch = QPatch.patch;
        QCustomer customer = QCustomer.customer;

        return queryFactory
                .select(Projections.constructor(CustomerPatchCount.class,
                        customer.customerId,
                        customer.customerCode,
                        customer.customerName,
                        patch.count()))
                .from(patch)
                .join(patch.customer, customer)
                .where(
                        patch.createdAt.goe(startDate),
                        patch.customer.isNotNull()
                )
                .groupBy(
                        customer.customerId,
                        customer.customerCode,
                        customer.customerName
                )
                .orderBy(patch.count().desc())
                .limit(topN)
                .fetch();
    }

    /**
     * 기간 내 월별 패치 건수 조회
     *
     * <p>모든 타입의 패치 집계 (STANDARD + CUSTOM)
     *
     * @param startDate 시작일시
     * @return 월별 패치 건수 목록 (오름차순)
     */
    @Override
    public List<MonthlyPatchCount> findMonthlyPatchCounts(LocalDateTime startDate) {
        QPatch patch = QPatch.patch;

        // DATE_FORMAT(created_at, '%Y-%m') 형식으로 월별 그룹화
        StringTemplate yearMonthTemplate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, '%Y-%m')",
                patch.createdAt
        );

        return queryFactory
                .select(Projections.constructor(MonthlyPatchCount.class,
                        yearMonthTemplate,
                        patch.count()))
                .from(patch)
                .where(patch.createdAt.goe(startDate))
                .groupBy(yearMonthTemplate)
                .orderBy(yearMonthTemplate.asc())
                .fetch();
    }
}
