package com.ts.rm.domain.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 대시보드 DTO
 */
public class DashboardDto {

    /**
     * 대시보드 응답 DTO
     */
    @Schema(description = "대시보드 응답")
    public record Response(
            @Schema(description = "최신 설치본 정보")
            LatestInstallVersion latestInstall,

            @Schema(description = "최신 릴리즈 버전 목록")
            List<RecentVersion> recentVersions,

            @Schema(description = "최근 생성 패치 목록")
            List<RecentPatch> recentPatches
    ) {
    }

    /**
     * 최신 설치본 정보
     */
    @Schema(description = "최신 설치본 정보")
    public record LatestInstallVersion(
            @Schema(description = "릴리즈 버전 ID", example = "1")
            Long releaseVersionId,

            @Schema(description = "버전", example = "1.0.0")
            String version,

            @Schema(description = "릴리즈 타입", example = "STANDARD")
            String releaseType,

            @Schema(description = "릴리즈 카테고리", example = "INSTALL")
            String releaseCategory,

            @Schema(description = "생성일시")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,

            @Schema(description = "생성자", example = "TS")
            String createdBy,

            @Schema(description = "설명")
            String comment
    ) {
    }

    /**
     * 최근 릴리즈 버전 정보
     */
    @Schema(description = "최근 릴리즈 버전 정보")
    public record RecentVersion(
            @Schema(description = "릴리즈 버전 ID", example = "2")
            Long releaseVersionId,

            @Schema(description = "버전", example = "1.1.0")
            String version,

            @Schema(description = "릴리즈 타입", example = "STANDARD")
            String releaseType,

            @Schema(description = "릴리즈 카테고리", example = "PATCH")
            String releaseCategory,

            @Schema(description = "생성일시")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,

            @Schema(description = "생성자", example = "jhlee@tscientific")
            String createdBy,

            @Schema(description = "설명")
            String comment,

            @Schema(description = "파일 카테고리 목록", example = "[\"DATABASE\", \"WEB\"]")
            List<String> fileCategories
    ) {
    }

    /**
     * 최근 생성 패치 정보
     */
    @Schema(description = "최근 생성 패치 정보")
    public record RecentPatch(
            @Schema(description = "패치 ID", example = "1")
            Long patchId,

            @Schema(description = "패치명", example = "standard_1.0.0_to_1.1.0_patch.zip")
            String patchName,

            @Schema(description = "시작 버전", example = "1.0.0")
            String fromVersion,

            @Schema(description = "종료 버전", example = "1.1.0")
            String toVersion,

            @Schema(description = "릴리즈 타입", example = "STANDARD")
            String releaseType,

            @Schema(description = "생성일시")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createdAt,

            @Schema(description = "생성자", example = "admin")
            String createdBy,

            @Schema(description = "설명")
            String description
    ) {
    }
}
