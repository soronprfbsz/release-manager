package com.ts.rm.domain.resourcelink.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.resourcelink.entity.QResourceLink;
import com.ts.rm.domain.resourcelink.entity.ResourceLink;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ResourceLink Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 리소스 링크 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ResourceLinkRepositoryImpl implements ResourceLinkRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QResourceLink resourceLink = QResourceLink.resourceLink;

    @Override
    public List<ResourceLink> findAllWithFilters(String linkCategory, String keyword) {
        return queryFactory
                .selectFrom(resourceLink)
                .where(
                        linkCategoryCondition(linkCategory),
                        keywordCondition(keyword)
                )
                .orderBy(resourceLink.sortOrder.asc(), resourceLink.createdAt.desc())
                .fetch();
    }

    /**
     * 링크 카테고리 조건
     */
    private BooleanExpression linkCategoryCondition(String linkCategory) {
        return (linkCategory != null && !linkCategory.trim().isEmpty())
                ? resourceLink.linkCategory.equalsIgnoreCase(linkCategory.trim())
                : null;
    }

    /**
     * 키워드 검색 조건 (링크명, 링크URL, 설명 통합 검색)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return resourceLink.linkName.containsIgnoreCase(trimmedKeyword)
                .or(resourceLink.linkUrl.containsIgnoreCase(trimmedKeyword))
                .or(resourceLink.description.containsIgnoreCase(trimmedKeyword));
    }

    @Override
    public Integer findMaxSortOrderByLinkCategory(String linkCategory) {
        Integer maxSortOrder = queryFactory
                .select(resourceLink.sortOrder.max())
                .from(resourceLink)
                .where(resourceLink.linkCategory.eq(linkCategory))
                .fetchOne();
        return maxSortOrder != null ? maxSortOrder : 0;
    }
}
