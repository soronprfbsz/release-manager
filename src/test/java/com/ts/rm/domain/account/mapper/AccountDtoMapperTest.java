package com.ts.rm.domain.account.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.entity.Account;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Account Mapper 단위 테스트
 *
 * <p>MapStruct 변환 로직 검증 - CreateRequest → Entity - Entity → DetailResponse - Entity →
 * SimpleResponse
 */
@SpringBootTest(classes = {AccountDtoMapperImpl.class})
@DisplayName("AccountDtoMapper 테스트")
class AccountDtoMapperTest {

    @Autowired
    private AccountDtoMapper mapper;

    @Test
    @DisplayName("CreateRequest → Entity 변환 성공")
    void toEntity_Success() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();

        // when
        Account entity = mapper.toEntity(request);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getPassword()).isEqualTo("password123");
        assertThat(entity.getAccountName()).isEqualTo("테스트계정");
        assertThat(entity.getRole()).isEqualTo("ACCOUNT_ROLE_USER");
        assertThat(entity.getStatus()).isEqualTo("ACCOUNT_STATUS_ACTIVE");

        // 자동 생성 필드는 null이어야 함
        assertThat(entity.getAccountId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("CreateRequest → Entity 변환 - 기본값 적용")
    void toEntity_WithDefaults() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .build();

        // when
        Account entity = mapper.toEntity(request);

        // then
        assertThat(entity.getRole()).isEqualTo("ACCOUNT_ROLE_USER");
        assertThat(entity.getStatus()).isEqualTo("ACCOUNT_STATUS_ACTIVE");
    }

    @Test
    @DisplayName("Entity → DetailResponse 변환 성공")
    void toDetailResponse_Success() {
        // given
        Account account = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();

        // when
        AccountDto.DetailResponse response = mapper.toDetailResponse(account);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.accountName()).isEqualTo("테스트계정");
        assertThat(response.role()).isEqualTo("ACCOUNT_ROLE_USER");
        assertThat(response.status()).isEqualTo("ACCOUNT_STATUS_ACTIVE");
    }

    @Test
    @DisplayName("Entity List → DetailResponse List 변환 성공")
    void toDetailResponseList_Success() {
        // given
        List<Account> accounts = List.of(
                Account.builder()
                        .accountId(1L)
                        .email("test1@example.com")
                        .password("password123")
                        .accountName("계정1")
                        .role("ACCOUNT_ROLE_USER")
                        .status("ACCOUNT_STATUS_ACTIVE")
                        .build(),
                Account.builder()
                        .accountId(2L)
                        .email("test2@example.com")
                        .password("password456")
                        .accountName("계정2")
                        .role("ACCOUNT_ROLE_ADMIN")
                        .status("ACCOUNT_STATUS_INACTIVE")
                        .build()
        );

        // when
        List<AccountDto.DetailResponse> responses = mapper.toDetailResponseList(accounts);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).accountId()).isEqualTo(1L);
        assertThat(responses.get(1).accountId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Entity → SimpleResponse 변환 성공")
    void toSimpleResponse_Success() {
        // given
        Account account = Account.builder()
                .accountId(1L)
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();

        // when
        AccountDto.SimpleResponse response = mapper.toSimpleResponse(account);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accountId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.accountName()).isEqualTo("테스트계정");
        assertThat(response.status()).isEqualTo("ACCOUNT_STATUS_ACTIVE");
    }

    @Test
    @DisplayName("Entity List → SimpleResponse List 변환 성공")
    void toSimpleResponseList_Success() {
        // given
        List<Account> accounts = List.of(
                Account.builder()
                        .accountId(1L)
                        .email("test1@example.com")
                        .password("password123")
                        .accountName("계정1")
                        .role("ACCOUNT_ROLE_USER")
                        .status("ACCOUNT_STATUS_ACTIVE")
                        .build(),
                Account.builder()
                        .accountId(2L)
                        .email("test2@example.com")
                        .password("password456")
                        .accountName("계정2")
                        .role("ACCOUNT_ROLE_ADMIN")
                        .status("ACCOUNT_STATUS_INACTIVE")
                        .build()
        );

        // when
        List<AccountDto.SimpleResponse> responses = mapper.toSimpleResponseList(accounts);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).accountId()).isEqualTo(1L);
        assertThat(responses.get(0).email()).isEqualTo("test1@example.com");
        assertThat(responses.get(1).accountId()).isEqualTo(2L);
        assertThat(responses.get(1).email()).isEqualTo("test2@example.com");
    }
}
