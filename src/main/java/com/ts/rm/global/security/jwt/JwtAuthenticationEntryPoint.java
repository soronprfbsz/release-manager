package com.ts.rm.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증되지 않은 요청에 대한 응답 처리
 *
 * <p>Spring Security에서 인증 실패 시 호출되어 401 Unauthorized 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.error("Unauthorized error: {} - URI: {}", authException.getMessage(), request.getRequestURI());

        // 401 Unauthorized 응답
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // ApiResponse 형식으로 응답
        ApiResponse<?> errorResponse = ApiResponse.fail(
                "AU006",
                "인증이 필요합니다. 유효한 Access Token을 제공해주세요."
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
