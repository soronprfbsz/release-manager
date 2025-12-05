package com.ts.rm.domain.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Department DTO 통합 클래스
 */
public final class DepartmentDto {

    private DepartmentDto() {
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

            @Schema(description = "설명", example = "소프트웨어 개발")
            String description
    ) {
    }
}
