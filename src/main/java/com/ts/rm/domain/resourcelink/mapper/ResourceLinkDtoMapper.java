package com.ts.rm.domain.resourcelink.mapper;

import com.ts.rm.domain.resourcelink.dto.ResourceLinkDto;
import com.ts.rm.domain.resourcelink.entity.ResourceLink;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ResourceLink DTO Mapper
 */
@Mapper(componentModel = "spring")
public interface ResourceLinkDtoMapper {

    /**
     * Entity → DetailResponse
     */
    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "createdByName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "isDeletedCreator", expression = "java(resourceLink.getCreator() == null)")
    ResourceLinkDto.DetailResponse toDetailResponse(ResourceLink resourceLink);

    /**
     * Entity → SimpleResponse
     */
    ResourceLinkDto.SimpleResponse toSimpleResponse(ResourceLink resourceLink);

    /**
     * Entity List → SimpleResponse List
     */
    List<ResourceLinkDto.SimpleResponse> toSimpleResponseList(List<ResourceLink> resourceLinks);
}
