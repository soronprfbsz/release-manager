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
import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.service.CustomerService;
import com.ts.rm.global.config.MessageConfig;
import com.ts.rm.global.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Customer Controller 통합 테스트
 */
@WebMvcTest(CustomerController.class)
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

    private CustomerDto.DetailResponse detailResponse;
    private CustomerDto.SimpleResponse simpleResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        detailResponse = new CustomerDto.DetailResponse(
                1L,
                "company_a",
                "A회사",
                "A회사 설명",
                true,
                now,
                now
        );

        simpleResponse = new CustomerDto.SimpleResponse(
                1L,
                "company_a",
                "A회사",
                true
        );
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

        given(customerService.createCustomer(any())).willReturn(detailResponse);

        // when & then
        mockMvc.perform(post("/api/v1/customers")
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
        mockMvc.perform(get("/api/v1/customers/{id}", 1L))
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
        List<CustomerDto.DetailResponse> responses = List.of(detailResponse);
        given(customerService.getCustomers(eq(true), isNull())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/customers").param("isActive", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].customerCode").value("company_a"));
    }

    @Test
    @DisplayName("전체 고객사 목록 조회 - 성공")
    void getAllCustomers_Success() throws Exception {
        // given
        List<CustomerDto.DetailResponse> responses = List.of(detailResponse);
        given(customerService.getCustomers(isNull(), isNull())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/customers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].customerId").value(1));
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
                detailResponse.createdAt(),
                LocalDateTime.now()
        );

        given(customerService.updateCustomer(eq(1L), any())).willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/v1/customers/{id}", 1L)
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
        mockMvc.perform(delete("/api/v1/customers/{id}", 1L))
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
        mockMvc.perform(patch("/api/v1/customers/{id}/status", 1L)
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
        mockMvc.perform(patch("/api/v1/customers/{id}/status", 1L)
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
        mockMvc.perform(post("/api/v1/customers")
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
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
