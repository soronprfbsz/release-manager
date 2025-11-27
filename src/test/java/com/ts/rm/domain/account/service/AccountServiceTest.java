package com.ts.rm.domain.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.mapper.AccountDtoMapper;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Account Service 단위 테스트
 *
 * <p>[@ExtendWith(MockitoExtension.class)] - Mockito를 사용한 단위 테스트 - Repository와 Mapper를
 * Mock으로 대체 - 비즈니스 로직만 집중 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 테스트")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountDtoMapper mapper;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private AccountDto.CreateRequest createRequest;
    private AccountDto.DetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();

        createRequest = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();

        detailResponse = new AccountDto.DetailResponse(
                1L,
                "test@example.com",
                "테스트계정",
                "ACCOUNT_ROLE_USER",
                "ACCOUNT_STATUS_ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("계정 생성 - 성공")
    void createAccount_Success() {
        // given
        given(accountRepository.existsByEmail(anyString())).willReturn(false);
        given(mapper.toEntity(any(AccountDto.CreateRequest.class))).willReturn(testAccount);
        given(accountRepository.save(any(Account.class))).willReturn(testAccount);
        given(mapper.toDetailResponse(any(Account.class))).willReturn(detailResponse);

        // when
        AccountDto.DetailResponse result = accountService.createAccount(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");

        then(accountRepository).should(times(1)).existsByEmail(anyString());
        then(accountRepository).should(times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("계정 생성 - 이메일 중복 실패")
    void createAccount_DuplicateEmail() {
        // given
        given(accountRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_EMAIL_CONFLICT);

        then(accountRepository).should(times(1)).existsByEmail(anyString());
        then(accountRepository).should(never()).save(any(Account.class));
    }

    @Test
    @DisplayName("계정 조회 - 성공")
    void getAccountByAccountId_Success() {
        // given
        given(accountRepository.findByAccountId(anyLong())).willReturn(Optional.of(testAccount));
        given(mapper.toDetailResponse(any(Account.class))).willReturn(detailResponse);

        // when
        AccountDto.DetailResponse result = accountService.getAccountByAccountId(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accountId()).isEqualTo(1L);

        then(accountRepository).should(times(1)).findByAccountId(1L);
    }

    @Test
    @DisplayName("계정 조회 - 존재하지 않음")
    void getAccountByAccountId_NotFound() {
        // given
        given(accountRepository.findByAccountId(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.getAccountByAccountId(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);

        then(accountRepository).should(times(1)).findByAccountId(999L);
    }

    @Test
    @DisplayName("전체 계정 조회 - 성공")
    void getAllAccounts_Success() {
        // given
        List<Account> accounts = List.of(testAccount);
        List<AccountDto.SimpleResponse> simpleResponses = List.of(
                new AccountDto.SimpleResponse(1L, "test@example.com", "테스트계정", "ACCOUNT_STATUS_ACTIVE")
        );

        given(accountRepository.findAll()).willReturn(accounts);
        given(mapper.toSimpleResponseList(any())).willReturn(simpleResponses);

        // when
        List<AccountDto.SimpleResponse> result = accountService.getAccounts(null, null);

        // then
        assertThat(result).hasSize(1);
        then(accountRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("상태별 계정 조회 - 성공")
    void getAccountsByStatus_Success() {
        // given
        List<Account> accounts = List.of(testAccount);
        List<AccountDto.SimpleResponse> simpleResponses = List.of(
                new AccountDto.SimpleResponse(1L, "test@example.com", "테스트계정", "ACCOUNT_STATUS_ACTIVE")
        );

        given(accountRepository.findAllByStatus(anyString())).willReturn(accounts);
        given(mapper.toSimpleResponseList(any())).willReturn(simpleResponses);

        // when
        List<AccountDto.SimpleResponse> result = accountService.getAccounts(
                AccountStatus.ACCOUNT_STATUS_ACTIVE, null);

        // then
        assertThat(result).hasSize(1);
        then(accountRepository).should(times(1)).findAllByStatus("ACCOUNT_STATUS_ACTIVE");
    }

    @Test
    @DisplayName("계정 수정 - 성공")
    void updateAccount_Success() {
        // given
        AccountDto.UpdateRequest updateRequest = AccountDto.UpdateRequest.builder()
                .accountName("새이름")
                .password("newPassword")
                .build();

        Account updatedAccount = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .password("newPassword")
                .accountName("새이름")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();

        given(accountRepository.findByAccountId(anyLong())).willReturn(Optional.of(testAccount));
        given(mapper.toDetailResponse(any(Account.class))).willReturn(detailResponse);

        // when
        AccountDto.DetailResponse result = accountService.updateAccount(1L, updateRequest);

        // then
        assertThat(result).isNotNull();
        // JPA Dirty Checking 사용 - 엔티티 조회만 검증
        then(accountRepository).should(times(1)).findByAccountId(1L);
        then(mapper).should(times(1)).toDetailResponse(any(Account.class));
    }

    @Test
    @DisplayName("계정 삭제 - 성공")
    void deleteAccount_Success() {
        // given - JpaRepository의 deleteById 사용

        // when
        accountService.deleteAccount(1L);

        // then
        then(accountRepository).should(times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("계정 활성화 - 성공")
    void activateAccount_Success() {
        // given
        given(accountRepository.findByAccountId(anyLong())).willReturn(Optional.of(testAccount));

        // when
        accountService.updateAccountStatus(1L, AccountStatus.ACCOUNT_STATUS_ACTIVE);

        // then
        // JPA Dirty Checking 사용 - 엔티티 조회만 검증
        then(accountRepository).should(times(1)).findByAccountId(1L);
    }

    @Test
    @DisplayName("계정 비활성화 - 성공")
    void deactivateAccount_Success() {
        // given
        given(accountRepository.findByAccountId(anyLong())).willReturn(Optional.of(testAccount));

        // when
        accountService.updateAccountStatus(1L, AccountStatus.ACCOUNT_STATUS_INACTIVE);

        // then
        // JPA Dirty Checking 사용 - 엔티티 조회만 검증
        then(accountRepository).should(times(1)).findByAccountId(1L);
    }

    @Test
    @DisplayName("이름으로 계정 검색 - 성공")
    void searchAccountsByName_Success() {
        // given
        List<Account> accounts = List.of(testAccount);
        List<AccountDto.SimpleResponse> simpleResponses = List.of(
                new AccountDto.SimpleResponse(1L, "test@example.com", "테스트계정", "ACCOUNT_STATUS_ACTIVE")
        );

        given(accountRepository.findByAccountNameContaining(anyString())).willReturn(accounts);
        given(mapper.toSimpleResponseList(any())).willReturn(simpleResponses);

        // when
        List<AccountDto.SimpleResponse> result = accountService.getAccounts(null, "테스트");

        // then
        assertThat(result).hasSize(1);
        then(accountRepository).should(times(1)).findByAccountNameContaining("테스트");
    }
}
