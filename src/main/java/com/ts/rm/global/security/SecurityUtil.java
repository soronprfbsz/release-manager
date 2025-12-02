package com.ts.rm.global.security;

import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.stream.Collectors;

/**
 * Security 유틸리티
 *
 * <p>Spring Security의 SecurityContext에서 인증 정보를 추출하는 유틸리티
 */
@Slf4j
public final class SecurityUtil {

    private SecurityUtil() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 현재 인증된 사용자의 토큰 정보 추출
     *
     * @return 토큰 정보 (이메일, 이름, 역할)
     * @throws BusinessException 인증 정보가 없거나 유효하지 않은 경우
     */
    public static TokenInfo getTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found in SecurityContext");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        Object principal = authentication.getPrincipal();

        // UserDetails에서 정보 추출
        if (principal instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            log.debug("Extracted token info from SecurityContext: email={}, role={}", email, role);

            return TokenInfo.builder()
                    .email(email)
                    .role(role)
                    .build();
        }

        // String principal (간단한 인증)
        if (principal instanceof String email) {
            log.debug("Extracted token info from SecurityContext (String): {}", email);
            return TokenInfo.ofEmail(email);
        }

        log.error("Unexpected principal type: {}", principal.getClass().getName());
        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }
}
