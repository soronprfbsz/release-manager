package com.ts.rm.global.security;

import lombok.Builder;

/**
 * 인증 토큰 정보
 *
 * <p>SecurityContext에서 추출한 인증 정보를 담는 DTO
 */
@Builder
public record TokenInfo(
        String email,
        String name,
        String role
) {
    /**
     * 기본 정보만 포함한 TokenInfo 생성 (이메일만)
     */
    public static TokenInfo ofEmail(String email) {
        return TokenInfo.builder()
                .email(email)
                .build();
    }
}
