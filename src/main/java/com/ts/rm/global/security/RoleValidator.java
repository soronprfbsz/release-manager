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
     * @return 현재 사용자의 역할 (ADMIN, DEVELOPER, USER 등)
     */
    public static String getCurrentRole() {
        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        return tokenInfo.role();
    }

    /**
     * DEVELOPER 이상 권한 필수 검증 (DEVELOPER 또는 ADMIN)
     *
     * <p>현재 인증된 사용자의 역할이 DEVELOPER 또는 ADMIN이 아니면 BusinessException(FORBIDDEN) 발생
     *
     * @throws BusinessException 현재 사용자가 DEVELOPER 이상이 아닌 경우
     */
    public static void requireDeveloperOrHigher() {
        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        String role = tokenInfo.role();

        if (role == null || (!role.equals(AccountRole.ADMIN.getCodeId())
                && !role.equals(AccountRole.DEVELOPER.getCodeId()))) {
            log.warn("Access denied - required role: DEVELOPER or higher, current role: {}", role);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        log.debug("Access granted - user has DEVELOPER or higher role");
    }

    /**
     * USER 이상 권한 필수 검증 (USER, DEVELOPER 또는 ADMIN)
     *
     * <p>현재 인증된 사용자의 역할이 USER 이상이 아니면 BusinessException(FORBIDDEN) 발생
     * GUEST는 제외됩니다.
     *
     * @throws BusinessException 현재 사용자가 USER 이상이 아닌 경우
     */
    public static void requireUserOrHigher() {
        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        String role = tokenInfo.role();

        if (role == null || (!role.equals(AccountRole.ADMIN.getCodeId())
                && !role.equals(AccountRole.DEVELOPER.getCodeId())
                && !role.equals(AccountRole.USER.getCodeId()))) {
            log.warn("Access denied - required role: USER or higher, current role: {}", role);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        log.debug("Access granted - user has USER or higher role");
    }

    /**
     * 현재 사용자의 역할이 DEVELOPER 이상인지 확인
     *
     * @return DEVELOPER 또는 ADMIN이면 true, 아니면 false
     */
    public static boolean isDeveloperOrHigher() {
        try {
            TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
            String role = tokenInfo.role();
            return role != null && (role.equals(AccountRole.ADMIN.getCodeId())
                    || role.equals(AccountRole.DEVELOPER.getCodeId()));
        } catch (Exception e) {
            log.warn("Failed to check developer or higher role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 현재 사용자의 역할이 USER 이상인지 확인
     *
     * @return USER, DEVELOPER 또는 ADMIN이면 true, 아니면 false (GUEST는 false)
     */
    public static boolean isUserOrHigher() {
        try {
            TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
            String role = tokenInfo.role();
            return role != null && (role.equals(AccountRole.ADMIN.getCodeId())
                    || role.equals(AccountRole.DEVELOPER.getCodeId())
                    || role.equals(AccountRole.USER.getCodeId()));
        } catch (Exception e) {
            log.warn("Failed to check user or higher role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 현재 사용자의 역할이 DEVELOPER인지 확인
     *
     * @return DEVELOPER면 true, 아니면 false
     */
    public static boolean isDeveloper() {
        try {
            TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
            String role = tokenInfo.role();
            return role != null && role.equals(AccountRole.DEVELOPER.getCodeId());
        } catch (Exception e) {
            log.warn("Failed to check developer role: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 권한 계층 레벨 조회
     *
     * <p>권한 계층: ADMIN(4) > DEVELOPER(3) > USER(2) > GUEST(1) > 없음(0)
     *
     * @return 현재 사용자의 권한 레벨 (0-4)
     */
    public static int getRoleLevel() {
        try {
            TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
            String role = tokenInfo.role();
            if (role == null) return 0;
            return switch (role) {
                case "ADMIN" -> 4;
                case "DEVELOPER" -> 3;
                case "USER" -> 2;
                case "GUEST" -> 1;
                default -> 0;
            };
        } catch (Exception e) {
            log.warn("Failed to get role level: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 지정된 역할 이상의 권한인지 확인
     *
     * @param requiredRole 요구되는 최소 역할
     * @return 요구되는 역할 이상이면 true
     */
    public static boolean hasRoleOrHigher(AccountRole requiredRole) {
        int currentLevel = getRoleLevel();
        int requiredLevel = switch (requiredRole.getCodeId()) {
            case "ADMIN" -> 4;
            case "DEVELOPER" -> 3;
            case "USER" -> 2;
            case "GUEST" -> 1;
            default -> 0;
        };
        return currentLevel >= requiredLevel;
    }

    /**
     * 지정된 역할 이상의 권한 필수 검증
     *
     * @param requiredRole 요구되는 최소 역할
     * @throws BusinessException 현재 사용자가 요구되는 역할 이상이 아닌 경우
     */
    public static void requireRoleOrHigher(AccountRole requiredRole) {
        if (!hasRoleOrHigher(requiredRole)) {
            String currentRole = getCurrentRole();
            log.warn("Access denied - required role: {} or higher, current role: {}",
                    requiredRole, currentRole);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        log.debug("Access granted - user has {} or higher role", requiredRole);
    }
}
