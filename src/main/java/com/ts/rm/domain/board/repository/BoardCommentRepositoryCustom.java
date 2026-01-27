package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardComment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * BoardComment Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리
 */
public interface BoardCommentRepositoryCustom {

    /**
     * 게시글의 최상위 댓글 목록 조회 (페이징)
     */
    Page<BoardComment> findRootCommentsByPostId(Long postId, Pageable pageable);

    /**
     * 부모 댓글의 대댓글 목록 조회
     */
    List<BoardComment> findRepliesByParentCommentId(Long parentCommentId);

    /**
     * 게시글의 전체 댓글 수 조회 (삭제되지 않은 것만)
     */
    long countActiveCommentsByPostId(Long postId);

    /**
     * 좋아요 수 증가
     */
    void incrementLikeCount(Long commentId);

    /**
     * 좋아요 수 감소
     */
    void decrementLikeCount(Long commentId);
}
