package com.ts.rm.domain.service.repository;

import com.ts.rm.domain.service.entity.Service;
import java.util.List;
import java.util.Optional;

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

    /**
     * 서비스 ID로 서비스 조회 (컴포넌트 포함)
     */
    Optional<Service> findByIdWithComponents(Long serviceId);

    /**
     * 서비스 타입으로 조회 (컴포넌트 포함)
     */
    List<Service> findByServiceTypeWithComponents(String serviceType);

    /**
     * 서비스명으로 검색 (LIKE, 컴포넌트 포함)
     */
    List<Service> findByServiceNameContainingWithComponents(String serviceName);

    /**
     * 전체 목록 조회 (컴포넌트 포함)
     */
    List<Service> findAllWithComponents();

    /**
     * 서비스 타입별 최대 sortOrder 조회
     */
    Integer findMaxSortOrderByServiceType(String serviceType);
}
