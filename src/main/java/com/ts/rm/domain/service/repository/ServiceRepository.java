package com.ts.rm.domain.service.repository;

import com.ts.rm.domain.service.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Service Repository
 *
 * <p>서비스 관리 Repository - Spring Data JPA 기반
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>, ServiceRepositoryCustom {

    /**
     * 서비스 타입별 서비스 수 조회
     *
     * @param serviceType 서비스 타입
     * @return 서비스 개수
     */
    long countByServiceType(String serviceType);
}
