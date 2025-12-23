package com.ts.rm.domain.resourcefile.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.resourcefile.entity.QResourceFile;
import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ResourceFile Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 리소스 파일 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ResourceFileRepositoryImpl implements ResourceFileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QResourceFile resourceFile = QResourceFile.resourceFile;

    @Override
    public List<ResourceFile> findAllWithFilters(String fileCategory, String keyword) {
        return queryFactory
                .selectFrom(resourceFile)
                .where(
                        fileCategoryCondition(fileCategory),
                        keywordCondition(keyword)
                )
                .orderBy(resourceFile.sortOrder.asc(), resourceFile.createdAt.desc())
                .fetch();
    }

    /**
     * 파일 카테고리 조건
     */
    private BooleanExpression fileCategoryCondition(String fileCategory) {
        return (fileCategory != null && !fileCategory.trim().isEmpty())
                ? resourceFile.fileCategory.equalsIgnoreCase(fileCategory.trim())
                : null;
    }

    /**
     * 키워드 검색 조건 (리소스파일명, 파일명, 설명 통합 검색)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return resourceFile.resourceFileName.containsIgnoreCase(trimmedKeyword)
                .or(resourceFile.fileName.containsIgnoreCase(trimmedKeyword))
                .or(resourceFile.description.containsIgnoreCase(trimmedKeyword));
    }
}
