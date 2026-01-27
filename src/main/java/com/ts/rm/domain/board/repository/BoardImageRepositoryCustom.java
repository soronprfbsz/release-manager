package com.ts.rm.domain.board.repository;

import java.time.LocalDateTime;

/**
 * BoardImage Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리
 */
public interface BoardImageRepositoryCustom {

    /**
     * 미사용 이미지 삭제 (배치 작업용)
     *
     * @param threshold 기준 시각
     * @return 삭제된 건수
     */
    long deleteOrphanedImagesBefore(LocalDateTime threshold);

    /**
     * 게시글의 이미지 연결 해제 (게시글 삭제 전 호출)
     *
     * @param postId 게시글 ID
     * @return 업데이트된 건수
     */
    long unlinkImagesFromPost(Long postId);
}
