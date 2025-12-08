package com.ts.rm.domain.account.service;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.enums.AccountRole;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.mapper.AccountDtoMapper;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.pagination.PageRowNumberUtil;
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
     * 계정 목록 조회 (필터링 및 검색, 페이징)
     *
     * @param status   계정 상태 필터 (ACTIVE, INACTIVE 등, null이면 전체)
     * @param keyword  계정명 검색 키워드
     * @param pageable 페이징 정보
     * @return 계정 페이지
     */
    public org.springframework.data.domain.Page<AccountDto.ListResponse> getAccounts(
            AccountStatus status, String keyword, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Account> accountPage;

        // 키워드 검색이 있는 경우
        if (keyword != null && !keyword.trim().isEmpty()) {
            accountPage = accountRepository.findByAccountNameContaining(keyword.trim(), pageable);
        }
        // 상태 필터링
        else if (status != null) {
            accountPage = accountRepository.findAllByStatus(status.name(), pageable);
        }
        // 전체 조회
        else {
            accountPage = accountRepository.findAll(pageable);
        }

        // rowNumber 계산 (공통 유틸리티 사용)
        return PageRowNumberUtil.mapWithRowNumber(accountPage, (account, rowNumber) ->
                new AccountDto.ListResponse(
                        rowNumber,
                        account.getAccountId(),
                        account.getEmail(),
                        account.getAccountName(),
                        account.getRole(),
                        account.getStatus(),
                        account.getLastLoginAt(),
                        account.getCreatedAt()
                )
        );
    }

    @Transactional
    public AccountDto.DetailResponse updateAccount(Long accountId,
            AccountDto.UpdateRequest request) {
        log.info("Updating account with accountId: {}", accountId);

        // 엔티티 조회
        Account account = findAccountByAccountId(accountId);

        // Setter를 통한 수정 (JPA Dirty Checking)
        if (request.accountName() != null) {
            account.setAccountName(request.accountName());
        }

        if (request.password() != null) {
            account.setPassword(request.password());
        }

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Account updated successfully with accountId: {}", accountId);
        return mapper.toDetailResponse(account);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Deleting account with accountId: {}", accountId);

        // JpaRepository의 deleteById 사용 (존재하지 않으면 EmptyResultDataAccessException 발생)
        accountRepository.deleteById(accountId);

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

        // 엔티티 조회 후 setter를 통한 수정 (JPA Dirty Checking)
        Account account = findAccountByAccountId(accountId);
        account.setStatus(status.name());

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Account status updated - accountId: {}, status: {}", accountId, status);
    }

    /**
     * 계정 수정 (ADMIN 전용 - 권한, 상태 수정)
     *
     * @param accountId 계정 ID
     * @param request   수정 요청 (role, status)
     * @return 수정된 계정 상세 정보
     */
    @Transactional
    public AccountDto.DetailResponse adminUpdateAccount(Long accountId,
            AccountDto.AdminUpdateRequest request) {
        log.info("Admin updating account with accountId: {}", accountId);

        // 엔티티 조회
        Account account = findAccountByAccountId(accountId);

        // Setter를 통한 수정 (JPA Dirty Checking)
        if (request.role() != null && !request.role().isBlank()) {
            // role 유효성 검증
            try {
                AccountRole.valueOf(request.role());
                account.setRole(request.role());
                log.debug("Role updated to {} for accountId: {}", request.role(), accountId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role value: {}", request.role());
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        if (request.status() != null && !request.status().isBlank()) {
            // status 유효성 检证
            try {
                AccountStatus.valueOf(request.status());
                account.setStatus(request.status());
                log.debug("Status updated to {} for accountId: {}", request.status(), accountId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", request.status());
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Account updated successfully by admin with accountId: {}", accountId);
        return mapper.toDetailResponse(account);
    }

    private Account findAccountByAccountId(Long accountId) {
        return accountRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND));
    }
}
