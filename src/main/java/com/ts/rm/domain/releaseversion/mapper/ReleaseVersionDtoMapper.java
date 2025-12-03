package com.ts.rm.domain.releaseversion.mapper;

import com.ts.rm.domain.releasefile.mapper.ReleaseFileDtoMapper;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ReleaseVersion Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring", uses = {ReleaseFileDtoMapper.class})
public interface ReleaseVersionDtoMapper {

    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "patchFileCount", expression = "java(releaseVersion.getReleaseFiles() != null ? releaseVersion.getReleaseFiles().size() : 0)")
    @Mapping(target = "fileCategories", ignore = true)
    ReleaseVersionDto.SimpleResponse toSimpleResponse(ReleaseVersion releaseVersion);

    List<ReleaseVersionDto.SimpleResponse> toSimpleResponseList(
            List<ReleaseVersion> releaseVersions);

    @Mapping(target = "releaseType", source = "releaseType")
    @Mapping(target = "customerCode", source = "customer.customerCode")
    @Mapping(target = "releaseFiles", source = "releaseFiles")
    ReleaseVersionDto.DetailResponse toDetailResponse(ReleaseVersion releaseVersion);

    List<ReleaseVersionDto.DetailResponse> toDetailResponseList(
            List<ReleaseVersion> releaseVersions);
}
