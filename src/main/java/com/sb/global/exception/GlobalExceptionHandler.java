package com.sb.global.exception;

import com.sb.global.common.ApiResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  // 비즈니스 예외 (자동 status 분류: fail/error, 국제화 지원)
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e,
      Locale locale) {
    ErrorCode errorCode = e.getErrorCode();
    String responseStatus = errorCode.getResponseStatus();

    // 커스텀 메시지인지 messageKey인지 판단
    String message;
    if (e.getMessage() != null && e.getMessage().equals(errorCode.getMessageKey())) {
      // messageKey → MessageSource에서 국제화된 메시지 조회
      message = messageSource.getMessage(errorCode.getMessageKey(), null, locale);
    } else {
      // 커스텀 메시지 → 그대로 사용
      message = e.getMessage();
    }

    // ErrorCode의 HttpStatus에 따라 fail(4xx) 또는 error(5xx) 자동 분류
    if ("fail".equals(responseStatus)) {
      log.warn("Business error (client): {}", message);
      return ResponseEntity.status(errorCode.getStatus())
          .body(ApiResponse.fail(errorCode.getCode(), message));
    } else {
      log.error("Business error (server): {}", message);
      return ResponseEntity.status(errorCode.getStatus())
          .body(ApiResponse.error(errorCode.getCode(), message));
    }
  }

  // Validation 에러 (클라이언트 에러, 국제화 지원)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ApiResponse.FailDetail>> handleValidationException(
      MethodArgumentNotValidException e, Locale locale) {
    log.warn("Validation error: {}", e.getMessage());

    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    String message =
        messageSource.getMessage(ErrorCode.INVALID_INPUT_VALUE.getMessageKey(), null, locale);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(), message, errors));
  }

  // HTTP 메서드 에러 (클라이언트 에러, 국제화 지원)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<ApiResponse.FailDetail>> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException e, Locale locale) {
    log.warn("Method not allowed: {}", e.getMessage());

    String message =
        messageSource.getMessage(ErrorCode.METHOD_NOT_ALLOWED.getMessageKey(), null, locale);

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ApiResponse.fail(ErrorCode.METHOD_NOT_ALLOWED.getCode(), message));
  }

  // 예상치 못한 서버 에러 (국제화 지원)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<ApiResponse.ErrorDetail>> handleInternalError(Exception e,
      Locale locale) {
    log.error("Unexpected error occurred", e);

    String message =
        messageSource.getMessage(ErrorCode.INTERNAL_SERVER_ERROR.getMessageKey(), null, locale);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), message));
  }
}
