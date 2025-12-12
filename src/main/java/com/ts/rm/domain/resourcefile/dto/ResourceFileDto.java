package com.ts.rm.domain.resourcefile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

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
            @Schema(description = "파일 카테고리",
                    example = "SCRIPT",
                    allowableValues = {"SCRIPT", "DOCKER", "DOCUMENT", "ETC"})
            @NotBlank(message = "파일 카테고리는 필수입니다")
            String fileCategory,

            @Schema(description = "하위 카테고리\n" +
                    "- SCRIPT: MARIADB, CRATEDB, ETC\n" +
                    "- DOCKER: SERVICE, DOCKERFILE, ETC\n" +
                    "- DOCUMENT: INFRAEYE1, INFRAEYE2, ETC\n" +
                    "- ETC: ETC",
                    example = "MARIADB")
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

    /**
     * 분류 가이드 응답
     */
    @Schema(description = "리소스 파일 분류 가이드")
    public record CategoryGuideResponse(
            @Schema(description = "카테고리 코드", example = "SCRIPT")
            String code,

            @Schema(description = "카테고리 표시명", example = "스크립트")
            String displayName,

            @Schema(description = "카테고리 설명", example = "스크립트 파일 (백업, 복원 등)")
            String description,

            @Schema(description = "하위 카테고리 목록")
            java.util.List<SubCategoryInfo> subCategories
    ) {
    }

    /**
     * 하위 카테고리 정보
     */
    @Schema(description = "하위 카테고리 정보")
    public record SubCategoryInfo(
            @Schema(description = "하위 카테고리 코드", example = "MARIADB")
            String code,

            @Schema(description = "하위 카테고리 표시명", example = "MariaDB")
            String displayName,

            @Schema(description = "하위 카테고리 설명", example = "MariaDB 관련 스크립트")
            String description
    ) {
    }

    /**
     * 리소스 파일 순서 변경 요청
     */
    @Schema(description = "리소스 파일 순서 변경 요청")
    public record ReorderResourceFilesRequest(
            @Schema(description = "정렬할 리소스 파일 ID 목록 (순서대로)", example = "[1, 3, 2, 4]")
            @NotEmpty(message = "리소스 파일 ID 목록은 비어있을 수 없습니다")
            List<Long> resourceFileIds
    ) {
    }
}
