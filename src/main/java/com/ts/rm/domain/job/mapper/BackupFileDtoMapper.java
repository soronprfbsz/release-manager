package com.ts.rm.domain.job.mapper;

import com.ts.rm.domain.job.dto.BackupFileDto;
import com.ts.rm.domain.job.entity.BackupFile;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * BackupFile DTO Mapper
 */
@Mapper(componentModel = "spring")
public interface BackupFileDtoMapper {

    /**
     * Entity → DetailResponse
     */
    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "createdByName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "isDeletedCreator", expression = "java(backupFile.getCreator() == null)")
    BackupFileDto.DetailResponse toDetailResponse(BackupFile backupFile);

    /**
     * Entity → ListResponse (rowNumber는 별도 설정 필요)
     */
    @Mapping(target = "rowNumber", ignore = true)
    @Mapping(target = "fileSizeFormatted", source = "fileSize", qualifiedByName = "formatFileSize")
    @Mapping(target = "createdByEmail", source = "createdByEmail")
    @Mapping(target = "createdByName", source = "createdByName")
    @Mapping(target = "createdByAvatarStyle", source = "creator.avatarStyle")
    @Mapping(target = "createdByAvatarSeed", source = "creator.avatarSeed")
    @Mapping(target = "isDeletedCreator", expression = "java(backupFile.getCreator() == null)")
    BackupFileDto.ListResponse toListResponse(BackupFile backupFile);

    /**
     * Entity List → ListResponse List
     */
    List<BackupFileDto.ListResponse> toListResponseList(List<BackupFile> backupFiles);

    /**
     * 파일 크기를 포맷팅된 문자열로 변환
     */
    @Named("formatFileSize")
    default String formatFileSize(Long bytes) {
        return BackupFileDto.formatFileSize(bytes);
    }
}
