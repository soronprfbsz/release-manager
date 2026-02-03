package com.ts.rm.global.logging.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ts.rm.global.logging.entity.ApiLog;
import com.ts.rm.global.logging.service.ApiLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * API Log Persistence Filter
 *
 * <p>모든 API 요청/응답을 DB에 저장하는 필터
 * <p>파일 업로드/다운로드 등 바이너리 데이터는 제외
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiLogPersistenceFilter extends OncePerRequestFilter {

    private final ApiLogService apiLogService;
    private final ObjectMapper objectMapper;

    /**
     * 로깅 제외 경로 (prefix 매칭)
     */
    private static final List<String> EXCLUDE_PATHS = List.of(
            "/actuator",
            "/swagger",
            "/api-docs",
            "/favicon.ico",
            "/ws",
            "/webjars",
            "/publishing"
    );

    /**
     * 로깅 제외 경로 패턴 (suffix 매칭) - 대용량 파일 다운로드
     */
    private static final List<String> EXCLUDE_PATH_SUFFIXES = List.of(
            "/download",
            "/zip-download"
    );

    /**
     * 로깅 제외 Content-Type (바이너리 데이터)
     */
    private static final Set<String> BINARY_CONTENT_TYPES = Set.of(
            "multipart/form-data",
            "application/octet-stream",
            "image/",
            "video/",
            "audio/",
            "application/pdf",
            "application/zip",
            "application/x-zip",
            "application/x-gzip"
    );

    /**
     * 마스킹 대상 필드명
     */
    private static final Set<String> MASK_FIELDS = Set.of(
            "password", "passwd", "pwd",
            "token", "accessToken", "refreshToken",
            "secret", "secretKey",
            "credential", "credentials",
            "authorization"
    );

    /**
     * 본문 최대 크기 (10KB)
     */
    private static final int MAX_BODY_SIZE = 10 * 1024;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 제외 경로 체크
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청/응답 래퍼 생성
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            // 실제 요청 처리
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // 로그 저장
            try {
                saveApiLog(wrappedRequest, wrappedResponse, requestId, startTime);
            } catch (Exception e) {
                log.warn("API 로그 저장 중 오류: {}", e.getMessage());
            }

            // 응답 본문 복사 (필수!)
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * 제외 경로 여부 확인
     */
    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // prefix 매칭 (정적 경로)
        if (EXCLUDE_PATHS.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        // suffix 매칭 (대용량 파일 다운로드 API)
        return EXCLUDE_PATH_SUFFIXES.stream().anyMatch(uri::endsWith);
    }

    /**
     * API 로그 저장
     */
    private void saveApiLog(ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            String requestId, long startTime) {

        String requestBody = extractRequestBody(request);
        String responseBody = extractResponseBody(response);

        // 사용자 정보 추출
        Long accountId = null;
        String accountEmail = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            accountEmail = auth.getName();
            // accountId는 Principal에서 추출
            if (auth.getPrincipal() instanceof com.ts.rm.global.security.AccountUserDetails userDetails) {
                accountId = userDetails.getAccountId();
                accountEmail = userDetails.getEmail();
            }
        }

        ApiLog apiLog = ApiLog.builder()
                .requestId(requestId)
                .httpMethod(request.getMethod())
                .requestUri(request.getRequestURI())
                .queryString(truncate(request.getQueryString(), 2000))
                .requestBody(requestBody)
                .requestContentType(request.getContentType())
                .responseStatus(response.getStatus())
                .responseBody(responseBody)
                .responseContentType(response.getContentType())
                .clientIp(getClientIp(request))
                .userAgent(truncate(request.getHeader("User-Agent"), 500))
                .accountId(accountId)
                .accountEmail(accountEmail)
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .build();

        apiLogService.saveAsync(apiLog);
    }

    /**
     * 요청 본문 추출
     */
    private String extractRequestBody(ContentCachingRequestWrapper request) {
        String contentType = request.getContentType();

        // 바이너리 Content-Type 체크
        if (isBinaryContentType(contentType)) {
            return "[BINARY: " + request.getContentLength() + " bytes]";
        }

        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        body = maskSensitiveFields(body, contentType);
        return truncate(body, MAX_BODY_SIZE);
    }

    /**
     * 응답 본문 추출
     */
    private String extractResponseBody(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();

        // 바이너리 Content-Type 체크
        if (isBinaryContentType(contentType)) {
            return "[BINARY: " + response.getContentSize() + " bytes]";
        }

        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        body = maskSensitiveFields(body, contentType);
        return truncate(body, MAX_BODY_SIZE);
    }

    /**
     * 바이너리 Content-Type 여부 확인
     */
    private boolean isBinaryContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String lowerContentType = contentType.toLowerCase();
        return BINARY_CONTENT_TYPES.stream()
                .anyMatch(lowerContentType::contains);
    }

    /**
     * 민감 정보 마스킹
     */
    private String maskSensitiveFields(String body, String contentType) {
        if (body == null || body.isBlank()) {
            return body;
        }

        // JSON인 경우만 마스킹 처리
        if (contentType != null && contentType.contains("application/json")) {
            try {
                JsonNode jsonNode = objectMapper.readTree(body);
                if (jsonNode.isObject()) {
                    maskJsonFields((ObjectNode) jsonNode);
                    return objectMapper.writeValueAsString(jsonNode);
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 원본 반환
                log.trace("JSON 마스킹 실패 (원본 유지): {}", e.getMessage());
            }
        }

        return body;
    }

    /**
     * JSON 필드 마스킹 (재귀)
     */
    private void maskJsonFields(ObjectNode node) {
        node.fieldNames().forEachRemaining(fieldName -> {
            if (MASK_FIELDS.stream().anyMatch(f -> fieldName.toLowerCase().contains(f))) {
                node.put(fieldName, "****");
            } else {
                JsonNode childNode = node.get(fieldName);
                if (childNode != null && childNode.isObject()) {
                    maskJsonFields((ObjectNode) childNode);
                }
            }
        });
    }

    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 콤마로 구분된 IP 목록일 수 있음
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 문자열 자르기
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
