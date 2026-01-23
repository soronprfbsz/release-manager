package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardPostLike;
import com.ts.rm.domain.board.entity.BoardPostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BoardPostLike Repository
 *
 * <p>게시글 좋아요 데이터 접근 레이어
 */
@Repository
public interface BoardPostLikeRepository extends JpaRepository<BoardPostLike, BoardPostLikeId> {

    /**
     * 게시글 좋아요 존재 여부 확인
     *
     * @param postId    게시글 ID
     * @param accountId 계정 ID
     * @return 좋아요 존재 여부
     */
    boolean existsByPostIdAndAccountId(Long postId, Long accountId);

    /**
     * 게시글 좋아요 삭제
     *
     * @param postId    게시글 ID
     * @param accountId 계정 ID
     */
    void deleteByPostIdAndAccountId(Long postId, Long accountId);

    /**
     * 게시글의 좋아요 수 조회
     *
     * @param postId 게시글 ID
     * @return 좋아요 수
     */
    long countByPostId(Long postId);
}
