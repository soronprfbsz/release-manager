package com.ts.rm.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그인 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로그인 응답")
public class SignInResponse {

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "토큰 만료 시간 (초)", example = "3600")
    private Long expiresIn;

    @Schema(description = "계정 정보")
    private AccountInfo accountInfo;

    /**
     * 계정 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "계정 정보")
    public static class AccountInfo {

        @Schema(description = "계정 ID", example = "1")
        private Long accountId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "계정 이름", example = "홍길동")
        private String accountName;

        @Schema(description = "권한", example = "ACCOUNT_ROLE_USER")
        private String role;
    }
}
