package com.ts.rm.domain.service.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.service.entity.QService;
import com.ts.rm.domain.service.entity.QServiceComponent;
import com.ts.rm.domain.service.entity.Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Service Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 서비스 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ServiceRepositoryImpl implements ServiceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QService service = QService.service;
    private static final QServiceComponent component = QServiceComponent.serviceComponent;

    @Override
    public List<Service> findAllWithFilters(String serviceType, String keyword) {
        return queryFactory
                .selectDistinct(service)
                .from(service)
                .leftJoin(service.components, component).fetchJoin()
                .where(
                        serviceTypeCondition(serviceType),
                        keywordCondition(keyword)
                )
                .orderBy(service.sortOrder.asc(), service.createdAt.desc())
                .fetch();
    }

    /**
     * 서비스 타입 조건
     */
    private BooleanExpression serviceTypeCondition(String serviceType) {
        return (serviceType != null && !serviceType.trim().isEmpty())
                ? service.serviceType.eq(serviceType.trim())
                : null;
    }

    /**
     * 키워드 검색 조건 (서비스명, 서비스타입, 설명 통합 검색)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return service.serviceName.containsIgnoreCase(trimmedKeyword)
                .or(service.serviceType.containsIgnoreCase(trimmedKeyword))
                .or(service.description.containsIgnoreCase(trimmedKeyword));
    }
}
