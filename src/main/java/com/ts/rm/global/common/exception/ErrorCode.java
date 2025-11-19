package com.ts.rm.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 관리 - FAIL: 클라이언트 에러 (4xx) - 잘못된 요청, 인증/인가 실패 등 - ERROR: 서버 에러 (5xx) - 서버 내부
 * 처리 오류 - 메시지는 messages_{locale}.properties에서 국제화 지원
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // ========================================
  // FAIL - 클라이언트 에러 (4xx)
  // ========================================

  // Common - 공통
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "error.common.invalid_input"),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "error.common.method_not_allowed"),
  DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "error.common.data_not_found"),
  DATA_CONFLICT(HttpStatus.CONFLICT, "C005", "error.common.data_conflict"),

  // Account - 계정
  ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "error.account.not_found"),
  ACCOUNT_EMAIL_CONFLICT(HttpStatus.CONFLICT, "A002", "error.account.email_conflict"),

  // ========================================
  // ERROR - 서버 에러 (5xx)
  // ========================================

  // Common - 공통
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002",
      "error.common.internal_server");

  private final HttpStatus status;
  private final String code;
  private final String messageKey; // 국제화 메시지 키

  /**
   * HTTP 상태 코드를 기반으로 응답 status 타입 반환
   *
   * @return "fail" (4xx 클라이언트 에러) 또는 "error" (5xx 서버 에러)
   */
  public String getResponseStatus() {
    if (status.is4xxClientError()) {
      return "fail";
    } else if (status.is5xxServerError()) {
      return "error";
    }
    return "error"; // 기본값
  }
}
