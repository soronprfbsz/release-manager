package com.ts.rm.domain.releasefile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * ReleaseFile DTO 통합 클래스
 */
public final class ReleaseFileDto {

    private ReleaseFileDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 릴리즈 파일 생성 요청
     */
    @Builder
    @Schema(description = "릴리즈 파일 생성 요청")
    public record CreateRequest(
            @Schema(description = "릴리즈 버전 ID", example = "1") @NotNull(message = "릴리즈 버전 ID는 필수입니다")
            Long releaseVersionId,

            @Schema(description = "파일 카테고리", example = "DATABASE")
            String fileCategory,

            @Schema(description = "하위 카테고리", example = "mariadb")
            String subCategory,

            @Schema(description = "파일명", example = "001_create_users_table.sql") @NotBlank(message = "파일명은 필수입니다") @Size(max = 255, message = "파일명은 255자 이하여야 합니다")
            String fileName,

            @Schema(description = "파일 경로", example = "/release-manager/versions/1.1.x/1.1.0/mariadb/001_create_users_table.sql") @NotBlank(message = "파일 경로는 필수입니다") @Size(max = 500, message = "파일 경로는 500자 이하여야 합니다")
            String filePath,

            @Schema(description = "파일 크기 (bytes)", example = "1024") @NotNull(message = "파일 크기는 필수입니다")
            Long fileSize,

            @Schema(description = "체크섬 (MD5)", example = "abc123def456") @NotBlank(message = "체크섬은 필수입니다")
            String checksum,

            @Schema(description = "실행 순서", example = "1") @NotNull(message = "실행 순서는 필수입니다")
            Integer executionOrder,

            @Schema(description = "설명", example = "Create users table")
            String description
    ) {

    }

    /**
     * 릴리즈 파일 업로드 요청
     */
    @Builder
    @Schema(description = "릴리즈 파일 업로드 요청")
    public record UploadRequest(
            @Schema(description = "파일 카테고리 (선택사항, 미입력 시 자동 감지)", example = "DATABASE")
            String fileCategory,

            @Schema(description = "하위 카테고리 (선택사항)", example = "mariadb")
            String subCategory,

            @Schema(description = "업로드 사용자", example = "admin@tscientific") @NotBlank(message = "업로드 사용자는 필수입니다") @Size(max = 100, message = "업로드 사용자는 100자 이하여야 합니다")
            String uploadedBy
    ) {

    }

    /**
     * 릴리즈 파일 수정 요청
     */
    @Builder
    @Schema(description = "릴리즈 파일 수정 요청")
    public record UpdateRequest(
            @Schema(description = "설명", example = "Updated description")
            String description,

            @Schema(description = "실행 순서", example = "1")
            Integer executionOrder
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 릴리즈 파일 상세 응답
     */
    @Schema(description = "릴리즈 파일 상세 응답")
    public record DetailResponse(
            @Schema(description = "릴리즈 파일 ID", example = "1")
            Long releaseFileId,

            @Schema(description = "릴리즈 버전 ID", example = "1")
            Long releaseVersionId,

            @Schema(description = "릴리즈 버전", example = "1.1.0")
            String releaseVersion,

            @Schema(description = "파일 카테고리", example = "DATABASE")
            String fileCategory,

            @Schema(description = "하위 카테고리", example = "mariadb")
            String subCategory,

            @Schema(description = "파일명", example = "001_create_users_table.sql")
            String fileName,

            @Schema(description = "파일 경로", example = "/release-manager/1.1.0/mariadb/001_create_users_table.sql")
            String filePath,

            @Schema(description = "파일 크기 (bytes)", example = "1024")
            Long fileSize,

            @Schema(description = "체크섬 (MD5)", example = "abc123def456")
            String checksum,

            @Schema(description = "실행 순서", example = "1")
            Integer executionOrder,

            @Schema(description = "설명", example = "Create users table")
            String description,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }

    /**
     * 릴리즈 파일 간단 응답
     */
    @Schema(description = "릴리즈 파일 간단 응답")
    public record SimpleResponse(
            @Schema(description = "릴리즈 파일 ID", example = "1")
            Long releaseFileId,

            @Schema(description = "릴리즈 버전", example = "1.1.0")
            String releaseVersion,

            @Schema(description = "파일 카테고리", example = "DATABASE")
            String fileCategory,

            @Schema(description = "하위 카테고리", example = "mariadb")
            String subCategory,

            @Schema(description = "파일명", example = "001_create_users_table.sql")
            String fileName,

            @Schema(description = "파일 크기 (bytes)", example = "1024")
            Long fileSize,

            @Schema(description = "체크섬 (MD5)", example = "abc123def456")
            String checksum,

            @Schema(description = "실행 순서", example = "1")
            Integer executionOrder,

            @Schema(description = "설명", example = "Create users table")
            String description
    ) {

    }

    /**
     * 파일 내용 응답
     */
    @Schema(description = "파일 내용 응답")
    public record FileContentResponse(
            @Schema(description = "릴리즈 파일 ID", example = "1")
            Long releaseFileId,

            @Schema(description = "파일 경로", example = "database/mariadb/001_create_users_table.sql")
            String path,

            @Schema(description = "파일명", example = "001_create_users_table.sql")
            String fileName,

            @Schema(description = "파일 크기 (bytes)", example = "1024")
            long size,

            @Schema(description = "MIME 타입", example = "text/x-sql")
            String mimeType,

            @Schema(description = "바이너리 파일 여부 (true면 content는 Base64 인코딩됨)", example = "false")
            boolean isBinary,

            @Schema(description = "파일 내용 (텍스트 또는 Base64)")
            String content
    ) {

    }
}
