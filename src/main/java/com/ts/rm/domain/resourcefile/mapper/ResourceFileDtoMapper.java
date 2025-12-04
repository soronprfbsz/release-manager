package com.ts.rm.domain.resourcefile.mapper;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * ResourceFile DTO Mapper
 */
@Mapper(componentModel = "spring")
public interface ResourceFileDtoMapper {

    ResourceFileDto.DetailResponse toDetailResponse(ResourceFile resourceFile);

    ResourceFileDto.SimpleResponse toSimpleResponse(ResourceFile resourceFile);

    List<ResourceFileDto.SimpleResponse> toSimpleResponseList(List<ResourceFile> resourceFiles);
}
