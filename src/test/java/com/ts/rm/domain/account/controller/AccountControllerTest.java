package com.ts.rm.domain.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.domain.account.dto.AccountDto;
import com.ts.rm.domain.account.enums.AccountStatus;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.domain.account.service.AccountService;
import com.ts.rm.global.config.MessageConfig;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import com.ts.rm.global.common.exception.GlobalExceptionHandler;
import com.ts.rm.global.jwt.JwtTokenProvider;
import com.ts.rm.global.security.CustomUserDetailsService;
import com.ts.rm.global.security.JwtAuthenticationFilter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Account Controller 통합 테스트
 *
 * <p>[@WebMvcTest] - Controller 계층만 테스트 - Service는 MockitoBean으로 대체 - MockMvc로
 * HTTP 요청/응답 검증
 * <p>[@Import] - GlobalExceptionHandler와 MessageConfig 포함
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

    // Security 관련 MockBean 추가
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private AccountDto.CreateRequest createRequest;
    private AccountDto.DetailResponse detailResponse;
    private AccountDto.SimpleResponse simpleResponse;

    @BeforeEach
    void setUp() {
        createRequest = AccountDto.CreateRequest.builder()
                .email("test@example.com").password("password123")
                .accountName("테스트계정").role("ACCOUNT_ROLE_USER")
                .status("ACCOUNT_STATUS_ACTIVE").build();

        detailResponse = new AccountDto.DetailResponse(1L, "test@example.com",
                "테스트계정", "ACCOUNT_ROLE_USER", "ACCOUNT_STATUS_ACTIVE",
                LocalDateTime.now(), LocalDateTime.now());

        simpleResponse = new AccountDto.SimpleResponse(1L, "test@example.com",
                "테스트계정", "ACCOUNT_STATUS_ACTIVE");
    }

    @Test
    @DisplayName("POST /api/accounts - 계정 생성 성공")
    void createAccount_Success() throws Exception {
        // given
        given(accountService.createAccount(
                any(AccountDto.CreateRequest.class))).willReturn(
                detailResponse);

        // when & then
        mockMvc.perform(
                        post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(createRequest)))
                .andDo(print()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accountId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.accountName").value("테스트계정"));
    }

    @Test
    @DisplayName("POST /api/accounts - 계정 생성 실패 (유효성 검증)")
    void createAccount_ValidationFail() throws Exception {
        // given
        AccountDto.CreateRequest invalidRequest = AccountDto.CreateRequest.builder()
                .email("invalid-email")  // 잘못된 이메일 형식
                .password("short")       // 비밀번호 짧음
                .accountName("a")        // 이름 짧음
                .build();

        // when & then
        mockMvc.perform(
                        post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        invalidRequest))).andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/accounts - 계정 생성 실패 (이메일 중복)")
    void createAccount_DuplicateEmail() throws Exception {
        // given
        given(accountService.createAccount(
                any(AccountDto.CreateRequest.class))).willThrow(
                new BusinessException(ErrorCode.ACCOUNT_EMAIL_CONFLICT));

        // when & then
        mockMvc.perform(
                        post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(createRequest)))
                .andDo(print()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.data.code").value("A002"));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - 계정 조회 성공")
    void getAccount_Success() throws Exception {
        // given
        given(accountService.getAccountByAccountId(anyLong())).willReturn(
                detailResponse);

        // when & then
        mockMvc.perform(get("/api/accounts/1")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accountId").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/accounts/{id} - 계정 조회 실패 (존재하지 않음)")
    void getAccount_NotFound() throws Exception {
        // given
        given(accountService.getAccountByAccountId(anyLong())).willThrow(
                new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/accounts/999")).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.data.code").value("A001"));
    }

    @Test
    @DisplayName("GET /api/accounts - 전체 계정 조회 성공")
    void getAllAccounts_Success() throws Exception {
        // given
        given(accountService.getAccounts(isNull(), isNull())).willReturn(
                List.of(simpleResponse));

        // when & then
        mockMvc.perform(get("/api/accounts")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].accountId").value(1L));
    }

    @Test
    @DisplayName("GET /api/accounts?status=ACTIVE - 상태별 계정 조회 성공")
    void getAccountsByStatus_Success() throws Exception {
        // given
        given(accountService.getAccounts(eq(AccountStatus.ACCOUNT_STATUS_ACTIVE), isNull())).willReturn(
                List.of(simpleResponse));

        // when & then
        mockMvc.perform(get("/api/accounts").param("status",
                        AccountStatus.ACCOUNT_STATUS_ACTIVE.name())).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/accounts?keyword=테스트 - 계정 검색 성공")
    void searchAccounts_Success() throws Exception {
        // given
        given(accountService.getAccounts(isNull(), eq("테스트"))).willReturn(
                List.of(simpleResponse));

        // when & then
        mockMvc.perform(get("/api/accounts").param("keyword", "테스트"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].accountName").value("테스트계정"));
    }

    @Test
    @DisplayName("PUT /api/accounts/{id} - 계정 수정 성공")
    void updateAccount_Success() throws Exception {
        // given
        AccountDto.UpdateRequest updateRequest = AccountDto.UpdateRequest.builder()
                .accountName("새이름").password("newPassword123").build();

        given(accountService.updateAccount(anyLong(),
                any(AccountDto.UpdateRequest.class))).willReturn(
                detailResponse);

        // when & then
        mockMvc.perform(
                        put("/api/accounts/1").contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(updateRequest)))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.accountId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id} - 계정 삭제 성공")
    void deleteAccount_Success() throws Exception {
        // given
        willDoNothing().given(accountService).deleteAccount(anyLong());

        // when & then
        mockMvc.perform(delete("/api/accounts/1")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("DELETE /api/accounts/{id} - 계정 삭제 실패 (존재하지 않음)")
    void deleteAccount_NotFound() throws Exception {
        // given
        willThrow(new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)).given(
                accountService).deleteAccount(anyLong());

        // when & then
        mockMvc.perform(delete("/api/accounts/999")).andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.data.code").value("A001"));
    }

    @Test
    @DisplayName("PATCH /api/accounts/{id}/status - 계정 활성화 성공")
    void activateAccount_Success() throws Exception {
        // given
        willDoNothing().given(accountService).updateAccountStatus(eq(1L), eq(AccountStatus.ACCOUNT_STATUS_ACTIVE));

        // when & then
        mockMvc.perform(patch("/api/accounts/1/status")
                        .param("status", AccountStatus.ACCOUNT_STATUS_ACTIVE.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("PATCH /api/accounts/{id}/status - 계정 비활성화 성공")
    void deactivateAccount_Success() throws Exception {
        // given
        willDoNothing().given(accountService).updateAccountStatus(eq(1L), eq(AccountStatus.ACCOUNT_STATUS_INACTIVE));

        // when & then
        mockMvc.perform(patch("/api/accounts/1/status")
                        .param("status", AccountStatus.ACCOUNT_STATUS_INACTIVE.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}
