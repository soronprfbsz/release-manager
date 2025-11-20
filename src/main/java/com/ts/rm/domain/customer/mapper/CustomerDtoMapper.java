package com.ts.rm.domain.customer.mapper;

import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.entity.Customer;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Customer Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface CustomerDtoMapper {

    @Mapping(target = "customerId", ignore = true)
    Customer toEntity(CustomerDto.CreateRequest request);

    CustomerDto.DetailResponse toDetailResponse(Customer customer);

    List<CustomerDto.DetailResponse> toDetailResponseList(List<Customer> customers);

    CustomerDto.SimpleResponse toSimpleResponse(Customer customer);

    List<CustomerDto.SimpleResponse> toSimpleResponseList(List<Customer> customers);
}
