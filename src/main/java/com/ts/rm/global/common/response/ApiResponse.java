package com.ts.rm.global.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 공통 응답")
public record ApiResponse<T>(
        @Schema(description = "응답 상태 (success | fail | error)") String status,
        @Schema(description = "응답 데이터") T data) {

    // 성공
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data);
    }

    // 실패 (클라이언트 에러)
    public static ApiResponse<FailDetail> fail(String code, String message,
            Object detail) {
        return new ApiResponse<>("fail", FailDetail.of(code, message, detail));
    }

    public static ApiResponse<FailDetail> fail(String code, String message) {
        return new ApiResponse<>("fail", FailDetail.of(code, message, null));
    }

    // 에러 (서버 처리 에러)
    public static ApiResponse<ErrorDetail> error(String code, String message,
            Object detail) {
        return new ApiResponse<>("error",
                ErrorDetail.of(code, message, detail));
    }

    public static ApiResponse<ErrorDetail> error(String code, String message) {
        return new ApiResponse<>("error", ErrorDetail.of(code, message, null));
    }

    @Schema(description = "실패 상세 정보 (클라이언트 요청 실패)")
    public record FailDetail(String code, String message, Object detail) {

        public static FailDetail of(String code, String message,
                Object detail) {
            return new FailDetail(code, message, detail);
        }
    }

    @Schema(description = "에러 상세 정보 (서버 에러)")
    public record ErrorDetail(String code, String message, Object detail) {

        public static ErrorDetail of(String code, String message,
                Object detail) {
            return new ErrorDetail(code, message, detail);
        }
    }
}
