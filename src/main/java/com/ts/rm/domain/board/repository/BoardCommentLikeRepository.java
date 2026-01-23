package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardCommentLike;
import com.ts.rm.domain.board.entity.BoardCommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BoardCommentLike Repository
 *
 * <p>댓글 좋아요 데이터 접근 레이어
 */
@Repository
public interface BoardCommentLikeRepository extends JpaRepository<BoardCommentLike, BoardCommentLikeId> {

    /**
     * 댓글 좋아요 존재 여부 확인
     *
     * @param commentId 댓글 ID
     * @param accountId 계정 ID
     * @return 좋아요 존재 여부
     */
    boolean existsByCommentIdAndAccountId(Long commentId, Long accountId);

    /**
     * 댓글 좋아요 삭제
     *
     * @param commentId 댓글 ID
     * @param accountId 계정 ID
     */
    void deleteByCommentIdAndAccountId(Long commentId, Long accountId);

    /**
     * 댓글의 좋아요 수 조회
     *
     * @param commentId 댓글 ID
     * @return 좋아요 수
     */
    long countByCommentId(Long commentId);
}
