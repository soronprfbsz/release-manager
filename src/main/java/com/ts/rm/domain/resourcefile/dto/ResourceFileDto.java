package com.ts.rm.domain.resourcefile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ResourceFile DTO
 *
 * <p>리소스 파일 관리를 위한 DTO (파일시스템 기반)
 */
public final class ResourceFileDto {

    private ResourceFileDto() {
    }

    // ========================================
    // File Tree DTOs
    // ========================================

    /**
     * 파일 트리 노드 (디렉토리 또는 파일)
     */
    @Schema(description = "파일 트리 노드")
    public record FileNode(
            @Schema(description = "파일/디렉토리 이름", example = "mariadb")
            String name,

            @Schema(description = "상대 경로", example = "/mariadb")
            String path,

            @Schema(description = "파일 경로 (resources/file/{category}/ 포함)", example = "resources/file/script/mariadb/backup.sh")
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
     * 카테고리 목록 응답
     */
    @Schema(description = "카테고리 목록 응답")
    public record CategoriesResponse(
            @Schema(description = "카테고리 수", example = "3")
            int categoryCount,

            @Schema(description = "카테고리 목록")
            List<CategoryInfo> categories
    ) {
    }

    /**
     * 카테고리 정보
     */
    @Schema(description = "카테고리 정보")
    public record CategoryInfo(
            @Schema(description = "카테고리명 (폴더명)", example = "script")
            String category,

            @Schema(description = "파일 수", example = "5")
            int fileCount,

            @Schema(description = "총 파일 크기 (bytes)", example = "102400")
            long totalSize
    ) {
    }

    /**
     * 파일 트리 응답
     */
    @Schema(description = "파일 트리 응답")
    public record FilesResponse(
            @Schema(description = "카테고리명", example = "script")
            String category,

            @Schema(description = "파일 존재 여부", example = "true")
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
     * 디렉토리 생성 응답
     */
    @Schema(description = "디렉토리 생성 응답")
    public record DirectoryResponse(
            @Schema(description = "카테고리명", example = "script")
            String category,

            @Schema(description = "생성된 디렉토리 경로", example = "/mariadb/scripts")
            String createdPath,

            @Schema(description = "메시지", example = "디렉토리가 생성되었습니다.")
            String message
    ) {
    }

    /**
     * 파일 업로드 응답
     */
    @Schema(description = "파일 업로드 응답")
    public record UploadResponse(
            @Schema(description = "카테고리명", example = "script")
            String category,

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
            @Schema(description = "파일명", example = "backup.sh")
            String fileName,

            @Schema(description = "파일 경로", example = "/mariadb/backup.sh")
            String path,

            @Schema(description = "파일 크기 (bytes)", example = "1024")
            long size
    ) {
    }

    /**
     * 파일 삭제 응답
     */
    @Schema(description = "파일 삭제 응답")
    public record DeleteResponse(
            @Schema(description = "카테고리명", example = "script")
            String category,

            @Schema(description = "삭제된 파일 경로", example = "/mariadb/backup.sh")
            String deletedPath,

            @Schema(description = "메시지", example = "삭제되었습니다.")
            String message
    ) {
    }

    // ========================================
    // Category DTOs
    // ========================================

    /**
     * 카테고리 생성 요청
     */
    @Schema(description = "카테고리 생성 요청")
    public record CategoryCreateRequest(
            @Schema(description = "카테고리명 (폴더명)", example = "docker")
            String categoryName
    ) {
    }

    /**
     * 카테고리 생성 응답
     */
    @Schema(description = "카테고리 생성 응답")
    public record CategoryCreateResponse(
            @Schema(description = "생성된 카테고리명", example = "docker")
            String category,

            @Schema(description = "메시지", example = "카테고리가 생성되었습니다.")
            String message
    ) {
    }

    /**
     * 카테고리 삭제 응답
     */
    @Schema(description = "카테고리 삭제 응답")
    public record CategoryDeleteResponse(
            @Schema(description = "삭제된 카테고리명", example = "docker")
            String category,

            @Schema(description = "메시지", example = "카테고리가 삭제되었습니다.")
            String message
    ) {
    }
}
