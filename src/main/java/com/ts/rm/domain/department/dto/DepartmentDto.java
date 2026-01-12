package com.ts.rm.domain.department.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Department DTO 통합 클래스
 */
public final class DepartmentDto {

    private DepartmentDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 부서 생성 요청
     */
    @Schema(description = "부서 생성 요청")
    public record CreateRequest(
            @Schema(description = "부서명", example = "개발2팀", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "부서명은 필수입니다")
            @Size(max = 100, message = "부서명은 100자 이하여야 합니다")
            String departmentName,

            @Schema(description = "부서 유형 (DEVELOPMENT: 개발, ENGINEER: 엔지니어)", example = "DEVELOPMENT")
            @Size(max = 50, message = "부서 유형은 50자 이하여야 합니다")
            String departmentType,

            @Schema(description = "설명", example = "소프트웨어 개발")
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description,

            @Schema(description = "상위 부서 ID (null이면 루트 부서 하위로 생성)", example = "1")
            Long parentDepartmentId,

            @Schema(description = "정렬 순서 (같은 부모 내에서 정렬)", example = "1")
            Integer sortOrder
    ) {
    }

    /**
     * 부서 수정 요청
     */
    @Schema(description = "부서 수정 요청")
    public record UpdateRequest(
            @Schema(description = "부서명", example = "개발2팀")
            @Size(max = 100, message = "부서명은 100자 이하여야 합니다")
            String departmentName,

            @Schema(description = "부서 유형 (DEVELOPMENT: 개발, ENGINEER: 엔지니어)", example = "DEVELOPMENT")
            @Size(max = 50, message = "부서 유형은 50자 이하여야 합니다")
            String departmentType,

            @Schema(description = "설명", example = "소프트웨어 개발")
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder
    ) {
    }

    /**
     * 부서 이동 요청
     */
    @Schema(description = "부서 이동 요청")
    public record MoveRequest(
            @Schema(description = "새 상위 부서 ID (null이면 루트 부서 하위로 이동)", example = "1")
            Long newParentId,

            @Schema(description = "정렬 순서 (같은 부모 내에서 정렬)", example = "1")
            Integer sortOrder
    ) {
    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 부서 응답
     */
    @Schema(description = "부서 응답")
    public record Response(
            @Schema(description = "부서 ID", example = "1")
            Long departmentId,

            @Schema(description = "부서명", example = "개발2팀")
            String departmentName,

            @Schema(description = "부서 유형 (DEVELOPMENT: 개발, ENGINEER: 엔지니어)", example = "DEVELOPMENT")
            String departmentType,

            @Schema(description = "설명", example = "소프트웨어 개발")
            String description,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder
    ) {
    }

    /**
     * 부서 상세 응답 (부모/자식 정보 포함)
     */
    @Schema(description = "부서 상세 응답")
    public record DetailResponse(
            @Schema(description = "부서 ID", example = "1")
            Long departmentId,

            @Schema(description = "부서명", example = "개발2팀")
            String departmentName,

            @Schema(description = "부서 유형 (DEVELOPMENT: 개발, ENGINEER: 엔지니어)", example = "DEVELOPMENT")
            String departmentType,

            @Schema(description = "설명", example = "소프트웨어 개발")
            String description,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder,

            @Schema(description = "상위 부서 ID", example = "1")
            Long parentDepartmentId,

            @Schema(description = "상위 부서명", example = "Tscientific")
            String parentDepartmentName,

            @Schema(description = "계층 깊이 (루트=0)", example = "1")
            Integer depth,

            @Schema(description = "직계 하위 부서 수", example = "3")
            Long childCount,

            @Schema(description = "소속 계정 수", example = "5")
            Long accountCount
    ) {
    }

    /**
     * 부서 트리 응답 (계층 구조)
     */
    @Schema(description = "부서 트리 응답")
    public record TreeResponse(
            @Schema(description = "부서 ID", example = "1")
            Long departmentId,

            @Schema(description = "부서명", example = "개발2팀")
            String departmentName,

            @Schema(description = "부서 유형 (DEVELOPMENT: 개발, ENGINEER: 엔지니어)", example = "DEVELOPMENT")
            String departmentType,

            @Schema(description = "설명", example = "소프트웨어 개발")
            String description,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder,

            @Schema(description = "계층 깊이 (루트=0)", example = "1")
            Integer depth,

            @Schema(description = "소속 계정 수", example = "5")
            Long accountCount,

            @Schema(description = "하위 부서 목록")
            List<TreeResponse> children
    ) {
    }
}
