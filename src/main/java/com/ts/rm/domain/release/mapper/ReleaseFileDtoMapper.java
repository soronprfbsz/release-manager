package com.ts.rm.domain.release.mapper;

import com.ts.rm.domain.release.dto.ReleaseFileDto;
import com.ts.rm.domain.release.entity.ReleaseFile;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ReleaseFile Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface ReleaseFileDtoMapper {

    @Mapping(target = "releaseVersion", source = "releaseVersion.version")
    @Mapping(target = "databaseTypeName", source = "databaseType")
    ReleaseFileDto.SimpleResponse toSimpleResponse(ReleaseFile releaseFile);

    List<ReleaseFileDto.SimpleResponse> toSimpleResponseList(List<ReleaseFile> releaseFiles);

    @Mapping(target = "releaseFileId", source = "releaseFileId")
    @Mapping(target = "releaseVersionId", source = "releaseVersion.releaseVersionId")
    @Mapping(target = "releaseVersion", source = "releaseVersion.version")
    @Mapping(target = "databaseTypeId", expression = "java(null)")
    @Mapping(target = "databaseTypeName", source = "databaseType")
    ReleaseFileDto.DetailResponse toDetailResponse(ReleaseFile releaseFile);

    List<ReleaseFileDto.DetailResponse> toDetailResponseList(List<ReleaseFile> releaseFiles);
}
