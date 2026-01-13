package com.ts.rm.domain.customer.repository;

import com.ts.rm.domain.customer.entity.CustomerNote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CustomerNote Repository
 *
 * <p>고객사 특이사항 데이터 접근 레이어
 */
@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    /**
     * 고객사 ID로 특이사항 목록 조회 (최신순)
     *
     * @param customerId 고객사 ID
     * @return 특이사항 목록
     */
    List<CustomerNote> findAllByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * 고객사 ID로 특이사항 개수 조회
     *
     * @param customerId 고객사 ID
     * @return 특이사항 개수
     */
    long countByCustomer_CustomerId(Long customerId);
}
