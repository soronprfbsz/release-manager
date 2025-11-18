package com.sb.global.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 - ErrorCode만 전달: 국제화된 메시지 사용 - ErrorCode + 커스텀 메시지: 커스텀 메시지 사용
 */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  /**
   * ErrorCode만 전달 - 국제화된 메시지 사용
   *
   * @param errorCode 에러 코드
   */
  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessageKey()); // 메시지 키 저장
    this.errorCode = errorCode;
  }

  /**
   * ErrorCode + 커스텀 메시지 전달 - 국제화 무시하고 커스텀 메시지 사용
   *
   * @param errorCode 에러 코드
   * @param customMessage 커스텀 메시지
   */
  public BusinessException(ErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
  }
}
