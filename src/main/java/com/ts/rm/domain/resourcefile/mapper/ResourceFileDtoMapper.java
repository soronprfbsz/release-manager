package com.ts.rm.domain.resourcefile.mapper;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ResourceFile DTO Mapper
 */
@Mapper(componentModel = "spring")
public interface ResourceFileDtoMapper {

    @Mapping(target = "createdByEmail", expression = "java(resourceFile.getCreatedByEmail())")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    ResourceFileDto.DetailResponse toDetailResponse(ResourceFile resourceFile);

    ResourceFileDto.SimpleResponse toSimpleResponse(ResourceFile resourceFile);

    List<ResourceFileDto.SimpleResponse> toSimpleResponseList(List<ResourceFile> resourceFiles);
}
