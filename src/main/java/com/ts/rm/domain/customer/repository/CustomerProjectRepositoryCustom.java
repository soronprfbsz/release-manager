package com.ts.rm.domain.customer.repository;

import com.ts.rm.domain.customer.entity.CustomerProject;
import java.util.List;

/**
 * CustomerProject Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 고객사-프로젝트 매핑 쿼리 인터페이스
 */
public interface CustomerProjectRepositoryCustom {

    /**
     * 고객사 ID로 프로젝트 매핑 목록 조회 (Project fetch join)
     *
     * @param customerId 고객사 ID
     * @return CustomerProject 목록 (Project 포함)
     */
    List<CustomerProject> findAllByCustomerIdWithProject(Long customerId);

    /**
     * 프로젝트 ID로 고객사 매핑 목록 조회 (Customer fetch join)
     *
     * @param projectId 프로젝트 ID
     * @return CustomerProject 목록 (Customer 포함)
     */
    List<CustomerProject> findAllByProjectIdWithCustomer(String projectId);
}
