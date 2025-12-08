package com.ts.rm.domain.customer.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.entity.QCustomer;
import com.ts.rm.domain.customer.entity.QCustomerProject;
import com.ts.rm.domain.project.entity.QProject;
import com.ts.rm.global.querydsl.QuerydslPaginationUtil;
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

    @Override
    public Page<Customer> findAllWithProjectInfo(Boolean isActive, String keyword, Pageable pageable) {
        // 1. 기본 쿼리 생성
        JPAQuery<Customer> contentQuery = queryFactory
                .selectDistinct(customer)
                .from(customer)
                .leftJoin(customerProject).on(customerProject.customer.customerId.eq(customer.customerId))
                .leftJoin(project).on(customerProject.project.projectId.eq(project.projectId))
                .where(
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
                        isActiveCondition(isActive),
                        keywordCondition(keyword)
                );

        // 3. 정렬 필드 매핑 정의
        Map<String, com.querydsl.core.types.Expression<?>> sortMapping = Map.of(
                // Customer 필드
                "customerId", customer.customerId,
                "customerCode", customer.customerCode,
                "customerName", customer.customerName,
                "isActive", customer.isActive,
                "createdAt", customer.createdAt,
                "updatedAt", customer.updatedAt,
                // Project 필드
                "project.projectName", project.projectName,
                "project.projectId", project.projectId,
                // CustomerProject 필드
                "lastPatchedVersion", customerProject.lastPatchedVersion,
                "lastPatchedAt", customerProject.lastPatchedAt
        );

        // 4. 공통 유틸리티로 페이징/정렬 적용
        return QuerydslPaginationUtil.applyPagination(
                contentQuery,
                countQuery,
                pageable,
                sortMapping,
                customer.createdAt.desc() // 기본 정렬
        );
    }

    /**
     * 활성화 여부 조건
     */
    private BooleanExpression isActiveCondition(Boolean isActive) {
        return isActive != null ? customer.isActive.eq(isActive) : null;
    }

    /**
     * 키워드 검색 조건
     */
    private BooleanExpression keywordCondition(String keyword) {
        return (keyword != null && !keyword.trim().isEmpty())
                ? customer.customerName.containsIgnoreCase(keyword.trim())
                : null;
    }
}
