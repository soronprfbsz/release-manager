package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BoardComment Repository
 *
 * <p>게시글 댓글 데이터 접근 레이어
 */
@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long>, BoardCommentRepositoryCustom {

    /**
     * 부모 댓글의 대댓글 존재 여부 확인
     *
     * @param parentCommentId 부모 댓글 ID
     * @return 대댓글 존재 여부
     */
    boolean existsByParentComment_CommentIdAndIsDeletedFalse(Long parentCommentId);
}
