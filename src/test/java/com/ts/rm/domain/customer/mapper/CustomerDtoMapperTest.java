package com.ts.rm.domain.customer.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.entity.Customer;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Customer Mapper 단위 테스트
 */
@SpringBootTest(classes = {CustomerDtoMapperImpl.class})
@DisplayName("CustomerDtoMapper 테스트")
class CustomerDtoMapperTest {

    @Autowired
    private CustomerDtoMapper mapper;

    @Test
    @DisplayName("CreateRequest → Entity 변환 성공")
    void toEntity_Success() {
        // given
        CustomerDto.CreateRequest request = CustomerDto.CreateRequest.builder()
                .customerCode("company_a")
                .customerName("A회사")
                .description("A회사 설명")
                .isActive(true)
                .build();

        // when
        Customer entity = mapper.toEntity(request);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getCustomerCode()).isEqualTo("company_a");
        assertThat(entity.getCustomerName()).isEqualTo("A회사");
        assertThat(entity.getDescription()).isEqualTo("A회사 설명");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCustomerId()).isNull();
    }

    @Test
    @DisplayName("CreateRequest → Entity 변환 - 기본값 적용")
    void toEntity_WithDefaults() {
        // given
        CustomerDto.CreateRequest request = CustomerDto.CreateRequest.builder()
                .customerCode("company_a")
                .customerName("A회사")
                .build();

        // when
        Customer entity = mapper.toEntity(request);

        // then
        assertThat(entity.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Entity → DetailResponse 변환 성공")
    void toDetailResponse_Success() {
        // given
        Customer customer = Customer.builder()
                .customerId(1L)
                .customerCode("company_a")
                .customerName("A회사")
                .description("A회사 설명")
                .isActive(true)
                .build();

        // when
        CustomerDto.DetailResponse response = mapper.toDetailResponse(customer);

        // then
        assertThat(response).isNotNull();
        assertThat(response.customerId()).isEqualTo(1L);
        assertThat(response.customerCode()).isEqualTo("company_a");
        assertThat(response.customerName()).isEqualTo("A회사");
        assertThat(response.description()).isEqualTo("A회사 설명");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("Entity List → DetailResponse List 변환 성공")
    void toDetailResponseList_Success() {
        // given
        List<Customer> customers = List.of(
                Customer.builder()
                        .customerId(1L)
                        .customerCode("company_a")
                        .customerName("A회사")
                        .description("설명A")
                        .isActive(true)
                        .build(),
                Customer.builder()
                        .customerId(2L)
                        .customerCode("company_b")
                        .customerName("B회사")
                        .description("설명B")
                        .isActive(false)
                        .build()
        );

        // when
        List<CustomerDto.DetailResponse> responses = mapper.toDetailResponseList(customers);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).customerId()).isEqualTo(1L);
        assertThat(responses.get(1).customerId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Entity → SimpleResponse 변환 성공")
    void toSimpleResponse_Success() {
        // given
        Customer customer = Customer.builder()
                .customerId(1L)
                .customerCode("company_a")
                .customerName("A회사")
                .description("설명")
                .isActive(true)
                .build();

        // when
        CustomerDto.SimpleResponse response = mapper.toSimpleResponse(customer);

        // then
        assertThat(response).isNotNull();
        assertThat(response.customerId()).isEqualTo(1L);
        assertThat(response.customerCode()).isEqualTo("company_a");
        assertThat(response.customerName()).isEqualTo("A회사");
        assertThat(response.isActive()).isTrue();
    }
}
