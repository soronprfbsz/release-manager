package com.ts.rm.domain.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.service.CustomerService;
import com.ts.rm.global.config.MessageConfig;
import com.ts.rm.global.exception.GlobalExceptionHandler;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import com.ts.rm.domain.common.service.CustomUserDetailsService;
import com.ts.rm.global.filter.JwtAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
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
 * Customer Controller 통합 테스트
 */
@WebMvcTest(controllers = CustomerController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, MessageConfig.class})
@ActiveProfiles("test")
@DisplayName("CustomerController 테스트")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

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

    private CustomerDto.DetailResponse detailResponse;
    private CustomerDto.ListResponse listResponse;
    private CustomerDto.SimpleResponse simpleResponse;

    private MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @BeforeEach
    void setUp() {
        // SecurityContextHolder 모킹 설정
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        UserDetails userDetails = User.builder()
                .username("admin@tscientific")
                .password("password")
                .roles("USER")
                .build();

        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authentication.isAuthenticated()).willReturn(true);
        given(securityContext.getAuthentication()).willReturn(authentication);

        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        LocalDateTime now = LocalDateTime.now();

        detailResponse = new CustomerDto.DetailResponse(
                1L,
                "company_a",
                "A회사",
                "A회사 설명",
                true,
                null,
                now,
                "admin@tscientific",
                now,
                "admin@tscientific"
        );

        listResponse = new CustomerDto.ListResponse(
                1L,
                1L,
                "company_a",
                "A회사",
                "A회사 설명",
                true,
                null,
                now
        );

        simpleResponse = new CustomerDto.SimpleResponse(
                1L,
                "company_a",
                "A회사",
                true
        );
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
    }

    @Test
    @DisplayName("고객사 생성 - 성공")
    void createCustomer_Success() throws Exception {
        // given
        CustomerDto.CreateRequest request = CustomerDto.CreateRequest.builder()
                .customerCode("company_a")
                .customerName("A회사")
                .description("A회사 설명")
                .isActive(true)
                .build();

        given(customerService.createCustomer(any(), eq("admin@tscientific"))).willReturn(detailResponse);

        // when & then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.customerCode").value("company_a"))
                .andExpect(jsonPath("$.data.customerName").value("A회사"));
    }

    @Test
    @DisplayName("고객사 조회 (ID) - 성공")
    void getCustomerById_Success() throws Exception {
        // given
        given(customerService.getCustomerById(1L)).willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/api/customers/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.customerCode").value("company_a"));
    }

    @Test
    @DisplayName("활성 고객사 목록 조회 - 성공")
    void getActiveCustomers_Success() throws Exception {
        // given
        Page<CustomerDto.ListResponse> page = new PageImpl<>(List.of(listResponse));
        given(customerService.getCustomersWithPaging(eq(true), isNull(), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/customers").param("isActive", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].customerCode").value("company_a"));
    }

    @Test
    @DisplayName("전체 고객사 목록 조회 - 성공")
    void getAllCustomers_Success() throws Exception {
        // given
        Page<CustomerDto.ListResponse> page = new PageImpl<>(List.of(listResponse));
        given(customerService.getCustomersWithPaging(isNull(), isNull(), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].customerId").value(1));
    }

    @Test
    @DisplayName("고객사 정보 수정 - 성공")
    void updateCustomer_Success() throws Exception {
        // given
        CustomerDto.UpdateRequest request = CustomerDto.UpdateRequest.builder()
                .customerName("수정된회사")
                .description("수정된설명")
                .build();

        CustomerDto.DetailResponse updatedResponse = new CustomerDto.DetailResponse(
                1L,
                "company_a",
                "수정된회사",
                "수정된설명",
                true,
                null,
                detailResponse.createdAt(),
                "admin@tscientific",
                LocalDateTime.now(),
                "admin@tscientific"
        );

        given(customerService.updateCustomer(eq(1L), any(), eq("admin@tscientific"))).willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/customers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.customerName").value("수정된회사"));
    }

    @Test
    @DisplayName("고객사 삭제 - 성공")
    void deleteCustomer_Success() throws Exception {
        // given
        willDoNothing().given(customerService).deleteCustomer(1L);

        // when & then
        mockMvc.perform(delete("/api/customers/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("고객사 활성화 - 성공")
    void activateCustomer_Success() throws Exception {
        // given
        willDoNothing().given(customerService).updateCustomerStatus(eq(1L), eq(true));

        // when & then
        mockMvc.perform(patch("/api/customers/{id}/status", 1L)
                        .param("isActive", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("고객사 비활성화 - 성공")
    void deactivateCustomer_Success() throws Exception {
        // given
        willDoNothing().given(customerService).updateCustomerStatus(eq(1L), eq(false));

        // when & then
        mockMvc.perform(patch("/api/customers/{id}/status", 1L)
                        .param("isActive", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("고객사 생성 - Validation 실패 (customerCode 누락)")
    void createCustomer_ValidationFail_MissingCustomerCode() throws Exception {
        // given
        CustomerDto.CreateRequest request = CustomerDto.CreateRequest.builder()
                .customerName("A회사")
                .description("설명")
                .isActive(true)
                .build();

        // when & then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("고객사 생성 - Validation 실패 (customerName 누락)")
    void createCustomer_ValidationFail_MissingCustomerName() throws Exception {
        // given
        CustomerDto.CreateRequest request = CustomerDto.CreateRequest.builder()
                .customerCode("company_a")
                .description("설명")
                .isActive(true)
                .build();

        // when & then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
