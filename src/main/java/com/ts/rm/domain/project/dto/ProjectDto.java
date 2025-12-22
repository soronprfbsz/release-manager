package com.ts.rm.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Project DTO 통합 클래스
 */
public final class ProjectDto {

    private ProjectDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 프로젝트 생성 요청
     */
    @Builder
    @Schema(description = "프로젝트 생성 요청")
    public record CreateRequest(
            @Schema(description = "프로젝트 ID (영문 소문자, 숫자, 언더스코어만 허용)", example = "infraeye1")
            @NotBlank(message = "프로젝트 ID는 필수입니다")
            @Size(max = 50, message = "프로젝트 ID는 50자 이하여야 합니다")
            @Pattern(regexp = "^[a-z0-9_]+$", message = "프로젝트 ID는 영문 소문자, 숫자, 언더스코어만 허용됩니다")
            String projectId,

            @Schema(description = "프로젝트명", example = "Infraeye 1")
            @NotBlank(message = "프로젝트명은 필수입니다")
            @Size(max = 100, message = "프로젝트명은 100자 이하여야 합니다")
            String projectName,

            @Schema(description = "설명", example = "Infraeye 1.0 - 레거시 NMS 솔루션")
            String description
    ) {

    }

    /**
     * 프로젝트 수정 요청
     */
    @Builder
    @Schema(description = "프로젝트 수정 요청")
    public record UpdateRequest(
            @Schema(description = "프로젝트명", example = "Infraeye 1")
            @Size(max = 100, message = "프로젝트명은 100자 이하여야 합니다")
            String projectName,

            @Schema(description = "설명", example = "Infraeye 1.0 - 레거시 NMS 솔루션")
            String description,

            @Schema(description = "활성 여부", example = "true")
            Boolean isEnabled
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 프로젝트 상세 응답
     */
    @Schema(description = "프로젝트 상세 응답")
    public record DetailResponse(
            @Schema(description = "프로젝트 ID", example = "infraeye1")
            String projectId,

            @Schema(description = "프로젝트명", example = "Infraeye 1")
            String projectName,

            @Schema(description = "설명", example = "Infraeye 1.0 - 레거시 NMS 솔루션")
            String description,

            @Schema(description = "활성 여부", example = "true")
            Boolean isEnabled,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "생성자", example = "SYSTEM")
            String createdBy
    ) {

    }
}
