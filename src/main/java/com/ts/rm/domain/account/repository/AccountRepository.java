package com.ts.rm.domain.account.repository;

import com.ts.rm.domain.account.entity.Account;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Account Repository
 *
 * <p>Spring Data JPA 메서드 네이밍으로 CRUD 처리
 * <p>업데이트는 JPA Dirty Checking 사용 (Service에서 엔티티 조회 후 setter 호출)
 */
public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {

    /**
     * 이메일로 계정 조회
     */
    Optional<Account> findByEmail(String email);

    /**
     * ID로 계정 조회
     */
    Optional<Account> findByAccountId(Long accountId);

    /**
     * 상태별 계정 조회 (페이징)
     */
    Page<Account> findAllByStatus(String status, Pageable pageable);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 계정 이름으로 검색 (부분 일치, 페이징)
     */
    Page<Account> findByAccountNameContaining(String keyword, Pageable pageable);

    /**
     * 특정 권한을 가진 계정 개수 조회
     *
     * @param role 권한 (ADMIN, USER 등)
     * @return 해당 권한을 가진 계정 개수
     */
    long countByRole(String role);

    /**
     * 특정 부서의 계정 수 조회
     */
    long countByDepartmentDepartmentId(Long departmentId);

    /**
     * 특정 부서의 계정 목록 조회 (페이징)
     */
    Page<Account> findByDepartmentDepartmentId(Long departmentId, Pageable pageable);

    /**
     * 특정 부서의 계정 목록 조회
     */
    List<Account> findByDepartmentDepartmentId(Long departmentId);

    /**
     * 부서별 계정 조회 + 이름 검색 (페이징)
     */
    Page<Account> findByDepartmentDepartmentIdAndAccountNameContaining(
            Long departmentId, String keyword, Pageable pageable);
}
