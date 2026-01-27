package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BoardPost Repository
 *
 * <p>게시글 데이터 접근 레이어
 */
@Repository
public interface BoardPostRepository extends JpaRepository<BoardPost, Long>, BoardPostRepositoryCustom {

    /**
     * 토픽별 게시글 개수 조회
     *
     * @param topicId 토픽 ID
     * @return 게시글 개수
     */
    long countByTopic_TopicId(String topicId);
}
