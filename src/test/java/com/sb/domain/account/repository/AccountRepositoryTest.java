package com.sb.domain.account.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb.domain.account.entity.Account;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Account Repository 단위 테스트
 *
 * <p>[@DataJpaTest] - JPA 관련 컴포넌트만 로드 (빠른 테스트) - H2 인메모리 DB 사용 - 트랜잭션 자동 롤백
 * <p>[@Import(TestConfig.class)] - QueryDSL 설정 임포트 (JPAQueryFactory 빈 등록)
 */
@DataJpaTest
@Import(AccountRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@DisplayName("AccountRepository 테스트")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();
    }

    @Test
    @DisplayName("계정 저장 - 성공")
    void save_Success() {
        // when
        Account saved = accountRepository.save(testAccount);

        // then
        assertThat(saved.getAccountId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 계정 조회 - 성공")
    void findByEmail_Success() {
        // given
        accountRepository.save(testAccount);

        // when
        Optional<Account> found = accountRepository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일로 계정 조회 - 없음")
    void findByEmail_NotFound() {
        // when
        Optional<Account> found = accountRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("ID로 계정 조회 - 성공")
    void findByAccountId_Success() {
        // given
        Account saved = accountRepository.save(testAccount);

        // when
        Optional<Account> found = accountRepository.findByAccountId(saved.getAccountId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAccountId()).isEqualTo(saved.getAccountId());
    }

    @Test
    @DisplayName("상태별 계정 조회 - 성공")
    void findAllByStatus_Success() {
        // given
        accountRepository.save(testAccount);

        Account inactiveAccount = Account.builder()
                .email("inactive@example.com")
                .password("password123")
                .accountName("비활성계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_INACTIVE")
                .build();
        accountRepository.save(inactiveAccount);

        // when
        List<Account> activeAccounts = accountRepository.findAllByStatus("ACCOUNT_STATUS_ACTIVE");

        // then
        assertThat(activeAccounts).hasSize(1);
        assertThat(activeAccounts.get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재함")
    void existsByEmail_True() {
        // given
        accountRepository.save(testAccount);

        // when
        boolean exists = accountRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않음")
    void existsByEmail_False() {
        // when
        boolean exists = accountRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("계정 이름으로 검색 - 성공")
    void findByAccountNameContaining_Success() {
        // given
        accountRepository.save(testAccount);

        Account anotherAccount = Account.builder()
                .email("another@example.com")
                .password("password123")
                .accountName("홍길동")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();
        accountRepository.save(anotherAccount);

        // when
        List<Account> results = accountRepository.findByAccountNameContaining("테스트");

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAccountName()).contains("테스트");
    }

    @Test
    @DisplayName("계정 이름 업데이트 - QueryDSL")
    void updateAccountNameByAccountId_Success() {
        // given
        Account saved = accountRepository.save(testAccount);

        // when
        long updatedCount = accountRepository.updateAccountNameByAccountId(
                saved.getAccountId(), "새이름");

        // then
        assertThat(updatedCount).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        Account updated = accountRepository.findByAccountId(saved.getAccountId()).get();
        assertThat(updated.getAccountName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("비밀번호 업데이트 - QueryDSL")
    void updatePasswordByAccountId_Success() {
        // given
        Account saved = accountRepository.save(testAccount);

        // when
        long updatedCount = accountRepository.updatePasswordByAccountId(
                saved.getAccountId(), "newPassword456");

        // then
        assertThat(updatedCount).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        Account updated = accountRepository.findByAccountId(saved.getAccountId()).get();
        assertThat(updated.getPassword()).isEqualTo("newPassword456");
    }

    @Test
    @DisplayName("계정 활성화 - QueryDSL")
    void activateByAccountId_Success() {
        // given
        Account inactiveAccount = Account.builder()
                .email("inactive@example.com")
                .password("password123")
                .accountName("비활성계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_INACTIVE")
                .build();
        Account saved = accountRepository.save(inactiveAccount);

        // when
        long updatedCount = accountRepository.activateByAccountId(saved.getAccountId());

        // then
        assertThat(updatedCount).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        Account activated = accountRepository.findByAccountId(saved.getAccountId()).get();
        assertThat(activated.getStatus()).isEqualTo("ACCOUNT_STATUS_ACTIVE");
    }

    @Test
    @DisplayName("계정 비활성화 - QueryDSL")
    void deactivateByAccountId_Success() {
        // given
        Account saved = accountRepository.save(testAccount);

        // when
        long updatedCount = accountRepository.deactivateByAccountId(saved.getAccountId());

        // then
        assertThat(updatedCount).isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        Account deactivated = accountRepository.findByAccountId(saved.getAccountId()).get();
        assertThat(deactivated.getStatus()).isEqualTo("ACCOUNT_STATUS_INACTIVE");
    }

    @Test
    @DisplayName("계정 삭제 - QueryDSL")
    void deleteAccountByAccountId_Success() {
        // given
        Account saved = accountRepository.save(testAccount);

        // when
        long deletedCount = accountRepository.deleteAccountByAccountId(saved.getAccountId());

        // then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(accountRepository.findByAccountId(saved.getAccountId())).isEmpty();
    }

    /**
     * QueryDSL 테스트용 설정 및 JPA Auditing 활성화
     */
    @org.springframework.boot.test.context.TestConfiguration
    @org.springframework.data.jpa.repository.config.EnableJpaAuditing
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public com.querydsl.jpa.impl.JPAQueryFactory jpaQueryFactory(
                jakarta.persistence.EntityManager entityManager) {
            return new com.querydsl.jpa.impl.JPAQueryFactory(entityManager);
        }
    }
}
