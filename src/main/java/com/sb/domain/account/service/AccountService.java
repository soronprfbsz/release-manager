package com.sb.domain.account.service;

import com.sb.domain.account.dto.AccountDto;
import com.sb.domain.account.entity.Account;
import com.sb.domain.account.mapper.AccountDtoMapper;
import com.sb.domain.account.repository.AccountRepository;
import com.sb.global.exception.BusinessException;
import com.sb.global.exception.ErrorCode;
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

    public List<AccountDto.SimpleResponse> getAllAccounts() {
        return mapper.toSimpleResponseList(accountRepository.findAll());
    }

    public List<AccountDto.SimpleResponse> getAccountsByStatus(String status) {
        return mapper.toSimpleResponseList(
                accountRepository.findAllByStatus(status));
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

    @Transactional
    public void activateAccount(Long accountId) {
        // Verify account exists before activation
        findAccountByAccountId(accountId);
        accountRepository.activateByAccountId(accountId);
        log.info("Account activated with accountId: {}", accountId);
    }

    @Transactional
    public void deactivateAccount(Long accountId) {
        // Verify account exists before deactivation
        findAccountByAccountId(accountId);
        accountRepository.deactivateByAccountId(accountId);
        log.info("Account deactivated with accountId: {}", accountId);
    }

    public List<AccountDto.SimpleResponse> searchAccountsByName(
            String keyword) {
        return mapper.toSimpleResponseList(
                accountRepository.findByAccountNameContaining(keyword));
    }

    private Account findAccountByAccountId(Long accountId) {
        return accountRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND));
    }
}
