package com.ts.rm.domain.patch.mapper;

import com.ts.rm.domain.patch.dto.PatchDto;
import com.ts.rm.domain.patch.entity.Patch;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Patch Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface PatchDtoMapper {

    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "customerName", source = "customer.customerName")
    @Mapping(target = "createdByEmail", expression = "java(patch.getCreatedByName())")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "engineerId", source = "engineer.engineerId")
    @Mapping(target = "engineerName", source = "engineer.engineerName")
    PatchDto.SimpleResponse toSimpleResponse(Patch patch);

    List<PatchDto.SimpleResponse> toSimpleResponseList(List<Patch> patches);

    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "projectName", source = "project.projectName")
    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "customerName", source = "customer.customerName")
    @Mapping(target = "createdByEmail", expression = "java(patch.getCreatedByName())")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "engineerId", source = "engineer.engineerId")
    @Mapping(target = "engineerName", source = "engineer.engineerName")
    PatchDto.DetailResponse toDetailResponse(Patch patch);

    List<PatchDto.DetailResponse> toDetailResponseList(List<Patch> patches);

    @Mapping(target = "rowNumber", ignore = true)
    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "customerName", source = "customer.customerName")
    @Mapping(target = "createdByEmail", expression = "java(patch.getCreatedByName())")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "engineerId", source = "engineer.engineerId")
    @Mapping(target = "engineerName", source = "engineer.engineerName")
    PatchDto.ListResponse toListResponse(Patch patch);
}
