package com.ts.rm.global.exception;

import com.ts.rm.global.response.ApiResponse;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
      log.warn("Business error (client): [{}] {} - {}", errorCode.getCode(), errorCode.name(),
          message, e);
      return ResponseEntity.status(errorCode.getStatus())
          .body(ApiResponse.fail(errorCode.getCode(), message));
    } else {
      log.error("Business error (server): [{}] {} - {}", errorCode.getCode(), errorCode.name(),
          message, e);
      return ResponseEntity.status(errorCode.getStatus())
          .body(ApiResponse.error(errorCode.getCode(), message));
    }
  }

  // Validation 에러 (클라이언트 에러, 국제화 지원)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ApiResponse.FailDetail>> handleValidationException(
      MethodArgumentNotValidException e, Locale locale) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    String message =
        messageSource.getMessage(ErrorCode.INVALID_INPUT_VALUE.getMessageKey(), null, locale);

    log.error("Validation error: [{}] {} - Fields: {}", ErrorCode.INVALID_INPUT_VALUE.getCode(),
        message, errors, e);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(), message, errors));
  }

  // HTTP 메서드 에러 (클라이언트 에러, 국제화 지원)
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<ApiResponse.FailDetail>> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException e, Locale locale) {
    String message =
        messageSource.getMessage(ErrorCode.METHOD_NOT_ALLOWED.getMessageKey(), null, locale);

    log.error("Method not allowed: [{}] {} - Supported methods: {}",
        ErrorCode.METHOD_NOT_ALLOWED.getCode(), message, e.getSupportedHttpMethods(), e);

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ApiResponse.fail(ErrorCode.METHOD_NOT_ALLOWED.getCode(), message));
  }

  // 인증 실패 (잘못된 인증 정보)
  @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
  public ResponseEntity<ApiResponse<?>> handleBadCredentials(
      org.springframework.security.authentication.BadCredentialsException e, Locale locale) {
    String message =
        messageSource.getMessage(ErrorCode.INVALID_CREDENTIALS.getMessageKey(), null, locale);

    log.warn("Authentication failed: [{}] {} - {}", ErrorCode.INVALID_CREDENTIALS.getCode(),
        message, e.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.fail(ErrorCode.INVALID_CREDENTIALS.getCode(), message));
  }

  // 잘못된 인자 (이메일 중복 등)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e,
      Locale locale) {
    log.warn("Illegal argument: {}", e.getMessage());

    // 메시지에 '이메일'이 포함되면 이메일 중복 에러로 처리
    if (e.getMessage() != null && e.getMessage().contains("이메일")) {
      String message =
          messageSource.getMessage(ErrorCode.DUPLICATE_EMAIL.getMessageKey(), null, locale);
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ApiResponse.fail(ErrorCode.DUPLICATE_EMAIL.getCode(), message));
    }

    // 일반 잘못된 입력으로 처리
    String message =
        messageSource.getMessage(ErrorCode.INVALID_INPUT_VALUE.getMessageKey(), null, locale);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(), message));
  }

  // Path Variable 또는 Request Parameter 타입 불일치 (클라이언트 에러)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<?>> handleTypeMismatch(
      MethodArgumentTypeMismatchException e, Locale locale) {
    String paramName = e.getName();
    String invalidValue = e.getValue() != null ? e.getValue().toString() : "null";

    log.warn("Type mismatch: parameter '{}' = '{}' (expected type: {})",
        paramName, invalidValue, e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown");

    String message =
        messageSource.getMessage(ErrorCode.INVALID_INPUT_VALUE.getMessageKey(), null, locale);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(),
            message + " (파라미터: " + paramName + ", 값: " + invalidValue + ")"));
  }

  // 필수 Request Parameter 누락 (클라이언트 에러)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<?>> handleMissingParam(
      MissingServletRequestParameterException e, Locale locale) {
    String paramName = e.getParameterName();
    String paramType = e.getParameterType();

    log.warn("Missing required parameter: '{}' (type: {})", paramName, paramType);

    String message =
        messageSource.getMessage(ErrorCode.INVALID_INPUT_VALUE.getMessageKey(), null, locale);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(),
            message + " (필수 파라미터 누락: " + paramName + ")"));
  }

  // 필수 Request Header 누락 (클라이언트 에러)
  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiResponse<?>> handleMissingHeader(
      MissingRequestHeaderException e, Locale locale) {
    String headerName = e.getHeaderName();

    log.warn("Missing required header: '{}'", headerName);

    String message =
        messageSource.getMessage(ErrorCode.INVALID_INPUT_VALUE.getMessageKey(), null, locale);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.fail(ErrorCode.INVALID_INPUT_VALUE.getCode(),
            message + " (필수 헤더 누락: " + headerName + ")"));
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
