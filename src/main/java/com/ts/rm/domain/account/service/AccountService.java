package com.ts.rm.domain.account.service;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.mapper.AccountDtoMapper;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account Service - MapStruct를 사용한 Entity ↔ DTO 자동 변환 - AccountDto 단일 클래스에서
 * Request/Response DTO 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountDtoMapper mapper;

    @Transactional
    public AccountDto.DetailResponse createAccount(
            AccountDto.CreateRequest request) {
        log.info("Creating account with email: {}", request.email());

        if (accountRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ACCOUNT_EMAIL_CONFLICT);
        }

        Account account = mapper.toEntity(request);
        Account savedAccount = accountRepository.save(account);

        log.info("Account created successfully with id: {}",
                savedAccount.getAccountId());
        return mapper.toDetailResponse(savedAccount);
    }

    public AccountDto.DetailResponse getAccountByAccountId(Long accountId) {
        Account account = findAccountByAccountId(accountId);
        return mapper.toDetailResponse(account);
    }

    /**
     * 계정 목록 조회 (필터링 및 검색)
     *
     * @param status  계정 상태 필터 (ACCOUNT_STATUS_ACTIVE, ACCOUNT_STATUS_INACTIVE 등, null이면 전체)
     * @param keyword 계정명 검색 키워드
     * @return 계정 목록
     */
    public List<AccountDto.SimpleResponse> getAccounts(AccountStatus status, String keyword) {
        List<Account> accounts;

        // 키워드 검색이 있는 경우
        if (keyword != null && !keyword.trim().isEmpty()) {
            accounts = accountRepository.findByAccountNameContaining(keyword.trim());
        }
        // 상태 필터링
        else if (status != null) {
            accounts = accountRepository.findAllByStatus(status.name());
        }
        // 전체 조회
        else {
            accounts = accountRepository.findAll();
        }

        return mapper.toSimpleResponseList(accounts);
    }

    @Transactional
    public AccountDto.DetailResponse updateAccount(Long accountId,
            AccountDto.UpdateRequest request) {
        log.info("Updating account with accountId: {}", accountId);

        Account account = findAccountByAccountId(accountId);

        if (request.accountName() != null) {
            accountRepository.updateAccountNameByAccountId(accountId,
                    request.accountName());
        }

        if (request.password() != null) {
            accountRepository.updatePasswordByAccountId(accountId,
                    request.password());
        }

        // Re-fetch the updated account to return latest data
        Account updatedAccount = findAccountByAccountId(accountId);

        log.info("Account updated successfully with accountId: {}", accountId);
        return mapper.toDetailResponse(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Deleting account with accountId: {}", accountId);

        // Verify account exists before delete
        findAccountByAccountId(accountId);
        accountRepository.deleteAccountByAccountId(accountId);

        log.info("Account deleted successfully with accountId: {}", accountId);
    }

    /**
     * 계정 상태 변경
     *
     * @param accountId 계정 ID
     * @param status    변경할 상태
     */
    @Transactional
    public void updateAccountStatus(Long accountId, AccountStatus status) {
        log.info("Updating account status - accountId: {}, status: {}", accountId, status);

        // 계정 존재 검증
        findAccountByAccountId(accountId);

        if (status == AccountStatus.ACCOUNT_STATUS_ACTIVE) {
            accountRepository.activateByAccountId(accountId);
        } else if (status == AccountStatus.ACCOUNT_STATUS_INACTIVE) {
            accountRepository.deactivateByAccountId(accountId);
        }

        log.info("Account status updated - accountId: {}, status: {}", accountId, status);
    }

    private Account findAccountByAccountId(Long accountId) {
        return accountRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND));
    }
}
