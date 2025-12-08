package com.ts.rm.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * Account DTO 통합 클래스 - Request/Response DTO를 내부 클래스로 통합 관리 - Request: 상단 배치 -
 * Response: 하단 배치
 */
public final class AccountDto {

    // 인스턴스화 방지
    private AccountDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 계정 생성 요청
     */
    @Builder
    @Schema(description = "계정 생성 요청")
    public record CreateRequest(
            @Schema(description = "이메일", example = "account@example.com") @NotBlank(message = "이메일은 필수입니다") @Email(message = "올바른 이메일 형식이 아닙니다")
            String email,

            @Schema(description = "비밀번호 (8~100자)", example = "password1234") @NotBlank(message = "비밀번호는 필수입니다") @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
            String password,

            @Schema(description = "이름 (2~50자)", example = "홍길동") @NotBlank(message = "이름은 필수입니다") @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
            String accountName,

            @Schema(description = "계정 권한", example = "USER", defaultValue = "USER")
            String role,

            @Schema(description = "계정 상태", example = "ACTIVE", defaultValue = "ACTIVE")
            String status
    ) {

        /**
         * Compact Constructor - 기본값 설정
         */
        public CreateRequest {
            if (role == null || role.isBlank()) {
                role = "USER";
            }
            if (status == null || status.isBlank()) {
                status = "ACTIVE";
            }
        }
    }

    /**
     * 계정 수정 요청 (본인 정보 수정용)
     */
    @Builder
    @Schema(description = "계정 수정 요청")
    public record UpdateRequest(
            @Schema(description = "이름 (2~50자)", example = "홍길동") @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
            String accountName,

            @Schema(description = "비밀번호 (8~100자)", example = "password1234") @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
            String password
    ) {

    }

    /**
     * 계정 수정 요청 (ADMIN용 - 권한, 상태 수정)
     */
    @Builder
    @Schema(description = "계정 수정 요청 (ADMIN 전용)")
    public record AdminUpdateRequest(
            @Schema(description = "권한 (ADMIN, USER)", example = "USER")
            String role,

            @Schema(description = "상태 (ACTIVE, INACTIVE)", example = "ACTIVE")
            String status
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 계정 상세 응답
     */
    @Schema(description = "계정 상세 응답")
    public record DetailResponse(
            @Schema(description = "계정 ID", example = "1")
            Long accountId,

            @Schema(description = "이메일", example = "account@example.com")
            String email,

            @Schema(description = "이름", example = "홍길동")
            String accountName,

            @Schema(description = "권한", example = "USER")
            String role,

            @Schema(description = "상태", example = "ACTIVE")
            String status,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }

    /**
     * 계정 간단 응답
     */
    @Schema(description = "계정 간단 응답")
    public record SimpleResponse(
            @Schema(description = "계정 ID", example = "1")
            Long accountId,

            @Schema(description = "이메일", example = "account@example.com")
            String email,

            @Schema(description = "이름", example = "홍길동")
            String accountName,

            @Schema(description = "상태", example = "ACTIVE")
            String status
    ) {

    }

    /**
     * 계정 목록 응답 (페이징용)
     */
    @Schema(description = "계정 목록 응답")
    public record ListResponse(
            @Schema(description = "행 번호", example = "1")
            Long rowNumber,

            @Schema(description = "계정 ID", example = "1")
            Long accountId,

            @Schema(description = "이메일", example = "account@example.com")
            String email,

            @Schema(description = "이름", example = "홍길동")
            String accountName,

            @Schema(description = "권한", example = "USER")
            String role,

            @Schema(description = "상태", example = "ACTIVE")
            String status,

            @Schema(description = "마지막 로그인 일시")
            LocalDateTime lastLoginAt,

            @Schema(description = "생성일시")
            LocalDateTime createdAt
    ) {

    }
}
