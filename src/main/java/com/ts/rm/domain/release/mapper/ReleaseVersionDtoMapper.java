package com.ts.rm.domain.release.mapper;

import com.ts.rm.domain.release.dto.ReleaseVersionDto;
import com.ts.rm.domain.release.entity.ReleaseVersion;
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
