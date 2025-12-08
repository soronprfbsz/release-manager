package com.ts.rm.domain.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.service.AccountService;
import com.ts.rm.domain.common.service.CustomUserDetailsService;
import com.ts.rm.global.config.MessageConfig;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.exception.GlobalExceptionHandler;
import com.ts.rm.global.filter.JwtAuthenticationFilter;
import com.ts.rm.global.security.RoleValidator;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AccountController 테스트
 */
@WebMvcTest(controllers = AccountController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, MessageConfig.class})
@ActiveProfiles("test")
@DisplayName("AccountController 테스트")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    // Security 관련 MockitoBean 추가
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private AccountDto.SimpleResponse simpleResponse;
    private AccountDto.DetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        simpleResponse = new AccountDto.SimpleResponse(
                1L,
                "test@example.com",
                "테스트계정",
                "ACTIVE"
        );

        detailResponse = new AccountDto.DetailResponse(
                1L,
                "test@example.com",
                "테스트계정",
                "USER",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // ========================================
    // GET /api/accounts - 계정 목록 조회
    // ========================================

    @Test
    @DisplayName("계정 목록 조회 - 성공")
    void getAccounts_Success() throws Exception {
        // given
        List<AccountDto.SimpleResponse> accounts = List.of(simpleResponse);
        given(accountService.getAccounts(null, null)).willReturn(accounts);

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].accountId").value(1))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"));
    }

    @Test
    @DisplayName("계정 목록 조회 - 상태 필터링")
    void getAccounts_WithStatusFilter_Success() throws Exception {
        // given
        List<AccountDto.SimpleResponse> accounts = List.of(simpleResponse);
        given(accountService.getAccounts(eq(AccountStatus.ACTIVE), eq(null))).willReturn(accounts);

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("계정 목록 조회 - 키워드 검색")
    void getAccounts_WithKeyword_Success() throws Exception {
        // given
        List<AccountDto.SimpleResponse> accounts = List.of(simpleResponse);
        given(accountService.getAccounts(null, "테스트")).willReturn(accounts);

        // when & then
        mockMvc.perform(get("/api/accounts")
                        .param("keyword", "테스트")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ========================================
    // PUT /api/accounts/{accountId} - 계정 수정
    // ========================================

    @Test
    @DisplayName("계정 수정 - 성공 (ADMIN 권한)")
    void updateAccount_Success_WithAdminRole() throws Exception {
        // given
        AccountDto.AdminUpdateRequest request = AccountDto.AdminUpdateRequest.builder()
                .password("newPassword123")
                .role("ADMIN")
                .status("INACTIVE")
                .build();

        given(accountService.adminUpdateAccount(anyLong(), any(AccountDto.AdminUpdateRequest.class)))
                .willReturn(detailResponse);

        // RoleValidator.requireAdmin() mock
        try (MockedStatic<RoleValidator> roleValidatorMock = Mockito.mockStatic(RoleValidator.class)) {
            roleValidatorMock.when(RoleValidator::requireAdmin).then(invocation -> null);

            // when & then
            mockMvc.perform(put("/api/accounts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.data.accountId").value(1))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }
    }

    @Test
    @DisplayName("계정 수정 - 실패 (ADMIN 권한 없음)")
    void updateAccount_Fail_WithoutAdminRole() throws Exception {
        // given
        AccountDto.AdminUpdateRequest request = AccountDto.AdminUpdateRequest.builder()
                .password("newPassword123")
                .build();

        // RoleValidator.requireAdmin() mock - FORBIDDEN 예외 발생
        try (MockedStatic<RoleValidator> roleValidatorMock = Mockito.mockStatic(RoleValidator.class)) {
            roleValidatorMock.when(RoleValidator::requireAdmin)
                    .thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

            // when & then
            mockMvc.perform(put("/api/accounts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("계정 수정 - 실패 (존재하지 않는 계정)")
    void updateAccount_Fail_AccountNotFound() throws Exception {
        // given
        AccountDto.AdminUpdateRequest request = AccountDto.AdminUpdateRequest.builder()
                .password("newPassword123")
                .build();

        given(accountService.adminUpdateAccount(anyLong(), any(AccountDto.AdminUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        // RoleValidator.requireAdmin() mock
        try (MockedStatic<RoleValidator> roleValidatorMock = Mockito.mockStatic(RoleValidator.class)) {
            roleValidatorMock.when(RoleValidator::requireAdmin).then(invocation -> null);

            // when & then
            mockMvc.perform(put("/api/accounts/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("계정 수정 - 실패 (잘못된 입력값)")
    void updateAccount_Fail_InvalidInput() throws Exception {
        // given: 비밀번호가 8자 미만 (validation 실패)
        AccountDto.AdminUpdateRequest request = AccountDto.AdminUpdateRequest.builder()
                .password("short")
                .build();

        // RoleValidator.requireAdmin() mock
        try (MockedStatic<RoleValidator> roleValidatorMock = Mockito.mockStatic(RoleValidator.class)) {
            roleValidatorMock.when(RoleValidator::requireAdmin).then(invocation -> null);

            // when & then
            mockMvc.perform(put("/api/accounts/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    // ========================================
    // DELETE /api/accounts/{accountId} - 계정 삭제
    // ========================================

    @Test
    @DisplayName("계정 삭제 - 성공 (ADMIN 권한)")
    void deleteAccount_Success_WithAdminRole() throws Exception {
        // given
        willDoNothing().given(accountService).deleteAccount(anyLong());

        // RoleValidator.requireAdmin() mock
        try (MockedStatic<RoleValidator> roleValidatorMock = Mockito.mockStatic(RoleValidator.class)) {
            roleValidatorMock.when(RoleValidator::requireAdmin).then(invocation -> null);

            // when & then
            mockMvc.perform(delete("/api/accounts/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"));
        }
    }

    @Test
    @DisplayName("계정 삭제 - 실패 (ADMIN 권한 없음)")
    void deleteAccount_Fail_WithoutAdminRole() throws Exception {
        // RoleValidator.requireAdmin() mock - FORBIDDEN 예외 발생
        try (MockedStatic<RoleValidator> roleValidatorMock = Mockito.mockStatic(RoleValidator.class)) {
            roleValidatorMock.when(RoleValidator::requireAdmin)
                    .thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

            // when & then
            mockMvc.perform(delete("/api/accounts/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("계정 삭제 - 실패 (존재하지 않는 계정)")
    void deleteAccount_Fail_AccountNotFound() throws Exception {
        // given
        willThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND))
                .given(accountService).deleteAccount(anyLong());

        // RoleValidator.requireAdmin() mock
        try (MockedStatic<RoleValidator> roleValidatorMock = Mockito.mockStatic(RoleValidator.class)) {
            roleValidatorMock.when(RoleValidator::requireAdmin).then(invocation -> null);

            // when & then
            mockMvc.perform(delete("/api/accounts/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
}
