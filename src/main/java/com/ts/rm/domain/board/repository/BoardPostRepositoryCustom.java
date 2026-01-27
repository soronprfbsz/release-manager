package com.ts.rm.domain.board.repository;

import com.ts.rm.domain.board.entity.BoardPost;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * BoardPost Repository Custom
 *
 * <p>QueryDSL을 사용한 복잡한 게시글 쿼리
 */
public interface BoardPostRepositoryCustom {

    /**
     * 게시글 목록 페이징 조회
     *
     * <p>토픽별 필터링, 키워드 검색, 정렬 지원
     *
     * @param topicId     토픽 ID (null이면 전체)
     * @param keyword     검색 키워드 (제목, 내용 검색)
     * @param isPublished 발행 여부 (null이면 전체)
     * @param pageable    페이징 정보
     * @return 게시글 페이지
     */
    Page<BoardPost> findAllWithFilters(String topicId, String keyword, Boolean isPublished, Pageable pageable);

    /**
     * 게시글 상세 조회 (토픽, 작성자 정보 포함)
     */
    Optional<BoardPost> findByIdWithTopic(Long postId);

    /**
     * 조회수 증가
     */
    void incrementViewCount(Long postId);

    /**
     * 좋아요 수 증가
     */
    void incrementLikeCount(Long postId);

    /**
     * 좋아요 수 감소
     */
    void decrementLikeCount(Long postId);

    /**
     * 댓글 수 증가
     */
    void incrementCommentCount(Long postId);

    /**
     * 댓글 수 감소
     */
    void decrementCommentCount(Long postId);
}
