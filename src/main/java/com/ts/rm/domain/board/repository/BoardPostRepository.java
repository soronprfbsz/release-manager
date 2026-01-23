package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardPost;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * BoardPost Repository
 *
 * <p>게시글 데이터 접근 레이어
 */
@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long>, BoardPostRepositoryCustom {

    /**
     * 게시글 상세 조회 (토픽 정보 포함)
     *
     * @param postId 게시글 ID
     * @return 게시글 (Optional)
     */
    @Query("SELECT p FROM BoardPost p " +
           "LEFT JOIN FETCH p.topic " +
           "LEFT JOIN FETCH p.creator " +
           "WHERE p.postId = :postId")
    Optional<BoardPost> findByIdWithTopic(@Param("postId") Long postId);

    /**
     * 조회수 증가
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE BoardPost p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    /**
     * 좋아요 수 증가
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE BoardPost p SET p.likeCount = p.likeCount + 1 WHERE p.postId = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    /**
     * 좋아요 수 감소
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE BoardPost p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.postId = :postId")
    void decrementLikeCount(@Param("postId") Long postId);

    /**
     * 댓글 수 증가
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE BoardPost p SET p.commentCount = p.commentCount + 1 WHERE p.postId = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    /**
     * 댓글 수 감소
     *
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("UPDATE BoardPost p SET p.commentCount = CASE WHEN p.commentCount > 0 THEN p.commentCount - 1 ELSE 0 END WHERE p.postId = :postId")
    void decrementCommentCount(@Param("postId") Long postId);

    /**
     * 토픽별 게시글 개수 조회
     *
     * @param topicId 토픽 ID
     * @return 게시글 개수
     */
    long countByTopic_TopicId(String topicId);
}
