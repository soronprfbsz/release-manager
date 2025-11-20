package com.ts.rm.domain.customer.repository;

/**
 * Customer Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
 */
public interface CustomerRepositoryCustom {

    /**
     * 고객사 활성화
     *
     * @param customerId 고객사 ID
     * @return 업데이트된 행 수
     */
    long activateByCustomerId(Long customerId);

    /**
     * 고객사 비활성화
     *
     * @param customerId 고객사 ID
     * @return 업데이트된 행 수
     */
    long deactivateByCustomerId(Long customerId);

    /**
     * 고객사 정보 업데이트
     *
     * @param customerId   고객사 ID
     * @param customerName 고객사명
     * @param description  설명
     * @return 업데이트된 행 수
     */
    long updateCustomerInfoByCustomerId(Long customerId, String customerName, String description);
}
