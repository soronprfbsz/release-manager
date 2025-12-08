package com.ts.rm.global.security;

import com.ts.rm.domain.account.enums.AccountRole;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 역할(Role) 기반 권한 검증 유틸리티
 *
 * <p>JWT 토큰에서 추출한 사용자 역할을 검증하는 공통 유틸리티
 *
 * <p>사용 예시:
 * <pre>{@code
 * // ADMIN 권한 필수인 API
 * public void deleteAccount(Long accountId) {
 *     RoleValidator.requireAdmin();  // ADMIN이 아니면 예외 발생
 *     // 삭제 로직 실행
 * }
 * }</pre>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleValidator {

    /**
     * ADMIN 권한 필수 검증
     *
     * <p>현재 인증된 사용자의 역할이 ADMIN이 아니면 BusinessException(FORBIDDEN) 발생
     *
     * @throws BusinessException 현재 사용자가 ADMIN이 아닌 경우
     */
    public static void requireAdmin() {
        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        String role = tokenInfo.role();

        if (role == null || !role.equals(AccountRole.ADMIN.getCodeId())) {
            log.warn("Access denied - required role: ADMIN, current role: {}", role);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        log.debug("Access granted - user has ADMIN role");
    }

    /**
     * 특정 역할 필수 검증
     *
     * <p>현재 인증된 사용자의 역할이 요구되는 역할과 일치하지 않으면 BusinessException(FORBIDDEN) 발생
     *
     * @param requiredRole 요구되는 역할
     * @throws BusinessException 현재 사용자가 요구되는 역할이 아닌 경우
     */
    public static void requireRole(AccountRole requiredRole) {
        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        String role = tokenInfo.role();

        if (role == null || !role.equals(requiredRole.getCodeId())) {
            log.warn("Access denied - required role: {}, current role: {}", requiredRole, role);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        log.debug("Access granted - user has {} role", requiredRole);
    }

    /**
     * 현재 사용자의 역할이 ADMIN인지 확인
     *
     * @return ADMIN이면 true, 아니면 false
     */
    public static boolean isAdmin() {
        try {
            TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
            String role = tokenInfo.role();
            return role != null && role.equals(AccountRole.ADMIN.getCodeId());
        } catch (Exception e) {
            log.warn("Failed to check admin role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 현재 사용자의 역할 조회
     *
     * @return 현재 사용자의 역할 (ADMIN, USER 등)
     */
    public static String getCurrentRole() {
        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        return tokenInfo.role();
    }
}
