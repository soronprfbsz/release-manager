package com.ts.rm.domain.account.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Account DTO Validation 테스트
 *
 * <p>Bean Validation 애너테이션 동작 검증 - @NotBlank, @Email, @Size 등
 */
@DisplayName("AccountDto Validation 테스트")
class AccountDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("CreateRequest - 유효한 요청")
    void createRequest_Valid() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CreateRequest - 이메일 필수 검증")
    void createRequest_EmailRequired() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email(null)  // 이메일 없음
                .password("password123")
                .accountName("테스트계정")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("이메일");
    }

    @Test
    @DisplayName("CreateRequest - 이메일 형식 검증")
    void createRequest_EmailFormat() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("invalid-email")  // 잘못된 형식
                .password("password123")
                .accountName("테스트계정")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("이메일");
    }

    @Test
    @DisplayName("CreateRequest - 빈 이메일 검증")
    void createRequest_EmailBlank() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("")  // 빈 문자열
                .password("password123")
                .accountName("테스트계정")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("CreateRequest - 비밀번호 필수 검증")
    void createRequest_PasswordRequired() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password(null)  // 비밀번호 없음
                .accountName("테스트계정")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("비밀번호");
    }

    @Test
    @DisplayName("CreateRequest - 비밀번호 최소 길이 검증")
    void createRequest_PasswordMinLength() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("short")  // 8자 미만
                .accountName("테스트계정")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("8자 이상");
    }

    @Test
    @DisplayName("CreateRequest - 비밀번호 최대 길이 검증")
    void createRequest_PasswordMaxLength() {
        // given
        String longPassword = "a".repeat(101);  // 100자 초과
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password(longPassword)
                .accountName("테스트계정")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("100자 이하");
    }

    @Test
    @DisplayName("CreateRequest - 이름 필수 검증")
    void createRequest_AccountNameRequired() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName(null)  // 이름 없음
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("이름");
    }

    @Test
    @DisplayName("CreateRequest - 이름 최소 길이 검증")
    void createRequest_AccountNameMinLength() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("a")  // 2자 미만
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("2자 이상");
    }

    @Test
    @DisplayName("CreateRequest - 이름 최대 길이 검증")
    void createRequest_AccountNameMaxLength() {
        // given
        String longName = "가".repeat(51);  // 50자 초과
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName(longName)
                .build();

        // when
        Set<ConstraintViolation<AccountDto.CreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("50자 이하");
    }

    @Test
    @DisplayName("CreateRequest - 기본값 설정 확인 (role)")
    void createRequest_DefaultRole() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .build();

        // then
        assertThat(request.role()).isEqualTo("ACCOUNT_ROLE_USER");
    }

    @Test
    @DisplayName("CreateRequest - 기본값 설정 확인 (status)")
    void createRequest_DefaultStatus() {
        // given
        AccountDto.CreateRequest request = AccountDto.CreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .accountName("테스트계정")
                .build();

        // then
        assertThat(request.status()).isEqualTo("ACCOUNT_STATUS_ACTIVE");
    }

    @Test
    @DisplayName("UpdateRequest - 유효한 요청")
    void updateRequest_Valid() {
        // given
        AccountDto.UpdateRequest request = AccountDto.UpdateRequest.builder()
                .accountName("새이름")
                .password("newPassword123")
                .build();

        // when
        Set<ConstraintViolation<AccountDto.UpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateRequest - 모든 필드 null 허용")
    void updateRequest_AllFieldsNull() {
        // given
        AccountDto.UpdateRequest request = AccountDto.UpdateRequest.builder()
                .build();

        // when
        Set<ConstraintViolation<AccountDto.UpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UpdateRequest - 이름 길이 검증")
    void updateRequest_AccountNameLength() {
        // given
        AccountDto.UpdateRequest request = AccountDto.UpdateRequest.builder()
                .accountName("a")  // 2자 미만
                .build();

        // when
        Set<ConstraintViolation<AccountDto.UpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("2자 이상");
    }

    @Test
    @DisplayName("UpdateRequest - 비밀번호 길이 검증")
    void updateRequest_PasswordLength() {
        // given
        AccountDto.UpdateRequest request = AccountDto.UpdateRequest.builder()
                .password("short")  // 8자 미만
                .build();

        // when
        Set<ConstraintViolation<AccountDto.UpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("8자 이상");
    }
}
