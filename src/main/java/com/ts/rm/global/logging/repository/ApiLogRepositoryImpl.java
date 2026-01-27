package com.ts.rm.global.logging.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.account.entity.QAccount;
import com.ts.rm.global.logging.dto.ApiLogDto;
import com.ts.rm.global.logging.entity.ApiLog;
import com.ts.rm.global.logging.entity.QApiLog;
import com.ts.rm.global.querydsl.QuerydslPaginationUtil;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * ApiLog Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ApiLogRepositoryImpl implements ApiLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QApiLog apiLog = QApiLog.apiLog;
    private static final QAccount account = QAccount.account;

    /**
     * 정렬 필드 매핑
     */
    private static final Map<String, com.querydsl.core.types.Expression<?>> SORT_MAPPING = Map.ofEntries(
            Map.entry("logId", apiLog.logId),
            Map.entry("requestId", apiLog.requestId),
            Map.entry("httpMethod", apiLog.httpMethod),
            Map.entry("requestUri", apiLog.requestUri),
            Map.entry("responseStatus", apiLog.responseStatus),
            Map.entry("clientIp", apiLog.clientIp),
            Map.entry("accountId", apiLog.accountId),
            Map.entry("accountEmail", apiLog.accountEmail),
            Map.entry("executionTimeMs", apiLog.executionTimeMs),
            Map.entry("createdAt", apiLog.createdAt)
    );

    @Override
    public long deleteByCreatedAtBefore(LocalDateTime cutoffDate) {
        return queryFactory
                .delete(apiLog)
                .where(apiLog.createdAt.lt(cutoffDate))
                .execute();
    }

    @Override
    public Page<ApiLog> searchWithFilters(ApiLogDto.SearchCondition condition, Pageable pageable) {
        boolean hasKeyword = condition.keyword() != null && !condition.keyword().isBlank();

        // 1. 기본 쿼리 생성 (keyword 검색 시 account 테이블 left join)
        JPAQuery<ApiLog> contentQuery = queryFactory
                .selectFrom(apiLog);

        if (hasKeyword) {
            contentQuery.leftJoin(account).on(account.accountId.eq(apiLog.accountId));
        }

        contentQuery.where(
                keywordCondition(condition.keyword()),
                httpMethodEq(condition.httpMethod()),
                responseStatusEq(condition.responseStatus()),
                clientIpEq(condition.clientIp()),
                createdAtBetween(condition.startDate(), condition.endDate())
        );

        // 2. Count 쿼리 생성
        JPAQuery<Long> countQuery = queryFactory
                .select(apiLog.count())
                .from(apiLog);

        if (hasKeyword) {
            countQuery.leftJoin(account).on(account.accountId.eq(apiLog.accountId));
        }

        countQuery.where(
                keywordCondition(condition.keyword()),
                httpMethodEq(condition.httpMethod()),
                responseStatusEq(condition.responseStatus()),
                clientIpEq(condition.clientIp()),
                createdAtBetween(condition.startDate(), condition.endDate())
        );

        // 3. 공통 유틸리티로 페이징/정렬 적용 (기본 정렬: createdAt DESC)
        return QuerydslPaginationUtil.applyPagination(
                contentQuery,
                countQuery,
                pageable,
                SORT_MAPPING,
                apiLog.createdAt.desc()
        );
    }

    /**
     * 통합 키워드 검색 조건 (요청 URI OR 계정 이메일 OR 계정 이름)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return apiLog.requestUri.containsIgnoreCase(trimmedKeyword)
                .or(apiLog.accountEmail.containsIgnoreCase(trimmedKeyword))
                .or(account.accountName.containsIgnoreCase(trimmedKeyword));
    }

    private BooleanExpression httpMethodEq(String httpMethod) {
        return (httpMethod != null && !httpMethod.isBlank())
                ? apiLog.httpMethod.equalsIgnoreCase(httpMethod.trim())
                : null;
    }

    private BooleanExpression responseStatusEq(Integer responseStatus) {
        return (responseStatus != null)
                ? apiLog.responseStatus.eq(responseStatus)
                : null;
    }

    private BooleanExpression clientIpEq(String clientIp) {
        return (clientIp != null && !clientIp.isBlank())
                ? apiLog.clientIp.eq(clientIp.trim())
                : null;
    }

    private BooleanExpression createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return apiLog.createdAt.between(startDate, endDate);
        } else if (startDate != null) {
            return apiLog.createdAt.goe(startDate);
        } else if (endDate != null) {
            return apiLog.createdAt.loe(endDate);
        }
        return null;
    }
}
