package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardPostView;
import com.ts.rm.domain.board.entity.BoardPostViewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BoardPostView Repository
 *
 * <p>게시글 조회 이력 데이터 접근 레이어
 */
@Repository
public interface BoardPostViewRepository extends JpaRepository<BoardPostView, BoardPostViewId>,
        BoardPostViewRepositoryCustom {

    /**
     * 게시글 조회 이력 존재 여부 확인
     *
     * @param postId    게시글 ID
     * @param accountId 계정 ID
     * @return 조회 이력 존재 여부
     */
    boolean existsByPostIdAndAccountId(Long postId, Long accountId);
}
