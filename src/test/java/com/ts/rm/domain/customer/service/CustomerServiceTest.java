package com.ts.rm.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.mapper.CustomerDtoMapper;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
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
 * Customer Service 단위 테스트
 *
 * <p>TDD 방식으로 테스트를 먼저 작성하고 구현
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService 테스트")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerDtoMapper mapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CustomerDto.CreateRequest createRequest;
    private CustomerDto.DetailResponse detailResponse;
    private CustomerDto.SimpleResponse simpleResponse;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .customerId(1L)
                .customerCode("company_a")
                .customerName("A회사")
                .description("A회사 설명")
                .isActive(true)
                .build();

        createRequest = CustomerDto.CreateRequest.builder()
                .customerCode("company_a")
                .customerName("A회사")
                .description("A회사 설명")
                .isActive(true)
                .build();

        detailResponse = new CustomerDto.DetailResponse(
                1L,
                "company_a",
                "A회사",
                "A회사 설명",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
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
    void createCustomer_Success() {
        // given
        given(customerRepository.existsByCustomerCode(anyString())).willReturn(false);
        given(mapper.toEntity(any(CustomerDto.CreateRequest.class))).willReturn(testCustomer);
        given(customerRepository.save(any(Customer.class))).willReturn(testCustomer);
        given(mapper.toDetailResponse(any(Customer.class))).willReturn(detailResponse);

        // when
        CustomerDto.DetailResponse result = customerService.createCustomer(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.customerCode()).isEqualTo("company_a");
        assertThat(result.customerName()).isEqualTo("A회사");

        then(customerRepository).should(times(1)).existsByCustomerCode(anyString());
        then(customerRepository).should(times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("고객사 생성 - 중복 코드로 실패")
    void createCustomer_DuplicateCode() {
        // given
        given(customerRepository.existsByCustomerCode(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> customerService.createCustomer(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CUSTOMER_CODE_CONFLICT);

        then(customerRepository).should(times(1)).existsByCustomerCode(anyString());
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("고객사 조회 (ID) - 성공")
    void getCustomerById_Success() {
        // given
        given(customerRepository.findById(anyLong())).willReturn(Optional.of(testCustomer));
        given(mapper.toDetailResponse(any(Customer.class))).willReturn(detailResponse);

        // when
        CustomerDto.DetailResponse result = customerService.getCustomerById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.customerId()).isEqualTo(1L);

        then(customerRepository).should(times(1)).findById(1L);
    }

    @Test
    @DisplayName("고객사 조회 (ID) - 존재하지 않음")
    void getCustomerById_NotFound() {
        // given
        given(customerRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customerService.getCustomerById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CUSTOMER_NOT_FOUND);

        then(customerRepository).should(times(1)).findById(999L);
    }

    @Test
    @DisplayName("고객사 코드로 조회 - 성공")
    void getCustomerByCode_Success() {
        // given
        given(customerRepository.findByCustomerCode(anyString())).willReturn(
                Optional.of(testCustomer));
        given(mapper.toDetailResponse(any(Customer.class))).willReturn(detailResponse);

        // when
        CustomerDto.DetailResponse result = customerService.getCustomerByCode("company_a");

        // then
        assertThat(result).isNotNull();
        assertThat(result.customerCode()).isEqualTo("company_a");

        then(customerRepository).should(times(1)).findByCustomerCode("company_a");
    }

    @Test
    @DisplayName("활성 고객사 목록 조회 - 성공")
    void getActiveCustomers_Success() {
        // given
        List<Customer> customers = List.of(testCustomer);
        List<CustomerDto.DetailResponse> responses = List.of(detailResponse);

        given(customerRepository.findAllByIsActive(true)).willReturn(customers);
        given(mapper.toDetailResponseList(any())).willReturn(responses);

        // when
        List<CustomerDto.DetailResponse> result = customerService.getCustomers(true, null);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).customerCode()).isEqualTo("company_a");

        then(customerRepository).should(times(1)).findAllByIsActive(true);
    }

    @Test
    @DisplayName("전체 고객사 목록 조회 - 성공")
    void getAllCustomers_Success() {
        // given
        List<Customer> customers = List.of(testCustomer);
        List<CustomerDto.DetailResponse> responses = List.of(detailResponse);

        given(customerRepository.findAll()).willReturn(customers);
        given(mapper.toDetailResponseList(any())).willReturn(responses);

        // when
        List<CustomerDto.DetailResponse> result = customerService.getCustomers(null, null);

        // then
        assertThat(result).hasSize(1);

        then(customerRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("고객사 수정 - 성공")
    void updateCustomer_Success() {
        // given
        CustomerDto.UpdateRequest updateRequest = CustomerDto.UpdateRequest.builder()
                .customerName("새이름")
                .description("새설명")
                .isActive(true)
                .build();

        given(customerRepository.findById(anyLong())).willReturn(Optional.of(testCustomer));
        given(customerRepository.updateCustomerInfoByCustomerId(anyLong(), anyString(),
                anyString())).willReturn(1L);
        given(customerRepository.findById(anyLong())).willReturn(Optional.of(testCustomer));
        given(mapper.toDetailResponse(any(Customer.class))).willReturn(detailResponse);

        // when
        CustomerDto.DetailResponse result = customerService.updateCustomer(1L, updateRequest);

        // then
        assertThat(result).isNotNull();

        then(customerRepository).should(times(1))
                .updateCustomerInfoByCustomerId(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("고객사 활성화 - 성공")
    void activateCustomer_Success() {
        // given
        given(customerRepository.findById(anyLong())).willReturn(Optional.of(testCustomer));
        given(customerRepository.activateByCustomerId(anyLong())).willReturn(1L);

        // when
        customerService.updateCustomerStatus(1L, true);

        // then
        then(customerRepository).should(times(1)).activateByCustomerId(1L);
    }

    @Test
    @DisplayName("고객사 비활성화 - 성공")
    void deactivateCustomer_Success() {
        // given
        given(customerRepository.findById(anyLong())).willReturn(Optional.of(testCustomer));
        given(customerRepository.deactivateByCustomerId(anyLong())).willReturn(1L);

        // when
        customerService.updateCustomerStatus(1L, false);

        // then
        then(customerRepository).should(times(1)).deactivateByCustomerId(1L);
    }

    @Test
    @DisplayName("고객사 삭제 - 성공")
    void deleteCustomer_Success() {
        // given
        given(customerRepository.findById(anyLong())).willReturn(Optional.of(testCustomer));

        // when
        customerService.deleteCustomer(1L);

        // then
        then(customerRepository).should(times(1)).delete(any(Customer.class));
    }

    @Test
    @DisplayName("고객사명으로 검색 - 성공")
    void searchCustomersByName_Success() {
        // given
        List<Customer> customers = List.of(testCustomer);
        List<CustomerDto.DetailResponse> responses = List.of(detailResponse);

        given(customerRepository.findByCustomerNameContaining(anyString())).willReturn(customers);
        given(mapper.toDetailResponseList(any())).willReturn(responses);

        // when
        List<CustomerDto.DetailResponse> result = customerService.getCustomers(null, "A회사");

        // then
        assertThat(result).hasSize(1);

        then(customerRepository).should(times(1)).findByCustomerNameContaining("A회사");
    }
}
