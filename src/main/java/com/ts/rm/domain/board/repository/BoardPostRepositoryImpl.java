package com.ts.rm.domain.board.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.board.entity.BoardPost;
import com.ts.rm.domain.board.entity.QBoardPost;
import com.ts.rm.domain.board.entity.QBoardTopic;
import com.ts.rm.global.querydsl.QuerydslPaginationUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * BoardPost Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 게시글 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class BoardPostRepositoryImpl implements BoardPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoardPost post = QBoardPost.boardPost;
    private static final QBoardTopic topic = QBoardTopic.boardTopic;

    @Override
    public Page<BoardPost> findAllWithFilters(String topicId, String keyword, Boolean isPublished, Pageable pageable) {
        // 1. 기본 쿼리 생성
        JPAQuery<BoardPost> contentQuery = queryFactory
                .selectFrom(post)
                .leftJoin(post.topic, topic).fetchJoin()
                .leftJoin(post.creator).fetchJoin()
                .where(
                        topicIdCondition(topicId),
                        keywordCondition(keyword),
                        isPublishedCondition(isPublished)
                );

        // 2. Count 쿼리 생성
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        topicIdCondition(topicId),
                        keywordCondition(keyword),
                        isPublishedCondition(isPublished)
                );

        // 3. 정렬 필드 매핑 정의
        Map<String, com.querydsl.core.types.Expression<?>> sortMapping = Map.of(
                "postId", post.postId,
                "title", post.title,
                "viewCount", post.viewCount,
                "likeCount", post.likeCount,
                "commentCount", post.commentCount,
                "createdAt", post.createdAt,
                "updatedAt", post.updatedAt,
                "isPinned", post.isPinned
        );

        // 4. 우선 정렬: 상단 고정 게시글 먼저
        List<OrderSpecifier<?>> prefixOrders = List.of(post.isPinned.desc());

        // 5. 공통 유틸리티로 페이징/정렬 적용
        return QuerydslPaginationUtil.applyPagination(
                contentQuery,
                countQuery,
                pageable,
                sortMapping,
                prefixOrders,
                post.createdAt.desc() // 기본 정렬: 최신순
        );
    }

    /**
     * 토픽 ID 조건
     */
    private BooleanExpression topicIdCondition(String topicId) {
        return (topicId != null && !topicId.isEmpty()) ? post.topic.topicId.eq(topicId) : null;
    }

    /**
     * 키워드 검색 조건 (제목, 내용)
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String trimmedKeyword = keyword.trim();
        return post.title.containsIgnoreCase(trimmedKeyword)
                .or(post.content.containsIgnoreCase(trimmedKeyword));
    }

    /**
     * 발행 여부 조건
     */
    private BooleanExpression isPublishedCondition(Boolean isPublished) {
        return isPublished != null ? post.isPublished.eq(isPublished) : null;
    }
}
