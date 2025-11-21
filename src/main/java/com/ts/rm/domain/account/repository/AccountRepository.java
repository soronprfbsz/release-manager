package com.ts.rm.domain.account.repository;

import com.ts.rm.domain.account.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Account Repository
 *
 * <p>Spring Data JPA 메서드 네이밍으로 CRUD 처리
 * <p>업데이트는 JPA Dirty Checking 사용 (Service에서 엔티티 조회 후 setter 호출)
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 이메일로 계정 조회
     */
    Optional<Account> findByEmail(String email);

    /**
     * ID로 계정 조회
     */
    Optional<Account> findByAccountId(Long accountId);

    /**
     * 상태별 계정 조회
     */
    List<Account> findAllByStatus(String status);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 계정 이름으로 검색 (부분 일치)
     */
    List<Account> findByAccountNameContaining(String keyword);
}
