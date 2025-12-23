package com.ts.rm.domain.engineer.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.engineer.entity.Engineer;
import com.ts.rm.domain.engineer.entity.QEngineer;
import com.ts.rm.global.querydsl.QuerydslPaginationUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Engineer Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 엔지니어 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class EngineerRepositoryImpl implements EngineerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QEngineer engineer = QEngineer.engineer;

    @Override
    public Page<Engineer> findAllWithFilters(Long departmentId, String keyword, Pageable pageable) {
        // 1. 기본 쿼리 생성
        JPAQuery<Engineer> contentQuery = queryFactory
                .selectFrom(engineer)
                .where(
                        departmentCondition(departmentId),
                        keywordCondition(keyword)
                );

        // 2. Count 쿼리 생성
        JPAQuery<Long> countQuery = queryFactory
                .select(engineer.count())
                .from(engineer)
                .where(
                        departmentCondition(departmentId),
                        keywordCondition(keyword)
                );

        // 3. 정렬 필드 매핑 정의
        Map<String, com.querydsl.core.types.Expression<?>> sortMapping = Map.of(
                "engineerId", engineer.engineerId,
                "engineerName", engineer.engineerName,
                "engineerEmail", engineer.engineerEmail,
                "engineerPhone", engineer.engineerPhone,
                "position", engineer.position,
                "createdAt", engineer.createdAt
        );

        // 4. 공통 유틸리티로 페이징/정렬 적용
        return QuerydslPaginationUtil.applyPagination(
                contentQuery,
                countQuery,
                pageable,
                sortMapping,
                engineer.engineerName.asc()
        );
    }

    /**
     * 부서 ID 조건
     */
    private BooleanExpression departmentCondition(Long departmentId) {
        return departmentId != null ? engineer.department.departmentId.eq(departmentId) : null;
    }

    /**
     * 키워드 검색 조건 (이름, 이메일, 직급, 설명 통합 검색)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return engineer.engineerName.containsIgnoreCase(trimmedKeyword)
                .or(engineer.engineerEmail.containsIgnoreCase(trimmedKeyword))
                .or(engineer.position.containsIgnoreCase(trimmedKeyword))
                .or(engineer.description.containsIgnoreCase(trimmedKeyword));
    }
}
