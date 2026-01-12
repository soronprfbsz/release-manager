package com.ts.rm.domain.common.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.global.security.AccountUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체
 * - 사용자 인증 정보 로드
 * - accountId 또는 email로 사용자 정보 조회
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    /**
     * 이메일로 사용자 조회 (Spring Security 기본 인터페이스)
     * 로그인 시 사용
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return buildAccountUserDetails(account);
    }

    /**
     * accountId로 사용자 조회
     * JWT 토큰 검증 시 사용
     *
     * @param accountId 계정 ID
     * @return AccountUserDetails
     * @throws UsernameNotFoundException 계정을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public AccountUserDetails loadUserByAccountId(Long accountId) throws UsernameNotFoundException {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with accountId: " + accountId));

        return buildAccountUserDetails(account);
    }

    /**
     * Account 엔티티로부터 AccountUserDetails 생성
     */
    private AccountUserDetails buildAccountUserDetails(Account account) {
        return AccountUserDetails.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .accountName(account.getAccountName())
                .password(account.getPassword())
                .role(account.getRole())
                .departmentId(account.getDepartment() != null ? account.getDepartment().getDepartmentId() : null)
                .departmentName(account.getDepartment() != null ? account.getDepartment().getDepartmentName() : null)
                .build();
    }
}
