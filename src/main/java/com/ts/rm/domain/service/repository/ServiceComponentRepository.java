package com.ts.rm.domain.service.repository;

import com.ts.rm.domain.service.entity.ServiceComponent;
import com.ts.rm.domain.service.enums.ComponentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ServiceComponent Repository
 *
 * <p>서비스 컴포넌트 Repository - Spring Data JPA 기반
 */
@Repository
public interface ServiceComponentRepository extends JpaRepository<ServiceComponent, Long> {

    /**
     * 서비스 ID로 컴포넌트 목록 조회 (정렬 순서대로)
     *
     * @param serviceId 서비스 ID
     * @return 컴포넌트 목록
     */
    List<ServiceComponent> findByService_ServiceIdOrderBySortOrderAsc(Long serviceId);

    /**
     * 서비스 ID로 컴포넌트 목록 조회 (생성일시 순서대로)
     *
     * @param serviceId 서비스 ID
     * @return 컴포넌트 목록
     */
    List<ServiceComponent> findByService_ServiceIdOrderByCreatedAtAsc(Long serviceId);

    /**
     * 서비스 ID와 컴포넌트 타입으로 조회
     *
     * @param serviceId     서비스 ID
     * @param componentType 컴포넌트 타입
     * @return 컴포넌트 목록
     */
    List<ServiceComponent> findByService_ServiceIdAndComponentTypeOrderBySortOrderAsc(
            Long serviceId, ComponentType componentType);

    /**
     * 서비스 ID로 컴포넌트 개수 조회
     *
     * @param serviceId 서비스 ID
     * @return 컴포넌트 개수
     */
    long countByService_ServiceId(Long serviceId);

    /**
     * 서비스 ID와 컴포넌트 타입으로 개수 조회
     *
     * @param serviceId     서비스 ID
     * @param componentType 컴포넌트 타입
     * @return 컴포넌트 개수
     */
    long countByService_ServiceIdAndComponentType(Long serviceId, ComponentType componentType);

    /**
     * 서비스 ID로 컴포넌트 삭제
     *
     * @param serviceId 서비스 ID
     */
    void deleteByService_ServiceId(Long serviceId);
}
