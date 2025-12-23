package com.ts.rm.domain.account.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.entity.QAccount;
import com.ts.rm.global.querydsl.QuerydslPaginationUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Account Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 계정 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QAccount account = QAccount.account;

    @Override
    public Page<Account> findAllWithFilters(String status, String keyword, Pageable pageable) {
        // 1. 기본 쿼리 생성
        JPAQuery<Account> contentQuery = queryFactory
                .selectFrom(account)
                .where(
                        statusCondition(status),
                        keywordCondition(keyword)
                );

        // 2. Count 쿼리 생성
        JPAQuery<Long> countQuery = queryFactory
                .select(account.count())
                .from(account)
                .where(
                        statusCondition(status),
                        keywordCondition(keyword)
                );

        // 3. 정렬 필드 매핑 정의
        Map<String, com.querydsl.core.types.Expression<?>> sortMapping = Map.of(
                "accountId", account.accountId,
                "email", account.email,
                "accountName", account.accountName,
                "role", account.role,
                "status", account.status,
                "lastLoginAt", account.lastLoginAt,
                "createdAt", account.createdAt
        );

        // 4. 공통 유틸리티로 페이징/정렬 적용
        return QuerydslPaginationUtil.applyPagination(
                contentQuery,
                countQuery,
                pageable,
                sortMapping,
                account.createdAt.desc()
        );
    }

    /**
     * 상태 조건
     */
    private BooleanExpression statusCondition(String status) {
        return (status != null && !status.trim().isEmpty()) ? account.status.eq(status) : null;
    }

    /**
     * 키워드 검색 조건 (계정명, 이메일 통합 검색)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return account.accountName.containsIgnoreCase(trimmedKeyword)
                .or(account.email.containsIgnoreCase(trimmedKeyword));
    }
}
