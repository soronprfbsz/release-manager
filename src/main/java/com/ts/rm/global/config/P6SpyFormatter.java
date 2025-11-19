package com.ts.rm.global.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * P6Spy SQL 로그 포맷 - 현재 요청 정보 포함
 */
public class P6SpyFormatter implements MessageFormattingStrategy {

  @Override
  public String formatMessage(
      int connectionId,
      String now,
      long elapsed,
      String category,
      String prepared,
      String sql,
      String url) {

    // SQL이 있는 경우만 포맷팅
    if (sql != null && !sql.trim().isEmpty()) {
      String requestInfo = getRequestInfo();

      // 요청 정보가 있으면 포함, 없으면 SQL만 표시
      if (!requestInfo.isEmpty()) {
        return String.format("%s | %dms | %s", requestInfo, elapsed, sql.trim());
      } else {
        return String.format("%dms | %s", elapsed, sql.trim());
      }
    }

    return "";
  }

  /**
   * RequestContextHolder를 통해 현재 HTTP 요청 정보 추출
   */
  private String getRequestInfo() {
    try {
      RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
      if (attributes instanceof ServletRequestAttributes) {
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        return String.format("[%s %s]", method, uri);
      }
    } catch (Exception e) {
      // 요청 컨텍스트가 없는 경우 (배치 작업, 스케줄러 등)
    }
    return "";
  }
}
