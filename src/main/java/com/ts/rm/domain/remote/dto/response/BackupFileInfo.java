package com.ts.rm.domain.remote.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 백업 파일 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "백업 파일 정보")
public class BackupFileInfo {

    @Schema(description = "파일명", example = "backup_remote_20251203_171500.sql")
    private String fileName;

    @Schema(description = "파일 크기 (바이트)", example = "131072000")
    private Long fileSizeBytes;

    @Schema(description = "파일 크기 (읽기 쉬운 형식)", example = "125 MB")
    private String fileSizeFormatted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 시간", example = "2025-12-03 17:15:00")
    private LocalDateTime createdAt;

    @Schema(description = "데이터베이스명", example = "NMS_DB")
    private String database;

    @Schema(description = "호스트", example = "192.168.1.100")
    private String host;

    /**
     * 바이트 크기를 읽기 쉬운 형식으로 변환
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
