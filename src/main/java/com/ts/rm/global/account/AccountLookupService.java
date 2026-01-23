package com.ts.rm.global.account;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Account 조회 공통 서비스
 *
 * <p>여러 도메인에서 이메일을 통한 Account 조회가 필요할 때 사용
 */
@Service
@RequiredArgsConstructor
public class AccountLookupService {

    private final AccountRepository accountRepository;

    /**
     * 이메일로 Account 조회 (없으면 null 반환)
     *
     * @param email 이메일
     * @return Account 엔티티 (없으면 null)
     */
    public Account findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return accountRepository.findByEmail(email).orElse(null);
    }

    /**
     * 이메일로 Account ID 조회 (없으면 null 반환)
     *
     * @param email 이메일
     * @return Account ID (없으면 null)
     */
    public Long findAccountIdByEmail(String email) {
        Account account = findByEmail(email);
        return account != null ? account.getAccountId() : null;
    }

    /**
     * Account ID로 Account 조회 (없으면 null 반환)
     *
     * @param accountId Account ID
     * @return Account 엔티티 (없으면 null)
     */
    public Account findById(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return accountRepository.findById(accountId).orElse(null);
    }
}
