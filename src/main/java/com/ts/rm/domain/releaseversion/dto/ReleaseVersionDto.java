package com.ts.rm.domain.releaseversion.dto;

import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * ReleaseVersion DTO 통합 클래스
 */
public final class ReleaseVersionDto {

    private ReleaseVersionDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 릴리즈 버전 생성 요청
     */
    @Builder
    @Schema(description = "릴리즈 버전 생성 요청")
    public record CreateRequest(
            @Schema(description = "버전 (예: 1.1.0)", example = "1.1.0") @NotBlank(message = "버전은 필수입니다") @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "버전 형식이 올바르지 않습니다 (예: 1.1.0)")
            String version,

            @Schema(description = "생성자", example = "jhlee@tscientific") @NotBlank(message = "생성자는 필수입니다") @Size(max = 100, message = "생성자는 100자 이하여야 합니다")
            String createdBy,

            @Schema(description = "버전 코멘트", example = "새로운 기능 추가")
            String comment,

            @Schema(description = "고객사 ID (커스텀 릴리즈인 경우)", example = "1")
            Long customerId,

            @Schema(description = "커스텀 버전 (커스텀 릴리즈인 경우)", example = "1.0.0-custom")
            String customVersion,

            @Schema(description = "설치본 여부", example = "false", defaultValue = "false")
            Boolean isInstall
    ) {

        public CreateRequest {
            if (isInstall == null) {
                isInstall = false;
            }
        }
    }

    /**
     * 릴리즈 버전 수정 요청
     */
    @Builder
    @Schema(description = "릴리즈 버전 수정 요청")
    public record UpdateRequest(
            @Schema(description = "버전 코멘트", example = "수정된 코멘트")
            String comment,

            @Schema(description = "설치본 여부", example = "true")
            Boolean isInstall
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 릴리즈 버전 상세 응답
     */
    @Schema(description = "릴리즈 버전 상세 응답")
    public record DetailResponse(
            @Schema(description = "릴리즈 버전 ID", example = "1")
            Long releaseVersionId,

            @Schema(description = "릴리즈 타입", example = "standard")
            String releaseType,

            @Schema(description = "고객사 코드 (커스텀인 경우)", example = "company_a")
            String customerCode,

            @Schema(description = "버전", example = "1.1.0")
            String version,

            @Schema(description = "메이저 버전", example = "1")
            Integer majorVersion,

            @Schema(description = "마이너 버전", example = "1")
            Integer minorVersion,

            @Schema(description = "패치 버전", example = "0")
            Integer patchVersion,

            @Schema(description = "메이저.마이너", example = "1.1.x")
            String majorMinor,

            @Schema(description = "생성자", example = "jhlee@tscientific")
            String createdBy,

            @Schema(description = "코멘트", example = "새로운 기능 추가")
            String comment,

            @Schema(description = "커스텀 버전", example = "1.0.0-custom")
            String customVersion,

            @Schema(description = "설치본 여부", example = "false")
            Boolean isInstall,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt,

            @Schema(description = "릴리즈 파일 목록")
            List<ReleaseFileDto.SimpleResponse> releaseFiles
    ) {

    }

    /**
     * 릴리즈 버전 간단 응답
     */
    @Schema(description = "릴리즈 버전 간단 응답")
    public record SimpleResponse(
            @Schema(description = "릴리즈 버전 ID", example = "1")
            Long releaseVersionId,

            @Schema(description = "릴리즈 타입", example = "standard")
            String releaseType,

            @Schema(description = "고객사 코드", example = "company_a")
            String customerCode,

            @Schema(description = "버전", example = "1.1.0")
            String version,

            @Schema(description = "메이저.마이너", example = "1.1.x")
            String majorMinor,

            @Schema(description = "생성자", example = "jhlee@tscientific")
            String createdBy,

            @Schema(description = "코멘트", example = "새로운 기능 추가")
            String comment,

            @Schema(description = "설치본 여부", example = "false")
            Boolean isInstall,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "패치 파일 개수", example = "5")
            Integer patchFileCount
    ) {

    }

    /**
     * 릴리즈 버전 트리 응답 (프론트엔드 트리 렌더링용)
     */
    @Schema(description = "릴리즈 버전 트리 응답")
    public record TreeResponse(
            @Schema(description = "릴리즈 타입", example = "STANDARD")
            String releaseType,

            @Schema(description = "고객사 코드 (커스텀인 경우)", example = "company_a")
            String customerCode,

            @Schema(description = "메이저.마이너 그룹 목록")
            List<MajorMinorNode> majorMinorGroups
    ) {

    }

    /**
     * 메이저.마이너 버전 그룹 노드 (예: 1.1.x)
     */
    @Schema(description = "메이저.마이너 버전 그룹 노드")
    public record MajorMinorNode(
            @Schema(description = "메이저.마이너", example = "1.1.x")
            String majorMinor,

            @Schema(description = "해당 메이저.마이너에 속한 버전 목록")
            List<VersionNode> versions
    ) {

    }

    /**
     * 버전 노드 (예: 1.1.0)
     */
    @Schema(description = "버전 노드")
    public record VersionNode(
            @Schema(description = "버전 ID", example = "1")
            Long versionId,

            @Schema(description = "버전", example = "1.1.0")
            String version,

            @Schema(description = "생성일자", example = "2025-11-20")
            String createdAt,

            @Schema(description = "생성자", example = "jhlee@tscientific")
            String createdBy,

            @Schema(description = "코멘트", example = "새로운 기능 추가")
            String comment,

            @Schema(description = "설치본 여부", example = "false")
            Boolean isInstall,

            @Schema(description = "데이터베이스별 패치 파일 목록")
            List<DatabaseNode> databases
    ) {

    }

    /**
     * 데이터베이스 노드 (예: MARIADB, CRATEDB)
     */
    @Schema(description = "데이터베이스 노드")
    public record DatabaseNode(
            @Schema(description = "데이터베이스 타입", example = "MARIADB")
            String databaseType,

            @Schema(description = "SQL 파일 목록")
            List<String> files
    ) {

    }
}
