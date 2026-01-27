package com.ts.rm.domain.board.repository;

import java.time.LocalDateTime;

/**
 * BoardPostView Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리
 */
public interface BoardPostViewRepositoryCustom {

    /**
     * 오래된 조회 이력 삭제
     *
     * @param cutoffDate 기준 날짜
     * @return 삭제된 건수
     */
    long deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
