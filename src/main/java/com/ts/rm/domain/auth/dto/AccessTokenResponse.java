package com.ts.rm.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Access Token 응답 DTO
 * (Refresh Token은 HttpOnly Cookie로 전달)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Access Token 응답 (Refresh Token은 HttpOnly Cookie로 전달)")
public class AccessTokenResponse {

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
    private Long expiresIn;

    @Schema(description = "계정 정보")
    private TokenResponse.AccountInfo accountInfo;

    /**
     * TokenResponse를 AccessTokenResponse로 변환
     */
    public static AccessTokenResponse from(TokenResponse tokenResponse) {
        return AccessTokenResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .accountInfo(tokenResponse.getAccountInfo())
                .build();
    }
}
