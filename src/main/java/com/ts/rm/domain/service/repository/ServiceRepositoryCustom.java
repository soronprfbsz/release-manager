package com.ts.rm.domain.service.repository;

import com.ts.rm.domain.service.entity.Service;
import java.util.List;

/**
 * Service Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 서비스 쿼리 인터페이스
 */
public interface ServiceRepositoryCustom {

    /**
     * 서비스 목록 조회 (필터링 + 키워드 검색)
     *
     * @param serviceType 서비스 타입 (null이면 전체)
     * @param keyword 검색 키워드 (서비스명, 서비스타입, 설명)
     * @return 서비스 목록 (컴포넌트 포함)
     */
    List<Service> findAllWithFilters(String serviceType, String keyword);
}
