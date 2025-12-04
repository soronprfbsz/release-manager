package com.ts.rm.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BackupLog DTO
 *
 * <p>백업/복원 로그 파일 관련 DTO
 */
public final class BackupLogDto {

    private BackupLogDto() {
    }

    /**
     * 로그 파일 목록 응답
     */
    @Schema(description = "로그 파일 목록 응답")
    public record LogListResponse(
            @Schema(description = "백업 파일 ID", example = "1")
            Long backupFileId,

            @Schema(description = "백업 파일명", example = "backup_test_db_20251205_120000.sql")
            String backupFileName,

            @Schema(description = "관련 로그 파일 목록")
            List<LogFileInfo> logFiles
    ) {
    }

    /**
     * 개별 로그 파일 정보
     */
    @Schema(description = "로그 파일 정보")
    public record LogFileInfo(
            @Schema(description = "로그 파일명", example = "backup_mariadb_20251205_120000.log")
            String logFileName,

            @Schema(description = "로그 타입 (BACKUP: 백업 시 생성된 로그, RESTORE: 복원 시도 로그)",
                    example = "BACKUP")
            String logType,

            @Schema(description = "파일 크기 (bytes)", example = "2048")
            Long fileSize,

            @Schema(description = "파일 크기 (읽기 쉬운 형식)", example = "2.0 KB")
            String fileSizeFormatted,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Schema(description = "파일 수정일시", example = "2025-12-05 12:00:00")
            LocalDateTime lastModified
    ) {
    }

    /**
     * 바이트 크기를 읽기 쉬운 형식으로 변환
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
