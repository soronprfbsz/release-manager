package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardComment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * BoardComment Repository
 *
 * <p>게시글 댓글 데이터 접근 레이어
 */
@Repository
public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    /**
     * 게시글의 최상위 댓글 목록 조회 (페이징)
     *
     * <p>삭제되지 않은 댓글 + 삭제되었지만 활성 대댓글이 있는 댓글
     *
     * @param postId   게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    @Query("SELECT c FROM BoardComment c " +
           "LEFT JOIN FETCH c.creator " +
           "WHERE c.post.postId = :postId " +
           "AND c.parentComment IS NULL " +
           "AND (c.isDeleted = false OR EXISTS (" +
           "    SELECT 1 FROM BoardComment r " +
           "    WHERE r.parentComment.commentId = c.commentId AND r.isDeleted = false" +
           ")) " +
           "ORDER BY c.createdAt ASC")
    Page<BoardComment> findRootCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    /**
     * 부모 댓글의 대댓글 목록 조회 (삭제되지 않은 것만)
     *
     * @param parentCommentId 부모 댓글 ID
     * @return 대댓글 목록
     */
    @Query("SELECT c FROM BoardComment c " +
           "LEFT JOIN FETCH c.creator " +
           "WHERE c.parentComment.commentId = :parentCommentId " +
           "AND c.isDeleted = false " +
           "ORDER BY c.createdAt ASC")
    List<BoardComment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    /**
     * 게시글의 전체 댓글 수 조회 (삭제되지 않은 것만)
     *
     * @param postId 게시글 ID
     * @return 댓글 수
     */
    @Query("SELECT COUNT(c) FROM BoardComment c " +
           "WHERE c.post.postId = :postId AND c.isDeleted = false")
    long countActiveCommentsByPostId(@Param("postId") Long postId);

    /**
     * 좋아요 수 증가
     *
     * @param commentId 댓글 ID
     */
    @Modifying
    @Query("UPDATE BoardComment c SET c.likeCount = c.likeCount + 1 WHERE c.commentId = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 좋아요 수 감소
     *
     * @param commentId 댓글 ID
     */
    @Modifying
    @Query("UPDATE BoardComment c SET c.likeCount = CASE WHEN c.likeCount > 0 THEN c.likeCount - 1 ELSE 0 END WHERE c.commentId = :commentId")
    void decrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 부모 댓글의 대댓글 존재 여부 확인
     *
     * @param parentCommentId 부모 댓글 ID
     * @return 대댓글 존재 여부
     */
    boolean existsByParentComment_CommentIdAndIsDeletedFalse(Long parentCommentId);
}
