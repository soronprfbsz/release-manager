package com.sb.domain.account.repository;

import com.sb.domain.account.entity.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Account Repository
 *
 * <p>우선순위: 1. Spring Data JPA (간단한 CRUD 조회) 2. QueryDSL (복잡한 UPDATE/DELETE, 동적
 * 쿼리) →
 * AccountRepositoryCustom
 */
public interface AccountRepository extends JpaRepository<Account, Long>,
        AccountRepositoryCustom {

    // ============ Spring Data JPA 메서드 네이밍 ============

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

    // ============ QueryDSL 메서드 (AccountRepositoryCustom) ============
    // AccountRepositoryCustom 인터페이스에 정의되고
    // AccountRepositoryImpl에서 QueryDSL로 구현됨:
    //
    // - long deleteAccountByAccountId(Long accountId)
    // - long activateByAccountId(Long accountId)
    // - long deactivateByAccountId(Long accountId)
    // - long updateAccountNameByAccountId(Long accountId, String accountName)
    // - long updatePasswordByAccountId(Long accountId, String password)
}
