package com.ts.rm.domain.customer.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.entity.QCustomer;
import com.ts.rm.domain.customer.entity.QCustomerProject;
import com.ts.rm.domain.project.entity.QProject;
import com.ts.rm.domain.releaseversion.entity.QReleaseVersion;
import com.ts.rm.global.querydsl.QuerydslPaginationUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Customer Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 고객사 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QCustomer customer = QCustomer.customer;
    private static final QCustomerProject customerProject = QCustomerProject.customerProject;
    private static final QProject project = QProject.project;
    private static final QReleaseVersion releaseVersion = QReleaseVersion.releaseVersion;

    @Override
    public Page<Customer> findAllWithProjectInfo(String projectId, Boolean isActive, String keyword, Pageable pageable) {
        // 1. 기본 쿼리 생성
        JPAQuery<Customer> contentQuery = queryFactory
                .selectDistinct(customer)
                .from(customer)
                .leftJoin(customerProject).on(customerProject.customer.customerId.eq(customer.customerId))
                .leftJoin(project).on(customerProject.project.projectId.eq(project.projectId))
                .where(
                        projectIdCondition(projectId),
                        isActiveCondition(isActive),
                        keywordCondition(keyword)
                );

        // 2. Count 쿼리 생성 (동일 조건)
        JPAQuery<Long> countQuery = queryFactory
                .select(customer.countDistinct())
                .from(customer)
                .leftJoin(customerProject).on(customerProject.customer.customerId.eq(customer.customerId))
                .leftJoin(project).on(customerProject.project.projectId.eq(project.projectId))
                .where(
                        projectIdCondition(projectId),
                        isActiveCondition(isActive),
                        keywordCondition(keyword)
                );

        // 3. 정렬 필드 매핑 정의
        // hasCustomVersion: 커스텀 버전 존재 여부 (EXISTS 서브쿼리 → CASE WHEN으로 1/0 변환)
        NumberExpression<Integer> hasCustomVersionExpression = new CaseBuilder()
                .when(JPAExpressions.selectOne()
                        .from(releaseVersion)
                        .where(releaseVersion.customer.customerId.eq(customer.customerId))
                        .exists())
                .then(1)
                .otherwise(0);

        Map<String, com.querydsl.core.types.Expression<?>> sortMapping = new HashMap<>();
        // Customer 필드
        sortMapping.put("customerId", customer.customerId);
        sortMapping.put("customerCode", customer.customerCode);
        sortMapping.put("customerName", customer.customerName);
        sortMapping.put("isActive", customer.isActive);
        sortMapping.put("createdAt", customer.createdAt);
        sortMapping.put("updatedAt", customer.updatedAt);
        // Project 필드
        sortMapping.put("project.projectName", project.projectName);
        sortMapping.put("project.projectId", project.projectId);
        // CustomerProject 필드
        sortMapping.put("lastPatchedVersion", customerProject.lastPatchedVersion);
        sortMapping.put("lastPatchedAt", customerProject.lastPatchedAt);
        // 커스텀 버전 존재 여부 (서브쿼리)
        sortMapping.put("hasCustomVersion", hasCustomVersionExpression);

        // 4. 공통 유틸리티로 페이징/정렬 적용
        return QuerydslPaginationUtil.applyPagination(
                contentQuery,
                countQuery,
                pageable,
                sortMapping,
                customer.customerName.asc() // 기본 정렬: 고객사명 오름차순
        );
    }

    /**
     * 프로젝트 ID 조건
     */
    private BooleanExpression projectIdCondition(String projectId) {
        return (projectId != null && !projectId.isBlank())
                ? customerProject.project.projectId.eq(projectId)
                : null;
    }

    /**
     * 활성화 여부 조건
     */
    private BooleanExpression isActiveCondition(Boolean isActive) {
        return isActive != null ? customer.isActive.eq(isActive) : null;
    }

    /**
     * 키워드 검색 조건 (고객사코드, 고객사명, 설명 통합 검색)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return customer.customerCode.containsIgnoreCase(trimmedKeyword)
                .or(customer.customerName.containsIgnoreCase(trimmedKeyword))
                .or(customer.description.containsIgnoreCase(trimmedKeyword));
    }
}
