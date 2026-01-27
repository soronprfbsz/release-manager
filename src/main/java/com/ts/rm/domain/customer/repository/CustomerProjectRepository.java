package com.ts.rm.domain.customer.repository;

import com.ts.rm.domain.customer.entity.CustomerProject;
import com.ts.rm.domain.customer.entity.CustomerProjectId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CustomerProject Repository
 *
 * <p>고객사-프로젝트 매핑 정보 조회 및 관리
 */
public interface CustomerProjectRepository extends JpaRepository<CustomerProject, CustomerProjectId>,
        CustomerProjectRepositoryCustom {

    /**
     * 고객사 ID로 프로젝트 매핑 목록 조회
     *
     * @param customerId 고객사 ID
     * @return CustomerProject 목록
     */
    List<CustomerProject> findAllByCustomer_CustomerId(Long customerId);

    /**
     * 프로젝트 ID로 고객사 매핑 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @return CustomerProject 목록
     */
    List<CustomerProject> findAllByProject_ProjectId(String projectId);

    /**
     * 고객사 ID와 프로젝트 ID로 매핑 조회
     *
     * @param customerId 고객사 ID
     * @param projectId  프로젝트 ID
     * @return CustomerProject
     */
    Optional<CustomerProject> findByCustomer_CustomerIdAndProject_ProjectId(Long customerId, String projectId);

    /**
     * 고객사의 특정 프로젝트 매핑 존재 여부 확인
     *
     * @param customerId 고객사 ID
     * @param projectId  프로젝트 ID
     * @return 존재 여부
     */
    boolean existsByCustomer_CustomerIdAndProject_ProjectId(Long customerId, String projectId);
}
