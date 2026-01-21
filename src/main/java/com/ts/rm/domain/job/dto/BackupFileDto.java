package com.ts.rm.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * BackupFile DTO
 */
public final class BackupFileDto {

    private BackupFileDto() {
    }

    /**
     * 백업 파일 상세 응답
     */
    @Schema(description = "백업 파일 상세 응답")
    public record DetailResponse(
            @Schema(description = "백업 파일 ID", example = "1")
            Long backupFileId,

            @Schema(description = "파일 카테고리", example = "MARIADB")
            String fileCategory,

            @Schema(description = "파일 타입", example = "SQL")
            String fileType,

            @Schema(description = "파일명", example = "backup_20251203_171500.sql")
            String fileName,

            @Schema(description = "파일 경로", example = "job/MARIADB/backup_files/backup_20251203_171500.sql")
            String filePath,

            @Schema(description = "파일 크기 (bytes)", example = "131072000")
            Long fileSize,

            @Schema(description = "체크섬 (SHA-256)", example = "a1b2c3d4e5...")
            String checksum,

            @Schema(description = "파일 설명", example = "월간 정기 백업")
            String description,

            @Schema(description = "생성자 이메일", example = "admin@company.com")
            String createdByEmail,

            @Schema(description = "생성자 이름", example = "홍길동")
            String createdByName,

            @Schema(description = "생성자 아바타 스타일", example = "lorelei")
            String createdByAvatarStyle,

            @Schema(description = "생성자 아바타 시드", example = "abc123")
            String createdByAvatarSeed,

            @Schema(description = "생성자 탈퇴 여부", example = "false")
            Boolean isDeletedCreator,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Schema(description = "생성일시", example = "2025-12-03 17:15:00")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 백업 파일 목록 응답 (페이징용)
     */
    @Schema(description = "백업 파일 목록 응답")
    public record ListResponse(
            @Schema(description = "행 번호", example = "1")
            Long rowNumber,

            @Schema(description = "백업 파일 ID", example = "1")
            Long backupFileId,

            @Schema(description = "파일 카테고리", example = "MARIADB")
            String fileCategory,

            @Schema(description = "파일 타입", example = "SQL")
            String fileType,

            @Schema(description = "파일명", example = "backup_20251203_171500.sql")
            String fileName,

            @Schema(description = "파일 크기 (bytes)", example = "131072000")
            Long fileSize,

            @Schema(description = "파일 크기 (읽기 쉬운 형식)", example = "125 MB")
            String fileSizeFormatted,

            @Schema(description = "파일 설명", example = "월간 정기 백업")
            String description,

            @Schema(description = "생성자 이메일", example = "admin@company.com")
            String createdByEmail,

            @Schema(description = "생성자 이름", example = "홍길동")
            String createdByName,

            @Schema(description = "생성자 아바타 스타일", example = "lorelei")
            String createdByAvatarStyle,

            @Schema(description = "생성자 아바타 시드", example = "abc123")
            String createdByAvatarSeed,

            @Schema(description = "생성자 탈퇴 여부", example = "false")
            Boolean isDeletedCreator,

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Schema(description = "생성일시", example = "2025-12-03 17:15:00")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 백업 파일 검색 조건 DTO
     */
    @Schema(description = "백업 파일 검색 조건")
    public record SearchRequest(
            @Schema(description = "파일 카테고리 (MARIADB, CRATEDB)", example = "MARIADB")
            String fileCategory,

            @Schema(description = "파일 타입 (SQL 등)", example = "SQL")
            String fileType,

            @Schema(description = "파일명 (부분 일치)", example = "backup")
            String fileName
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
