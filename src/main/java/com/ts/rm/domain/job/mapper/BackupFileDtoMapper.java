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
    BackupFileDto.DetailResponse toDetailResponse(BackupFile backupFile);

    /**
     * Entity → ListResponse (rowNumber는 별도 설정 필요)
     */
    @Mapping(target = "rowNumber", ignore = true)
    @Mapping(target = "fileSizeFormatted", source = "fileSize", qualifiedByName = "formatFileSize")
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
