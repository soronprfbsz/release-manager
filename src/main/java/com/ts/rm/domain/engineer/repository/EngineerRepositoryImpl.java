package com.ts.rm.domain.engineer.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.common.entity.QCode;
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
    private static final QCode positionCode = new QCode("positionCode");

    @Override
    public Page<Engineer> findAllWithFilters(Long departmentId, String keyword, Pageable pageable) {
        // 1. 기본 쿼리 생성 (Code 테이블 left join으로 직급 sort_order 조회)
        JPAQuery<Engineer> contentQuery = queryFactory
                .selectFrom(engineer)
                .leftJoin(positionCode)
                .on(positionCode.codeTypeId.eq("POSITION")
                        .and(positionCode.codeId.eq(engineer.position)))
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
                "position", positionCode.sortOrder,
                "department", engineer.department.departmentId,
                "createdAt", engineer.createdAt
        );

        // 4. 공통 유틸리티로 페이징/정렬 적용
        // 보조 정렬: position (sort_order asc) - 요청 정렬 후 항상 적용
        // 기본 정렬: 소속팀(department_id) asc, 직급(sort_order) asc - 정렬 요청이 없을 때 적용
        return QuerydslPaginationUtil.applyPaginationWithSecondaryOrder(
                contentQuery,
                countQuery,
                pageable,
                sortMapping,
                new com.querydsl.core.types.OrderSpecifier[]{positionCode.sortOrder.asc()},
                engineer.department.departmentId.asc(),
                positionCode.sortOrder.asc()
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
