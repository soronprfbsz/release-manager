package com.ts.rm.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * API 요청/응답 로깅 필터
 * - dev 환경: 상세한 DEBUG 레벨 로깅 (요청/응답 바디 포함)
 * - prod 환경: 비활성화 (성능 최적화)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.api-logging.enabled", havingValue = "true")
public class ApiLoggingFilter extends OncePerRequestFilter {

  private static final int MAX_PAYLOAD_LENGTH = 1000;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Swagger, Actuator 등 정적 리소스는 로깅 제외
    String path = request.getRequestURI();
    if (isSkipPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 요청/응답 본문을 캐싱하는 래퍼로 감싸기
    ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

    long startTime = System.currentTimeMillis();

    try {
      // 실제 요청 처리
      filterChain.doFilter(cachingRequest, cachingResponse);

    } finally {
      long duration = System.currentTimeMillis() - startTime;

      // dev 환경에서는 항상 상세 로깅
      logDetailedRequest(cachingRequest);
      logDetailedResponse(cachingResponse, duration);

      // 응답 본문을 실제 응답으로 복사 (중요!)
      cachingResponse.copyBodyToResponse();
    }
  }

  /**
   * 상세 요청 정보 로깅
   */
  private void logDetailedRequest(ContentCachingRequestWrapper request) {
    StringBuilder sb = new StringBuilder("\n");
    sb.append("========== API Request ==========\n");
    sb.append(String.format("[%s] %s\n", request.getMethod(), request.getRequestURI()));

    // Query String
    String queryString = request.getQueryString();
    if (queryString != null) {
      sb.append(String.format("Query: %s\n", queryString));
    }

    // Request Body
    byte[] content = request.getContentAsByteArray();
    if (content.length > 0) {
      String body = new String(content, StandardCharsets.UTF_8);
      sb.append("Body: ");
      sb.append(body.length() > MAX_PAYLOAD_LENGTH
          ? body.substring(0, MAX_PAYLOAD_LENGTH) + "... (truncated)"
          : body);
      sb.append("\n");
    }

    sb.append("=================================");
    log.debug(sb.toString());
  }

  /**
   * 상세 응답 정보 로깅
   */
  private void logDetailedResponse(ContentCachingResponseWrapper response, long duration) {
    StringBuilder sb = new StringBuilder("\n");
    sb.append("========== API Response =========\n");
    sb.append(String.format("Status: %d\n", response.getStatus()));
    sb.append(String.format("Duration: %dms\n", duration));

    // Response Body
    byte[] content = response.getContentAsByteArray();
    if (content.length > 0) {
      String body = new String(content, StandardCharsets.UTF_8);
      sb.append("Body: ");
      sb.append(body.length() > MAX_PAYLOAD_LENGTH
          ? body.substring(0, MAX_PAYLOAD_LENGTH) + "... (truncated)"
          : body);
      sb.append("\n");
    }

    sb.append("=================================");
    log.debug(sb.toString());
  }

  /**
   * 로깅을 제외할 경로 체크
   */
  private boolean isSkipPath(String path) {
    return path.startsWith("/swagger")
        || path.startsWith("/api-docs")
        || path.startsWith("/actuator")
        || path.startsWith("/v3/api-docs")
        || path.contains("swagger-ui")
        || path.endsWith(".html")
        || path.endsWith(".css")
        || path.endsWith(".js")
        || path.endsWith(".ico");
  }
}
