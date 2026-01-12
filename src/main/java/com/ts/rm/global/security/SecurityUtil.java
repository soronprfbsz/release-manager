package com.ts.rm.global.security;

import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 유틸리티
 *
 * <p>Spring Security의 SecurityContext에서 인증 정보를 추출하는 유틸리티
 * AccountUserDetails를 사용하여 확장된 사용자 정보를 제공
 */
@Slf4j
public final class SecurityUtil {

    private SecurityUtil() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 현재 인증된 사용자의 토큰 정보 추출
     *
     * <p>SecurityContext에서 AccountUserDetails를 추출하여 TokenInfo로 변환
     * 확장된 정보 포함: accountId, email, accountName, role, departmentId, departmentName
     *
     * @return 토큰 정보
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static TokenInfo getTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found in SecurityContext");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        Object principal = authentication.getPrincipal();

        // AccountUserDetails에서 정보 추출 (권장 방식)
        if (principal instanceof AccountUserDetails userDetails) {
            log.debug("Extracted token info from SecurityContext: accountId={}, email={}, role={}",
                    userDetails.getAccountId(), userDetails.getEmail(), userDetails.getRole());

            return TokenInfo.from(userDetails);
        }

        // String principal (간단한 인증) - 레거시 지원
        if (principal instanceof String email) {
            log.debug("Extracted token info from SecurityContext (String): {}", email);
            return TokenInfo.ofEmail(email);
        }

        log.error("Unexpected principal type: {}", principal.getClass().getName());
        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    /**
     * 현재 인증된 사용자의 accountId 추출
     *
     * @return 계정 ID
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static Long getCurrentAccountId() {
        return getTokenInfo().accountId();
    }

    /**
     * 현재 인증된 사용자의 email 추출
     *
     * @return 이메일
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static String getCurrentEmail() {
        return getTokenInfo().email();
    }

    /**
     * 현재 인증된 사용자의 role 추출
     *
     * @return 권한
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static String getCurrentRole() {
        return getTokenInfo().role();
    }

    /**
     * 현재 인증된 사용자의 departmentId 추출
     *
     * @return 부서 ID (없으면 null)
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static Long getCurrentDepartmentId() {
        return getTokenInfo().departmentId();
    }
}
