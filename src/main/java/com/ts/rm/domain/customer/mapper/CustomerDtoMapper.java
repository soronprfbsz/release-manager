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
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "updater", ignore = true)
    Customer toEntity(CustomerDto.CreateRequest request);

    @Mapping(target = "project", ignore = true)
    @Mapping(target = "hasCustomVersion", ignore = true)
    @Mapping(target = "createdByEmail", expression = "java(customer.getCreatedByName())")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "updatedBy", expression = "java(customer.getUpdatedByName())")
    @Mapping(target = "updatedByAvatarStyle", source = "updater.avatarStyle")
    @Mapping(target = "updatedByAvatarSeed", source = "updater.avatarSeed")
    CustomerDto.DetailResponse toDetailResponse(Customer customer);

    List<CustomerDto.DetailResponse> toDetailResponseList(List<Customer> customers);

    CustomerDto.SimpleResponse toSimpleResponse(Customer customer);

    List<CustomerDto.SimpleResponse> toSimpleResponseList(List<Customer> customers);

    @Mapping(target = "rowNumber", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "hasCustomVersion", ignore = true)
    CustomerDto.ListResponse toListResponse(Customer customer);
}
