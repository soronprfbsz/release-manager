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

    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "createdByName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "isDeletedCreator", expression = "java(resourceFile.getCreator() == null)")
    ResourceFileDto.DetailResponse toDetailResponse(ResourceFile resourceFile);

    ResourceFileDto.SimpleResponse toSimpleResponse(ResourceFile resourceFile);

    List<ResourceFileDto.SimpleResponse> toSimpleResponseList(List<ResourceFile> resourceFiles);
}
