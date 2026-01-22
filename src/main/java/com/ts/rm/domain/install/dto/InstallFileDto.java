package com.ts.rm.domain.install.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Install File DTO 통합 클래스
 *
 * <p>프로젝트별 인스톨 파일 관리를 위한 DTO
 */
public final class InstallFileDto {

    private InstallFileDto() {
    }

    // ========================================
    // File Tree DTOs
    // ========================================

    /**
     * 인스톨 파일 트리 노드 (디렉토리 또는 파일)
     */
    @Schema(description = "인스톨 파일 트리 노드")
    public record FileNode(
            @Schema(description = "파일/디렉토리 이름", example = "mariadb")
            String name,

            @Schema(description = "전체 경로", example = "/mariadb")
            String path,

            @Schema(description = "파일 경로 (installs/{projectId}/ 포함)", example = "installs/infraeye2/mariadb/init.sql")
            String filePath,

            @Schema(description = "타입 (file 또는 directory)", example = "directory")
            String type,

            @Schema(description = "파일 크기 (파일인 경우만, bytes)", example = "1024")
            Long size,

            @Schema(description = "수정 날짜 (파일인 경우만)", example = "2025-01-22T10:30:00")
            LocalDateTime modifiedAt,

            @Schema(description = "파일 유형/MIME 타입 (파일인 경우만)", example = "text/x-sql")
            String mimeType,

            @Schema(description = "하위 노드 (디렉토리인 경우만)")
            List<FileNode> children
    ) {
        /**
         * 디렉토리 노드 생성
         */
        public static FileNode directory(String name, String path, String filePath, List<FileNode> children) {
            return new FileNode(name, path, filePath, "directory", null, null, null, children);
        }

        /**
         * 파일 노드 생성
         */
        public static FileNode file(String name, String path, String filePath, Long size,
                LocalDateTime modifiedAt, String mimeType) {
            return new FileNode(name, path, filePath, "file", size, modifiedAt, mimeType, null);
        }
    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 인스톨 파일 트리 응답
     */
    @Schema(description = "인스톨 파일 트리 응답")
    public record FilesResponse(
            @Schema(description = "프로젝트 ID", example = "infraeye2")
            String projectId,

            @Schema(description = "프로젝트명", example = "Infraeye 2")
            String projectName,

            @Schema(description = "인스톨 파일 존재 여부", example = "true")
            boolean hasFiles,

            @Schema(description = "총 파일 수", example = "5")
            int totalFileCount,

            @Schema(description = "총 파일 크기 (bytes)", example = "102400")
            long totalSize,

            @Schema(description = "파일 트리")
            FileNode files
    ) {
    }

    /**
     * 인스톨 디렉토리 생성 응답
     */
    @Schema(description = "인스톨 디렉토리 생성 응답")
    public record DirectoryResponse(
            @Schema(description = "프로젝트 ID", example = "infraeye2")
            String projectId,

            @Schema(description = "생성된 디렉토리 경로", example = "/mariadb/scripts")
            String createdPath,

            @Schema(description = "메시지", example = "디렉토리가 생성되었습니다.")
            String message
    ) {
    }

    /**
     * 인스톨 파일 업로드 응답
     */
    @Schema(description = "인스톨 파일 업로드 응답")
    public record UploadResponse(
            @Schema(description = "프로젝트 ID", example = "infraeye2")
            String projectId,

            @Schema(description = "업로드된 파일 수", example = "3")
            int uploadedFileCount,

            @Schema(description = "업로드된 파일 목록")
            List<UploadedFileInfo> uploadedFiles,

            @Schema(description = "메시지", example = "3개 파일이 업로드되었습니다.")
            String message
    ) {
    }

    /**
     * 업로드된 파일 정보
     */
    @Schema(description = "업로드된 파일 정보")
    public record UploadedFileInfo(
            @Schema(description = "파일명", example = "init_schema.sql")
            String fileName,

            @Schema(description = "파일 경로", example = "/mariadb/init_schema.sql")
            String path,

            @Schema(description = "파일 크기 (bytes)", example = "1024")
            long size
    ) {
    }

    /**
     * 인스톨 파일 삭제 응답
     */
    @Schema(description = "인스톨 파일 삭제 응답")
    public record DeleteResponse(
            @Schema(description = "프로젝트 ID", example = "infraeye2")
            String projectId,

            @Schema(description = "삭제된 파일 경로", example = "/mariadb/init_schema.sql")
            String deletedPath,

            @Schema(description = "메시지", example = "파일이 삭제되었습니다.")
            String message
    ) {
    }
}
