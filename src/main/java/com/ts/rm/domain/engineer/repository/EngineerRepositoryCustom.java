package com.ts.rm.domain.engineer.repository;

import com.ts.rm.domain.engineer.entity.Engineer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Engineer Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 엔지니어 쿼리 인터페이스
 */
public interface EngineerRepositoryCustom {

    /**
     * 엔지니어 목록 조회 (부서 필터링 + 키워드 검색)
     *
     * @param departmentId 부서 ID (null이면 전체)
     * @param keyword 검색 키워드 (이름, 이메일, 직급, 설명)
     * @param pageable 페이징 정보
     * @return 엔지니어 페이지
     */
    Page<Engineer> findAllWithFilters(Long departmentId, String keyword, Pageable pageable);
}
