package com.ts.rm.global.filter;

import com.ts.rm.domain.common.service.CustomUserDetailsService;
import com.ts.rm.global.security.AccountUserDetails;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 인증 필터
 * - HTTP 요청에서 JWT 토큰 추출 및 검증
 * - 유효한 토큰인 경우 SecurityContext에 인증 정보 저장
 * - accountId 기반 사용자 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. HTTP 요청에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);

            // 2. 토큰 검증 및 인증 정보 설정
            if (StringUtils.hasText(token)) {
                if (jwtTokenProvider.validateToken(token)) {
                    // 유효한 토큰 → accountId 추출 후 사용자 정보 로드
                    Long accountId = jwtTokenProvider.getAccountId(token);
                    AccountUserDetails userDetails = userDetailsService.loadUserByAccountId(accountId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Set Authentication to security context for accountId: '{}', uri: {}",
                            accountId, request.getRequestURI());
                } else {
                    // 토큰은 있지만 유효하지 않음 (만료 또는 변조)
                    log.warn("Invalid or expired JWT token for uri: {}", request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 또는 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
