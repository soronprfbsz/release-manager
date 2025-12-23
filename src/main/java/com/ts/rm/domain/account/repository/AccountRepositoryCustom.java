package com.ts.rm.domain.account.repository;

import com.ts.rm.domain.account.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Account Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 계정 쿼리 인터페이스
 */
public interface AccountRepositoryCustom {

    /**
     * 계정 목록 조회 (상태 필터링 + 키워드 검색)
     *
     * @param status 계정 상태 (null이면 전체)
     * @param keyword 검색 키워드 (계정명, 이메일)
     * @param pageable 페이징 정보
     * @return 계정 페이지
     */
    Page<Account> findAllWithFilters(String status, String keyword, Pageable pageable);
}
