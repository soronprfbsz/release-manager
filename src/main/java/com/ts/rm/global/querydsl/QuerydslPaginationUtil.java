package com.ts.rm.global.querydsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * QueryDSL 페이징 및 정렬 공통 유틸리티
 *
 * <p>QueryDSL Custom Repository 구현 시 페이징/정렬 로직 재사용을 위한 유틸리티 클래스
 *
 * <p><b>사용 예시:</b>
 * <pre>{@code
 * // 1. 정렬 매핑 정의
 * Map<String, com.querydsl.core.types.Expression<?>> sortMapping = Map.of(
 *     "customerId", customer.customerId,
 *     "customerName", customer.customerName,
 *     "lastPatchedVersion", customerProject.lastPatchedVersion
 * );
 *
 * // 2. 페이징 적용
 * JPAQuery<Customer> query = queryFactory.selectFrom(customer);
 * Page<Customer> result = QuerydslPaginationUtil.applyPagination(
 *     query,
 *     query, // count 쿼리 (동일 쿼리 재사용)
 *     pageable,
 *     sortMapping,
 *     customer.createdAt.desc() // 기본 정렬
 * );
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuerydslPaginationUtil {

    /**
     * JPAQuery에 페이징 및 정렬을 적용하여 Page 객체 반환
     *
     * @param <T>             엔티티 타입
     * @param contentQuery    데이터 조회 쿼리 (정렬/페이징 적용 전)
     * @param countQuery      전체 개수 조회 쿼리 (정렬/페이징 미적용)
     * @param pageable        페이징 정보
     * @param sortMapping     정렬 필드 매핑 (key: 필드명, value: QueryDSL Expression)
     * @param defaultOrder    기본 정렬 (정렬 조건이 없을 때 사용)
     * @return Page 객체
     */
    public static <T> Page<T> applyPagination(
            JPAQuery<T> contentQuery,
            JPAQuery<?> countQuery,
            Pageable pageable,
            Map<String, com.querydsl.core.types.Expression<?>> sortMapping,
            OrderSpecifier<?>... defaultOrder) {

        // 1. 정렬 적용
        List<OrderSpecifier<?>> orders = createOrderSpecifiers(pageable.getSort(), sortMapping);
        for (OrderSpecifier<?> order : orders) {
            contentQuery.orderBy(order);
        }

        // 기본 정렬 추가 (정렬이 없을 경우)
        if (orders.isEmpty() && defaultOrder.length > 0) {
            contentQuery.orderBy(defaultOrder);
        }

        // 2. 페이징 적용
        List<T> content = contentQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 3. 전체 개수 조회
        Long total = countQuery.fetchCount();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * Pageable Sort를 QueryDSL OrderSpecifier 리스트로 변환
     *
     * @param sort        Spring Data Sort 객체
     * @param sortMapping 정렬 필드 매핑 (key: 필드명, value: QueryDSL Expression)
     * @return OrderSpecifier 리스트
     */
    public static List<OrderSpecifier<?>> createOrderSpecifiers(
            Sort sort,
            Map<String, com.querydsl.core.types.Expression<?>> sortMapping) {

        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for (Sort.Order sortOrder : sort) {
            String property = sortOrder.getProperty();
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

            // 매핑된 Expression이 있으면 사용
            if (sortMapping.containsKey(property)) {
                com.querydsl.core.types.Expression<?> expression = sortMapping.get(property);
                orders.add(new OrderSpecifier(direction, expression));
            }
            // 매핑이 없으면 무시 (또는 예외 발생 가능)
        }

        return orders;
    }

    /**
     * 동적 필드 정렬을 위한 PathBuilder 기반 OrderSpecifier 생성
     *
     * <p>주의: 타입 안전성이 보장되지 않으므로 사용 시 주의 필요
     *
     * @param <T>         엔티티 타입
     * @param entityClass 엔티티 클래스
     * @param alias       QueryDSL 엔티티 별칭
     * @param sort        Spring Data Sort 객체
     * @return OrderSpecifier 리스트
     */
    @SuppressWarnings("unchecked")
    public static <T> List<OrderSpecifier<?>> createDynamicOrderSpecifiers(
            Class<T> entityClass,
            String alias,
            Sort sort) {

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        PathBuilder<T> pathBuilder = new PathBuilder<>(entityClass, alias);

        for (Sort.Order sortOrder : sort) {
            String property = sortOrder.getProperty();
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

            orders.add(new OrderSpecifier(direction, pathBuilder.get(property)));
        }

        return orders;
    }
}
