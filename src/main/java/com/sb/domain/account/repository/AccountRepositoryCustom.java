package com.sb.domain.account.repository;

/**
 * Account Repository Custom Interface
 *
 * <p>Spring Data JPA 메서드 네이밍으로 불가능한 복잡한 쿼리를 QueryDSL로 구현 (주로 UPDATE, DELETE,
 * 복잡한 동적 쿼리)
 */
public interface AccountRepositoryCustom {

    /**
     * 계정 삭제 (Hard Delete - QueryDSL)
     *
     * @param accountId 계정 ID
     * @return 삭제된 행 수
     */
    long deleteAccountByAccountId(Long accountId);

    /**
     * 계정 활성화 (QueryDSL)
     *
     * @param accountId 계정 ID
     * @return 업데이트된 행 수
     */
    long activateByAccountId(Long accountId);

    /**
     * 계정 비활성화 (QueryDSL)
     *
     * @param accountId 계정 ID
     * @return 업데이트된 행 수
     */
    long deactivateByAccountId(Long accountId);

    /**
     * 계정 이름 업데이트 (QueryDSL)
     *
     * @param accountId   계정 ID
     * @param accountName 새 계정 이름
     * @return 업데이트된 행 수
     */
    long updateAccountNameByAccountId(Long accountId, String accountName);

    /**
     * 비밀번호 업데이트 (QueryDSL)
     *
     * @param accountId 계정 ID
     * @param password  새 비밀번호
     * @return 업데이트된 행 수
     */
    long updatePasswordByAccountId(Long accountId, String password);
}
