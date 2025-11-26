package com.ts.rm.domain.patch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Patch DTO 통합 클래스
 */
public final class PatchDto {

    private PatchDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 패치 생성 요청
     */
    @Builder
    @Schema(description = "패치 생성 요청")
    public record GenerateRequest(
            @Schema(description = "릴리즈 타입", example = "standard")
            @NotBlank(message = "릴리즈 타입은 필수입니다")
            String type,

            @Schema(description = "고객사 ID (커스텀인 경우)", example = "1")
            Long customerId,

            @Schema(description = "시작 버전", example = "1.0.0")
            @NotBlank(message = "시작 버전은 필수입니다")
            @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "버전 형식이 올바르지 않습니다 (예: 1.0.0)")
            String fromVersion,

            @Schema(description = "종료 버전", example = "1.1.1")
            @NotBlank(message = "종료 버전은 필수입니다")
            @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "버전 형식이 올바르지 않습니다 (예: 1.1.1)")
            String toVersion,

            @Schema(description = "생성자", example = "admin@tscientific")
            @NotBlank(message = "생성자는 필수입니다")
            @Size(max = 100, message = "생성자는 100자 이하여야 합니다")
            String generatedBy,

            @Schema(description = "설명", example = "1.0.0에서 1.1.1로 업그레이드용 누적 패치")
            String description,

            @Schema(description = "패치 담당자", example = "홍길동")
            String patchedBy,

            @Schema(description = "패치 이름 (미입력 시 자동 생성: 날짜_fromversion_toversion)", example = "20251125_1.0.0_1.1.1")
            @Size(max = 100, message = "패치 이름은 100자 이하여야 합니다")
            String patchName
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 패치 상세 응답
     */
    @Schema(description = "패치 상세 응답")
    public record DetailResponse(
            @Schema(description = "패치 ID", example = "1")
            Long patchId,

            @Schema(description = "릴리즈 타입", example = "standard")
            String releaseType,

            @Schema(description = "고객사 코드", example = "company_a")
            String customerCode,

            @Schema(description = "고객사명", example = "A 회사")
            String customerName,

            @Schema(description = "시작 버전", example = "1.0.0")
            String fromVersion,

            @Schema(description = "종료 버전", example = "1.1.1")
            String toVersion,

            @Schema(description = "패치 이름", example = "20251125_1.0.0_1.1.1")
            String patchName,

            @Schema(description = "출력 경로", example = "releases/standard/1.1.x/1.1.1/from-1.0.0")
            String outputPath,

            @Schema(description = "생성일시")
            LocalDateTime generatedAt,

            @Schema(description = "생성자", example = "admin@tscientific")
            String generatedBy,

            @Schema(description = "설명", example = "1.0.0에서 1.1.1로 업그레이드용 누적 패치")
            String description,

            @Schema(description = "패치 담당자", example = "홍길동")
            String patchedBy,

            @Schema(description = "등록일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }

    /**
     * 패치 간단 응답
     */
    @Schema(description = "패치 간단 응답")
    public record SimpleResponse(
            @Schema(description = "패치 ID", example = "1")
            Long patchId,

            @Schema(description = "릴리즈 타입", example = "standard")
            String releaseType,

            @Schema(description = "고객사 코드", example = "company_a")
            String customerCode,

            @Schema(description = "고객사명", example = "A 회사")
            String customerName,

            @Schema(description = "시작 버전", example = "1.0.0")
            String fromVersion,

            @Schema(description = "종료 버전", example = "1.1.1")
            String toVersion,

            @Schema(description = "패치 이름", example = "20251125_1.0.0_1.1.1")
            String patchName,

            @Schema(description = "생성일시")
            LocalDateTime generatedAt,

            @Schema(description = "생성자", example = "admin@tscientific")
            String generatedBy,

            @Schema(description = "설명", example = "1.0.0에서 1.1.1로 업그레이드용 누적 패치")
            String description,

            @Schema(description = "패치 담당자", example = "홍길동")
            String patchedBy
    ) {

    }
}
