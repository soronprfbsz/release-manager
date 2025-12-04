package com.ts.rm.domain.resourcefile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * ResourceFile DTO
 */
public final class ResourceFileDto {

    private ResourceFileDto() {
    }

    /**
     * 리소스 파일 업로드 요청
     */
    @Schema(description = "리소스 파일 업로드 요청")
    public record UploadRequest(
            @Schema(description = "파일 카테고리 (SCRIPT/DOCUMENT/ETC)", example = "SCRIPT")
            @NotBlank(message = "파일 카테고리는 필수입니다")
            String fileCategory,

            @Schema(description = "서브 카테고리 (SCRIPT: MARIADB_BACKUP, MARIADB_RESTORE 등 / DOCUMENT: PDF, TXT 등)", example = "MARIADB_BACKUP")
            String subCategory,

            @Schema(description = "파일 설명", example = "MariaDB 백업 스크립트")
            String description,

            @Schema(description = "업로드 담당자", example = "admin@company.com")
            @NotBlank(message = "업로드 담당자는 필수입니다")
            String createdBy
    ) {
    }

    /**
     * 리소스 파일 상세 응답
     */
    @Schema(description = "리소스 파일 상세 응답")
    public record DetailResponse(
            @Schema(description = "리소스 파일 ID", example = "1")
            Long resourceFileId,

            @Schema(description = "파일 타입 (확장자)", example = "SCRIPT")
            String fileType,

            @Schema(description = "파일 카테고리", example = "SCRIPT")
            String fileCategory,

            @Schema(description = "서브 카테고리", example = "MARIADB_BACKUP")
            String subCategory,

            @Schema(description = "파일명", example = "mariadb_backup.sh")
            String fileName,

            @Schema(description = "파일 경로", example = "resource/script/MARIADB/mariadb_backup.sh")
            String filePath,

            @Schema(description = "파일 크기 (bytes)", example = "11025")
            Long fileSize,

            @Schema(description = "체크섬 (SHA-256)", example = "a1b2c3d4...")
            String checksum,

            @Schema(description = "파일 설명", example = "MariaDB 백업 스크립트")
            String description,

            @Schema(description = "생성자", example = "admin@company.com")
            String createdBy,

            @Schema(description = "생성일시", example = "2025-12-04T10:30:00")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 리소스 파일 목록 응답 (간략)
     */
    @Schema(description = "리소스 파일 목록 응답")
    public record SimpleResponse(
            @Schema(description = "리소스 파일 ID", example = "1")
            Long resourceFileId,

            @Schema(description = "파일 타입 (확장자)", example = "SCRIPT")
            String fileType,

            @Schema(description = "파일 카테고리", example = "SCRIPT")
            String fileCategory,

            @Schema(description = "서브 카테고리", example = "MARIADB_BACKUP")
            String subCategory,

            @Schema(description = "파일명", example = "mariadb_backup.sh")
            String fileName,

            @Schema(description = "파일 경로", example = "resource/script/MARIADB/mariadb_backup.sh")
            String filePath,

            @Schema(description = "파일 크기 (bytes)", example = "11025")
            Long fileSize,

            @Schema(description = "파일 설명", example = "MariaDB 백업 스크립트")
            String description,

            @Schema(description = "생성일시", example = "2025-12-04T10:30:00")
            LocalDateTime createdAt
    ) {
    }
}
