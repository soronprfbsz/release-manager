package com.ts.rm.domain.engineer.mapper;

import com.ts.rm.domain.engineer.dto.EngineerDto;
import com.ts.rm.domain.engineer.entity.Engineer;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Engineer Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface EngineerDtoMapper {

    @Mapping(target = "engineerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Engineer toEntity(EngineerDto.CreateRequest request);

    EngineerDto.DetailResponse toDetailResponse(Engineer engineer);

    List<EngineerDto.DetailResponse> toDetailResponseList(List<Engineer> engineers);

    EngineerDto.SimpleResponse toSimpleResponse(Engineer engineer);

    List<EngineerDto.SimpleResponse> toSimpleResponseList(List<Engineer> engineers);
}
