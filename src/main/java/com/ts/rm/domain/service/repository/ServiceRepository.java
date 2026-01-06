package com.ts.rm.domain.service.repository;

import com.ts.rm.domain.service.entity.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Service Repository
 *
 * <p>서비스 관리 Repository - Spring Data JPA 기반
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>, ServiceRepositoryCustom {

    /**
     * 서비스 ID로 서비스 조회 (컴포넌트 포함 fetch join)
     *
     * @param serviceId 서비스 ID
     * @return 서비스 (Optional)
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.components WHERE s.serviceId = :serviceId")
    Optional<Service> findByIdWithComponents(@Param("serviceId") Long serviceId);

    /**
     * 서비스 타입으로 조회 (컴포넌트 포함 fetch join)
     *
     * @param serviceType 서비스 타입
     * @return 서비스 목록
     */
    @Query("SELECT DISTINCT s FROM Service s LEFT JOIN FETCH s.components WHERE s.serviceType = :serviceType ORDER BY s.sortOrder ASC, s.createdAt DESC")
    List<Service> findByServiceTypeWithComponents(@Param("serviceType") String serviceType);

    /**
     * 서비스명으로 검색 (LIKE, 컴포넌트 포함 fetch join)
     *
     * @param serviceName 서비스명
     * @return 서비스 목록
     */
    @Query("SELECT DISTINCT s FROM Service s LEFT JOIN FETCH s.components WHERE s.serviceName LIKE %:serviceName% ORDER BY s.sortOrder ASC, s.createdAt DESC")
    List<Service> findByServiceNameContainingWithComponents(@Param("serviceName") String serviceName);

    /**
     * 전체 목록 조회 (컴포넌트 포함 fetch join, sortOrder 오름차순 → 생성일시 내림차순)
     *
     * @return 서비스 목록
     */
    @Query("SELECT DISTINCT s FROM Service s LEFT JOIN FETCH s.components ORDER BY s.sortOrder ASC, s.createdAt DESC")
    List<Service> findAllWithComponents();

    /**
     * 서비스 타입별 서비스 수 조회
     *
     * @param serviceType 서비스 타입
     * @return 서비스 개수
     */
    long countByServiceType(String serviceType);

    /**
     * 서비스 타입별 최대 sortOrder 조회
     *
     * @param serviceType 서비스 타입
     * @return 최대 sortOrder (데이터가 없으면 null)
     */
    @Query("SELECT MAX(s.sortOrder) FROM Service s WHERE s.serviceType = :serviceType")
    Integer findMaxSortOrderByServiceType(@Param("serviceType") String serviceType);
}
