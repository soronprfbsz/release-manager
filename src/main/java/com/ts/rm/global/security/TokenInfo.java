package com.ts.rm.global.security;

import lombok.Builder;

/**
 * 인증 토큰 정보
 *
 * <p>SecurityContext에서 추출한 인증 정보를 담는 DTO
 * AccountUserDetails에서 추출된 전체 사용자 정보를 포함
 */
@Builder
public record TokenInfo(
        Long accountId,
        String email,
        String accountName,
        String role,
        Long departmentId,
        String departmentName
) {
    /**
     * AccountUserDetails에서 TokenInfo 생성
     *
     * @param userDetails AccountUserDetails 객체
     * @return TokenInfo
     */
    public static TokenInfo from(AccountUserDetails userDetails) {
        return TokenInfo.builder()
                .accountId(userDetails.getAccountId())
                .email(userDetails.getEmail())
                .accountName(userDetails.getAccountName())
                .role(userDetails.getRole())
                .departmentId(userDetails.getDepartmentId())
                .departmentName(userDetails.getDepartmentName())
                .build();
    }

    /**
     * 기본 정보만 포함한 TokenInfo 생성 (이메일만)
     *
     * @deprecated AccountUserDetails 기반의 from() 메서드 사용 권장
     */
    @Deprecated
    public static TokenInfo ofEmail(String email) {
        return TokenInfo.builder()
                .email(email)
                .build();
    }
}
