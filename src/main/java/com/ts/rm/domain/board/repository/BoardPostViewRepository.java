package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardPostView;
import com.ts.rm.domain.board.entity.BoardPostViewId;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * BoardPostView Repository
 *
 * <p>게시글 조회 이력 데이터 접근 레이어
 */
@Repository
public interface BoardPostViewRepository extends JpaRepository<BoardPostView, BoardPostViewId> {

    /**
     * 게시글 조회 이력 존재 여부 확인
     *
     * @param postId    게시글 ID
     * @param accountId 계정 ID
     * @return 조회 이력 존재 여부
     */
    boolean existsByPostIdAndAccountId(Long postId, Long accountId);

    /**
     * 오래된 조회 이력 삭제
     *
     * @param cutoffDate 기준 날짜
     * @return 삭제된 건수
     */
    @Modifying
    @Query("DELETE FROM BoardPostView v WHERE v.createdAt < :cutoffDate")
    int deleteByCreatedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
