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
    if (isSkipPath(request)) {
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

      // 요청과 응답을 하나의 블록으로 로깅
      logApiTransaction(cachingRequest, cachingResponse, duration);

      // 응답 본문을 실제 응답으로 복사 (중요!)
      cachingResponse.copyBodyToResponse();
    }
  }

  /**
   * API 트랜잭션 전체를 하나의 블록으로 로깅
   * 요청부터 응답까지 모든 정보를 한 번에 출력하여 가독성 향상
   */
  private void logApiTransaction(ContentCachingRequestWrapper request,
                                   ContentCachingResponseWrapper response,
                                   long duration) {
    StringBuilder sb = new StringBuilder("\n");

    String method = request.getMethod();
    String uri = request.getRequestURI();

    // 헤더 시작
    sb.append("╔════════════════════════════════════════════════════════════════\n");
    sb.append(String.format("║ API Request: [%s] %s\n", method, uri));
    sb.append("╠════════════════════════════════════════════════════════════════\n");

    // Query String
    String queryString = request.getQueryString();
    if (queryString != null) {
      sb.append(String.format("║ Query: %s\n", queryString));
    }

    // Request Body
    byte[] requestContent = request.getContentAsByteArray();
    if (requestContent.length > 0) {
      String requestBody = new String(requestContent, StandardCharsets.UTF_8);
      sb.append("║ Request Body: ");
      sb.append(requestBody.length() > MAX_PAYLOAD_LENGTH
          ? requestBody.substring(0, MAX_PAYLOAD_LENGTH) + "... (truncated)"
          : requestBody);
      sb.append("\n");
    }

    // 구분선
    sb.append("╠════════════════════════════════════════════════════════════════\n");
    sb.append(String.format("║ API Response: [%s] %s\n", method, uri));
    sb.append("╠════════════════════════════════════════════════════════════════\n");
    sb.append(String.format("║ Status: %d\n", response.getStatus()));
    sb.append(String.format("║ Duration: %dms\n", duration));

    // Response Body
    byte[] responseContent = response.getContentAsByteArray();
    if (responseContent.length > 0) {
      String responseBody = new String(responseContent, StandardCharsets.UTF_8);
      sb.append("║ Response Body: ");
      sb.append(responseBody.length() > MAX_PAYLOAD_LENGTH
          ? responseBody.substring(0, MAX_PAYLOAD_LENGTH) + "... (truncated)"
          : responseBody);
      sb.append("\n");
    }

    // 하단 마감
    sb.append("╚════════════════════════════════════════════════════════════════");

    log.debug(sb.toString());
  }

  /**
   * 로깅을 제외할 경로 체크
   *
   * <p>제외 대상:
   * <ul>
   *   <li>정적 리소스: Swagger, API Docs, Actuator</li>
   *   <li>파일 다운로드: 대용량 응답 스트리밍 (ContentCachingResponseWrapper OOM 방지)</li>
   *   <li>파일 업로드: 대용량 요청 (ContentCachingRequestWrapper OOM 방지)</li>
   * </ul>
   */
  private boolean isSkipPath(HttpServletRequest request) {
    String path = request.getRequestURI();
    String method = request.getMethod();

    // 정적 리소스 제외
    if (path.startsWith("/swagger")
        || path.startsWith("/api-docs")
        || path.startsWith("/actuator")
        || path.startsWith("/v3/api-docs")
        || path.contains("swagger-ui")
        || path.endsWith(".html")
        || path.endsWith(".css")
        || path.endsWith(".js")
        || path.endsWith(".ico")) {
      return true;
    }

    // 파일 다운로드 엔드포인트 제외 (대용량 응답 스트리밍 - OOM 방지)
    if (path.endsWith("/download") || path.contains("/download/")) {
      return true;
    }

    // 파일 업로드 엔드포인트 제외 (대용량 요청 - OOM 방지)
    // POST /api/releases/standard/versions, POST /api/releases/custom/versions
    if ("POST".equalsIgnoreCase(method) && path.endsWith("/versions")) {
      return true;
    }

    return false;
  }
}
