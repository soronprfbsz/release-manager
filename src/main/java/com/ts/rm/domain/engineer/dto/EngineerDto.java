package com.ts.rm.domain.engineer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Engineer DTO 통합 클래스
 * - Request/Response DTO를 내부 클래스로 통합 관리
 * - Request: 상단 배치
 * - Response: 하단 배치
 */
public final class EngineerDto {

    // 인스턴스화 방지
    private EngineerDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 엔지니어 생성 요청
     */
    @Builder
    @Schema(description = "엔지니어 생성 요청")
    public record CreateRequest(
            @Schema(description = "엔지니어 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "이름은 필수입니다")
            @Size(max = 50, message = "이름은 50자 이하여야 합니다")
            String engineerName,

            @Schema(description = "회사 이메일", example = "engineer@tscientific.co.kr", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "이메일은 필수입니다")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
            String engineerEmail,

            @Schema(description = "연락처 (선택)", example = "010-1234-5678", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @Size(max = 20, message = "연락처는 20자 이하여야 합니다")
            String engineerPhone,

            @Schema(description = "직급 코드 (code_type_id=POSITION의 code_id)", example = "MANAGER", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @Size(max = 100, message = "직급 코드는 100자 이하여야 합니다")
            String positionCode,

            @Schema(description = "소속 부서 ID (선택)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            Long departmentId,

            @Schema(description = "설명 (선택)", example = "백엔드 개발 담당", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description
    ) {
    }

    /**
     * 엔지니어 수정 요청
     */
    @Builder
    @Schema(description = "엔지니어 수정 요청")
    public record UpdateRequest(
            @Schema(description = "엔지니어 이름", example = "홍길동")
            @Size(max = 50, message = "이름은 50자 이하여야 합니다")
            String engineerName,

            @Schema(description = "회사 이메일", example = "engineer@tscientific.co.kr")
            @Email(message = "올바른 이메일 형식이 아닙니다")
            @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
            String engineerEmail,

            @Schema(description = "연락처", example = "010-1234-5678")
            @Size(max = 20, message = "연락처는 20자 이하여야 합니다")
            String engineerPhone,

            @Schema(description = "직급 코드 (code_type_id=POSITION의 code_id)", example = "MANAGER")
            @Size(max = 100, message = "직급 코드는 100자 이하여야 합니다")
            String positionCode,

            @Schema(description = "소속 부서 ID", example = "1")
            Long departmentId,

            @Schema(description = "설명", example = "백엔드 개발 담당")
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description
    ) {
    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 엔지니어 상세 응답
     */
    @Schema(description = "엔지니어 상세 응답")
    public record DetailResponse(
            @Schema(description = "엔지니어 ID", example = "1")
            Long engineerId,

            @Schema(description = "엔지니어 이름", example = "홍길동")
            String engineerName,

            @Schema(description = "회사 이메일", example = "engineer@tscientific.co.kr")
            String engineerEmail,

            @Schema(description = "연락처", example = "010-1234-5678")
            String engineerPhone,

            @Schema(description = "직급 코드 (code_id)", example = "MANAGER")
            String positionCode,

            @Schema(description = "직급명 (code_name)", example = "과장")
            String position,

            @Schema(description = "소속 부서 ID", example = "1")
            Long departmentId,

            @Schema(description = "소속 부서명", example = "개발2팀")
            String departmentName,

            @Schema(description = "설명", example = "백엔드 개발 담당")
            String description,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "생성자")
            String createdBy,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt,

            @Schema(description = "수정자")
            String updatedBy
    ) {
    }

    /**
     * 엔지니어 간단 응답
     */
    @Schema(description = "엔지니어 간단 응답")
    public record SimpleResponse(
            @Schema(description = "엔지니어 ID", example = "1")
            Long engineerId,

            @Schema(description = "엔지니어 이름", example = "홍길동")
            String engineerName,

            @Schema(description = "회사 이메일", example = "engineer@tscientific.co.kr")
            String engineerEmail,

            @Schema(description = "직급 코드 (code_id)", example = "MANAGER")
            String positionCode,

            @Schema(description = "직급명 (code_name)", example = "과장")
            String position,

            @Schema(description = "소속 부서명", example = "개발2팀")
            String departmentName
    ) {
    }

    /**
     * 엔지니어 목록 응답 (페이징용)
     */
    @Schema(description = "엔지니어 목록 응답")
    public record ListResponse(
            @Schema(description = "행 번호", example = "1")
            Long rowNumber,

            @Schema(description = "엔지니어 ID", example = "1")
            Long engineerId,

            @Schema(description = "엔지니어 이름", example = "홍길동")
            String engineerName,

            @Schema(description = "회사 이메일", example = "engineer@tscientific.co.kr")
            String engineerEmail,

            @Schema(description = "연락처", example = "010-1234-5678")
            String engineerPhone,

            @Schema(description = "직급 코드 (code_id)", example = "MANAGER")
            String positionCode,

            @Schema(description = "직급명 (code_name)", example = "과장")
            String position,

            @Schema(description = "소속 부서 ID", example = "1")
            Long departmentId,

            @Schema(description = "소속 부서명", example = "개발2팀")
            String departmentName,

            @Schema(description = "설명", example = "백엔드 개발 담당")
            String description,

            @Schema(description = "생성일시")
            LocalDateTime createdAt
    ) {
    }
}
