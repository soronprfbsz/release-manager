package com.ts.rm.domain.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Customer DTO 통합 클래스
 */
public final class CustomerDto {

    private CustomerDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 고객사 생성 요청
     */
    @Builder
    @Schema(description = "고객사 생성 요청")
    public record CreateRequest(
            @Schema(description = "고객사 코드", example = "company_a") @NotBlank(message = "고객사 코드는 필수입니다") @Size(max = 50, message = "고객사 코드는 50자 이하여야 합니다")
            String customerCode,

            @Schema(description = "고객사명", example = "A회사") @NotBlank(message = "고객사명은 필수입니다") @Size(max = 100, message = "고객사명은 100자 이하여야 합니다")
            String customerName,

            @Schema(description = "설명", example = "고객사 설명")
            String description,

            @Schema(description = "활성 여부", example = "true", defaultValue = "true")
            Boolean isActive,

            @Schema(description = "생성자 (JWT 토큰에서 자동 추출)", example = "admin@tscientific", hidden = true) @Size(max = 100, message = "생성자는 100자 이하여야 합니다")
            String createdBy
    ) {

        public CreateRequest {
            if (isActive == null) {
                isActive = true;
            }
        }
    }

    /**
     * 고객사 수정 요청
     */
    @Builder
    @Schema(description = "고객사 수정 요청")
    public record UpdateRequest(
            @Schema(description = "고객사명", example = "A회사") @Size(max = 100, message = "고객사명은 100자 이하여야 합니다")
            String customerName,

            @Schema(description = "설명", example = "고객사 설명")
            String description,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive,

            @Schema(description = "수정자", example = "admin@tscientific") @NotBlank(message = "수정자는 필수입니다") @Size(max = 100, message = "수정자는 100자 이하여야 합니다")
            String updatedBy
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 고객사 상세 응답
     */
    @Schema(description = "고객사 상세 응답")
    public record DetailResponse(
            @Schema(description = "고객사 ID", example = "1")
            Long customerId,

            @Schema(description = "고객사 코드", example = "company_a")
            String customerCode,

            @Schema(description = "고객사명", example = "A회사")
            String customerName,

            @Schema(description = "설명", example = "고객사 설명")
            String description,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "생성자", example = "admin@tscientific")
            String createdBy,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt,

            @Schema(description = "수정자", example = "admin@tscientific")
            String updatedBy
    ) {

    }

    /**
     * 고객사 간단 응답
     */
    @Schema(description = "고객사 간단 응답")
    public record SimpleResponse(
            @Schema(description = "고객사 ID", example = "1")
            Long customerId,

            @Schema(description = "고객사 코드", example = "company_a")
            String customerCode,

            @Schema(description = "고객사명", example = "A회사")
            String customerName,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive
    ) {

    }
}
