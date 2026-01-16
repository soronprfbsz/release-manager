package com.ts.rm.domain.patch.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.entity.QPatch;
import com.ts.rm.global.querydsl.QuerydslPaginationUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Patch Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class PatchRepositoryImpl implements PatchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QPatch patch = QPatch.patch;

    @Override
    public List<Patch> findRecentByProjectIdAndReleaseType(String projectId, String releaseType, int limit) {
        return queryFactory
                .selectFrom(patch)
                .where(
                        patch.project.projectId.eq(projectId),
                        patch.releaseType.eq(releaseType)
                )
                .orderBy(patch.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Patch> findAllWithFilters(String projectId, String releaseType, String customerCode, Pageable pageable) {
        // 1. Content 쿼리 (customer LEFT JOIN으로 정렬 지원)
        JPAQuery<Patch> contentQuery = queryFactory
                .selectFrom(patch)
                .leftJoin(patch.customer).fetchJoin()
                .where(
                        projectIdCondition(projectId),
                        releaseTypeCondition(releaseType),
                        customerCodeCondition(customerCode)
                );

        // 2. Count 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(patch.count())
                .from(patch)
                .where(
                        projectIdCondition(projectId),
                        releaseTypeCondition(releaseType),
                        customerCodeCondition(customerCode)
                );

        // 3. 정렬 필드 매핑
        Map<String, com.querydsl.core.types.Expression<?>> sortMapping = Map.of(
                "patchId", patch.patchId,
                "patchName", patch.patchName,
                "releaseType", patch.releaseType,
                "fromVersion", patch.fromVersion,
                "toVersion", patch.toVersion,
                "createdAt", patch.createdAt,
                "customerName", patch.customer.customerName
        );

        // 4. 페이징 적용
        return QuerydslPaginationUtil.applyPagination(
                contentQuery,
                countQuery,
                pageable,
                sortMapping,
                (java.util.List<com.querydsl.core.types.OrderSpecifier<?>>) null,
                patch.createdAt.desc()
        );
    }

    /**
     * 프로젝트 ID 조건
     */
    private BooleanExpression projectIdCondition(String projectId) {
        return (projectId != null && !projectId.isBlank())
                ? patch.project.projectId.eq(projectId)
                : null;
    }

    /**
     * 릴리즈 타입 조건
     */
    private BooleanExpression releaseTypeCondition(String releaseType) {
        return (releaseType != null && !releaseType.isBlank())
                ? patch.releaseType.eq(releaseType.toUpperCase())
                : null;
    }

    /**
     * 고객사 코드 조건
     */
    private BooleanExpression customerCodeCondition(String customerCode) {
        return (customerCode != null && !customerCode.isBlank())
                ? patch.customer.customerCode.eq(customerCode)
                : null;
    }
}
