package com.ts.rm.domain.resourcelink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ResourceLink DTO
 */
public final class ResourceLinkDto {

    private ResourceLinkDto() {
    }

    /**
     * 리소스 링크 생성 요청
     */
    @Schema(description = "리소스 링크 생성 요청")
    public record CreateRequest(
            @Schema(description = "링크 카테고리",
                    example = "DOCUMENT",
                    allowableValues = {"DOCUMENT", "TOOL", "ETC"})
            @NotBlank(message = "링크 카테고리는 필수입니다")
            String linkCategory,

            @Schema(description = "하위 카테고리\n" +
                    "- DOCUMENT: INFRAEYE1, INFRAEYE2, ETC\n" +
                    "- TOOL: DEV_TOOL, DESIGN_TOOL, ETC\n" +
                    "- ETC: ETC",
                    example = "INFRAEYE2")
            String subCategory,

            @Schema(description = "링크 이름", example = "Infraeye2 요구사항 정의서")
            @NotBlank(message = "링크 이름은 필수입니다")
            String linkName,

            @Schema(description = "링크 주소", example = "https://docs.google.com/spreadsheets/d/xxx")
            @NotBlank(message = "링크 주소는 필수입니다")
            String linkUrl,

            @Schema(description = "링크 설명", example = "Infraeye2 프로젝트 요구사항 정의서")
            String description,

            @Schema(description = "생성자 이메일", example = "admin@company.com")
            @NotBlank(message = "생성자 이메일은 필수입니다")
            String createdByEmail
    ) {
    }

    /**
     * 리소스 링크 수정 요청
     */
    @Schema(description = "리소스 링크 수정 요청")
    public record UpdateRequest(
            @Schema(description = "링크 카테고리",
                    example = "DOCUMENT",
                    allowableValues = {"DOCUMENT", "TOOL", "ETC"})
            @NotBlank(message = "링크 카테고리는 필수입니다")
            String linkCategory,

            @Schema(description = "하위 카테고리", example = "INFRAEYE2")
            String subCategory,

            @Schema(description = "링크 이름", example = "Infraeye2 요구사항 정의서")
            @NotBlank(message = "링크 이름은 필수입니다")
            String linkName,

            @Schema(description = "링크 주소", example = "https://docs.google.com/spreadsheets/d/xxx")
            @NotBlank(message = "링크 주소는 필수입니다")
            String linkUrl,

            @Schema(description = "링크 설명", example = "Infraeye2 프로젝트 요구사항 정의서")
            String description
    ) {
    }

    /**
     * 리소스 링크 상세 응답
     */
    @Schema(description = "리소스 링크 상세 응답")
    public record DetailResponse(
            @Schema(description = "리소스 링크 ID", example = "1")
            Long resourceLinkId,

            @Schema(description = "링크 카테고리", example = "DOCUMENT")
            String linkCategory,

            @Schema(description = "하위 카테고리", example = "INFRAEYE2")
            String subCategory,

            @Schema(description = "링크 이름", example = "Infraeye2 요구사항 정의서")
            String linkName,

            @Schema(description = "링크 주소", example = "https://docs.google.com/spreadsheets/d/xxx")
            String linkUrl,

            @Schema(description = "링크 설명", example = "Infraeye2 프로젝트 요구사항 정의서")
            String description,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder,

            @Schema(description = "생성자 이메일", example = "admin@company.com")
            String createdByEmail,

            @Schema(description = "생성자 아바타 스타일", example = "lorelei")
            String createdByAvatarStyle,

            @Schema(description = "생성자 아바타 시드", example = "abc123")
            String createdByAvatarSeed,

            @Schema(description = "생성일시", example = "2025-12-19T10:30:00")
            LocalDateTime createdAt,

            @Schema(description = "수정일시", example = "2025-12-19T10:30:00")
            LocalDateTime updatedAt
    ) {
    }

    /**
     * 리소스 링크 목록 응답 (간략)
     */
    @Schema(description = "리소스 링크 목록 응답")
    public record SimpleResponse(
            @Schema(description = "리소스 링크 ID", example = "1")
            Long resourceLinkId,

            @Schema(description = "링크 카테고리", example = "DOCUMENT")
            String linkCategory,

            @Schema(description = "하위 카테고리", example = "INFRAEYE2")
            String subCategory,

            @Schema(description = "링크 이름", example = "Infraeye2 요구사항 정의서")
            String linkName,

            @Schema(description = "링크 주소", example = "https://docs.google.com/spreadsheets/d/xxx")
            String linkUrl,

            @Schema(description = "링크 설명", example = "Infraeye2 프로젝트 요구사항 정의서")
            String description,

            @Schema(description = "생성일시", example = "2025-12-19T10:30:00")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 분류 가이드 응답
     */
    @Schema(description = "리소스 링크 분류 가이드")
    public record CategoryGuideResponse(
            @Schema(description = "카테고리 코드", example = "DOCUMENT")
            String code,

            @Schema(description = "카테고리 표시명", example = "문서")
            String displayName,

            @Schema(description = "카테고리 설명", example = "구글 시트, 노션 등 문서 링크")
            String description,

            @Schema(description = "하위 카테고리 목록")
            List<SubCategoryInfo> subCategories
    ) {
    }

    /**
     * 하위 카테고리 정보
     */
    @Schema(description = "하위 카테고리 정보")
    public record SubCategoryInfo(
            @Schema(description = "하위 카테고리 코드", example = "INFRAEYE2")
            String code,

            @Schema(description = "하위 카테고리 표시명", example = "Infraeye 2")
            String displayName,

            @Schema(description = "하위 카테고리 설명", example = "Infraeye 2 관련 문서")
            String description
    ) {
    }

    /**
     * 리소스 링크 순서 변경 요청
     */
    @Schema(description = "리소스 링크 순서 변경 요청")
    public record ReorderResourceLinksRequest(
            @Schema(description = "링크 카테고리",
                    example = "DOCUMENT",
                    allowableValues = {"DOCUMENT", "TOOL", "ETC"})
            @NotBlank(message = "링크 카테고리는 필수입니다")
            String linkCategory,

            @Schema(description = "정렬할 리소스 링크 ID 목록 (순서대로)", example = "[1, 3, 2, 4]")
            @NotEmpty(message = "리소스 링크 ID 목록은 비어있을 수 없습니다")
            List<Long> resourceLinkIds
    ) {
    }
}
