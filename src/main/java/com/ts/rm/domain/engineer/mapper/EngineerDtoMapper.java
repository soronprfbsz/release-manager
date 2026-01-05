package com.ts.rm.domain.engineer.mapper;

import com.ts.rm.domain.engineer.dto.EngineerDto;
import com.ts.rm.domain.engineer.entity.Engineer;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Engineer Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 *
 * <p>position 필드 매핑:
 * - Engineer.position = code_id 값 (예: MANAGER)
 * - Response.positionCode = code_id 값
 * - Response.position = code_name 값 (Service에서 설정)
 */
@Mapper(componentModel = "spring")
public interface EngineerDtoMapper {

    @Mapping(target = "engineerId", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", source = "positionCode")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Engineer toEntity(EngineerDto.CreateRequest request);

    @Mapping(target = "positionCode", source = "position")
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "departmentId", source = "department.departmentId")
    @Mapping(target = "departmentName", source = "department.departmentName")
    EngineerDto.DetailResponse toDetailResponse(Engineer engineer);

    List<EngineerDto.DetailResponse> toDetailResponseList(List<Engineer> engineers);

    @Mapping(target = "positionCode", source = "position")
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "departmentName", source = "department.departmentName")
    EngineerDto.SimpleResponse toSimpleResponse(Engineer engineer);

    List<EngineerDto.SimpleResponse> toSimpleResponseList(List<Engineer> engineers);

    @Mapping(target = "rowNumber", ignore = true)
    @Mapping(target = "positionCode", source = "position")
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "departmentId", source = "department.departmentId")
    @Mapping(target = "departmentName", source = "department.departmentName")
    EngineerDto.ListResponse toListResponse(Engineer engineer);
}
