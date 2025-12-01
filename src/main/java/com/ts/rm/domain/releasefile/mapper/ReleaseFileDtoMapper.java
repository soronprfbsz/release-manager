package com.ts.rm.domain.releasefile.mapper;

import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ReleaseFile Entity ↔ DTO 변환 Mapper (MapStruct로 구현)
 */
@Mapper(componentModel = "spring")
public interface ReleaseFileDtoMapper {

    @Mapping(target = "releaseVersion", source = "releaseVersion.version")
    @Mapping(target = "fileCategory", expression = "java(releaseFile.getFileCategory() != null ? releaseFile.getFileCategory().getCode() : null)")
    ReleaseFileDto.SimpleResponse toSimpleResponse(ReleaseFile releaseFile);

    List<ReleaseFileDto.SimpleResponse> toSimpleResponseList(List<ReleaseFile> releaseFiles);

    @Mapping(target = "releaseFileId", source = "releaseFileId")
    @Mapping(target = "releaseVersionId", source = "releaseVersion.releaseVersionId")
    @Mapping(target = "releaseVersion", source = "releaseVersion.version")
    @Mapping(target = "fileCategory", expression = "java(releaseFile.getFileCategory() != null ? releaseFile.getFileCategory().getCode() : null)")
    ReleaseFileDto.DetailResponse toDetailResponse(ReleaseFile releaseFile);

    List<ReleaseFileDto.DetailResponse> toDetailResponseList(List<ReleaseFile> releaseFiles);
}
