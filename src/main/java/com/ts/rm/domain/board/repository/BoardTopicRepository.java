package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardTopic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BoardTopic Repository
 *
 * <p>게시판 토픽 데이터 접근 레이어
 */
@Repository
public interface BoardTopicRepository extends JpaRepository<BoardTopic, String> {

    /**
     * 활성화된 토픽 목록 조회 (정렬 순서)
     *
     * @return 활성화된 토픽 목록
     */
    List<BoardTopic> findAllByIsEnabledTrueOrderBySortOrderAsc();

    /**
     * 전체 토픽 목록 조회 (정렬 순서)
     *
     * @return 전체 토픽 목록
     */
    List<BoardTopic> findAllByOrderBySortOrderAsc();
}
