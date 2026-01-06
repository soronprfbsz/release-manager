package com.ts.rm.domain.publishing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * PublishingFile DTO
 */
public final class PublishingFileDto {

    private PublishingFileDto() {
    }

    /**
     * 퍼블리싱 파일 상세 응답
     */
    @Schema(description = "퍼블리싱 파일 상세 응답")
    public record DetailResponse(
            @Schema(description = "퍼블리싱 파일 ID", example = "1")
            Long publishingFileId,

            @Schema(description = "퍼블리싱 ID", example = "1")
            Long publishingId,

            @Schema(description = "파일 타입 (확장자)", example = "CSS")
            String fileType,

            @Schema(description = "파일명", example = "style.css")
            String fileName,

            @Schema(description = "파일 경로", example = "dashboard/css/style.css")
            String filePath,

            @Schema(description = "파일 크기 (bytes)", example = "11025")
            Long fileSize,

            @Schema(description = "체크섬 (SHA-256)", example = "a1b2c3d4...")
            String checksum,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder,

            @Schema(description = "생성일시", example = "2025-12-04T10:30:00")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 퍼블리싱 파일 목록 응답 (간략)
     */
    @Schema(description = "퍼블리싱 파일 목록 응답")
    public record SimpleResponse(
            @Schema(description = "퍼블리싱 파일 ID", example = "1")
            Long publishingFileId,

            @Schema(description = "파일 타입 (확장자)", example = "CSS")
            String fileType,

            @Schema(description = "파일명", example = "style.css")
            String fileName,

            @Schema(description = "파일 경로", example = "dashboard/css/style.css")
            String filePath,

            @Schema(description = "파일 크기 (bytes)", example = "11025")
            Long fileSize,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder
    ) {
    }
}
