package com.ts.rm.global.logging.repository;

import com.ts.rm.global.logging.dto.ApiLogDto;
import com.ts.rm.global.logging.entity.ApiLog;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ApiLog Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리
 */
public interface ApiLogRepositoryCustom {

    /**
     * 오래된 로그 삭제
     *
     * @param cutoffDate 기준 날짜
     * @return 삭제된 건수
     */
    long deleteByCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * API 로그 검색 (페이징)
     *
     * @param condition 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    Page<ApiLog> searchWithFilters(ApiLogDto.SearchCondition condition, Pageable pageable);
}
