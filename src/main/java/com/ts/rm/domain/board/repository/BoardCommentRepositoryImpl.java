package com.ts.rm.domain.board.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.board.entity.BoardComment;
import com.ts.rm.domain.board.entity.QBoardComment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * BoardComment Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class BoardCommentRepositoryImpl implements BoardCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoardComment comment = QBoardComment.boardComment;
    private static final QBoardComment reply = new QBoardComment("reply");

    @Override
    public Page<BoardComment> findRootCommentsByPostId(Long postId, Pageable pageable) {
        List<BoardComment> content = queryFactory
                .selectFrom(comment)
                .leftJoin(comment.creator).fetchJoin()
                .where(
                        comment.post.postId.eq(postId),
                        comment.parentComment.isNull(),
                        comment.isDeleted.eq(false)
                                .or(JPAExpressions
                                        .selectOne()
                                        .from(reply)
                                        .where(
                                                reply.parentComment.commentId.eq(comment.commentId),
                                                reply.isDeleted.eq(false)
                                        )
                                        .exists())
                )
                .orderBy(comment.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.post.postId.eq(postId),
                        comment.parentComment.isNull(),
                        comment.isDeleted.eq(false)
                                .or(JPAExpressions
                                        .selectOne()
                                        .from(reply)
                                        .where(
                                                reply.parentComment.commentId.eq(comment.commentId),
                                                reply.isDeleted.eq(false)
                                        )
                                        .exists())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<BoardComment> findRepliesByParentCommentId(Long parentCommentId) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.creator).fetchJoin()
                .where(
                        comment.parentComment.commentId.eq(parentCommentId),
                        comment.isDeleted.eq(false)
                )
                .orderBy(comment.createdAt.asc())
                .fetch();
    }

    @Override
    public long countActiveCommentsByPostId(Long postId) {
        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.post.postId.eq(postId),
                        comment.isDeleted.eq(false)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public void incrementLikeCount(Long commentId) {
        queryFactory
                .update(comment)
                .set(comment.likeCount, comment.likeCount.add(1))
                .where(comment.commentId.eq(commentId))
                .execute();
    }

    @Override
    public void decrementLikeCount(Long commentId) {
        queryFactory
                .update(comment)
                .set(comment.likeCount,
                        new com.querydsl.core.types.dsl.CaseBuilder()
                                .when(comment.likeCount.gt(0)).then(comment.likeCount.subtract(1))
                                .otherwise(0))
                .where(comment.commentId.eq(commentId))
                .execute();
    }
}
