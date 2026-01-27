package com.ts.rm.global.logging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * ApiLog DTO
 *
 * <p>API 로그 조회 응답 DTO
 */
public final class ApiLogDto {

    private ApiLogDto() {
    }

    /**
     * API 로그 상세 응답
     */
    @Schema(description = "API 로그 상세 응답")
    public record Response(
            @Schema(description = "로그 ID", example = "1")
            Long logId,

            @Schema(description = "요청 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            String requestId,

            @Schema(description = "HTTP 메서드", example = "POST")
            String httpMethod,

            @Schema(description = "요청 URI", example = "/api/auth/login")
            String requestUri,

            @Schema(description = "쿼리 스트링", example = "page=0&size=20")
            String queryString,

            @Schema(description = "요청 본문")
            String requestBody,

            @Schema(description = "요청 Content-Type", example = "application/json")
            String requestContentType,

            @Schema(description = "응답 상태 코드", example = "200")
            Integer responseStatus,

            @Schema(description = "응답 본문")
            String responseBody,

            @Schema(description = "응답 Content-Type", example = "application/json")
            String responseContentType,

            @Schema(description = "클라이언트 IP", example = "192.168.1.1")
            String clientIp,

            @Schema(description = "User-Agent")
            String userAgent,

            @Schema(description = "계정 ID", example = "1")
            Long accountId,

            @Schema(description = "계정 이메일", example = "admin@example.com")
            String accountEmail,

            @Schema(description = "계정 이름", example = "홍길동")
            String accountName,

            @Schema(description = "아바타 스타일", example = "adventurer")
            String avatarStyle,

            @Schema(description = "아바타 시드", example = "abc123")
            String avatarSeed,

            @Schema(description = "실행 시간 (ms)", example = "150")
            Long executionTimeMs,

            @Schema(description = "생성일시")
            LocalDateTime createdAt
    ) {
    }

    /**
     * API 로그 목록 응답 (간략)
     */
    @Schema(description = "API 로그 목록 응답")
    public record ListResponse(
            @Schema(description = "로그 ID", example = "1")
            Long logId,

            @Schema(description = "요청 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            String requestId,

            @Schema(description = "HTTP 메서드", example = "POST")
            String httpMethod,

            @Schema(description = "요청 URI", example = "/api/auth/login")
            String requestUri,

            @Schema(description = "응답 상태 코드", example = "200")
            Integer responseStatus,

            @Schema(description = "클라이언트 IP", example = "192.168.1.1")
            String clientIp,

            @Schema(description = "계정 ID", example = "1")
            Long accountId,

            @Schema(description = "계정 이메일", example = "admin@example.com")
            String accountEmail,

            @Schema(description = "계정 이름", example = "홍길동")
            String accountName,

            @Schema(description = "아바타 스타일", example = "adventurer")
            String avatarStyle,

            @Schema(description = "아바타 시드", example = "abc123")
            String avatarSeed,

            @Schema(description = "실행 시간 (ms)", example = "150")
            Long executionTimeMs,

            @Schema(description = "생성일시")
            LocalDateTime createdAt
    ) {
    }

    /**
     * API 로그 검색 조건
     */
    @Schema(description = "API 로그 검색 조건")
    public record SearchCondition(
            @Schema(description = "통합 검색 키워드 (요청 URI, 계정 이메일, 계정 이름 OR 검색)", example = "admin")
            String keyword,

            @Schema(description = "HTTP 메서드", example = "GET")
            String httpMethod,

            @Schema(description = "응답 상태 코드", example = "200")
            Integer responseStatus,

            @Schema(description = "클라이언트 IP", example = "192.168.1.1")
            String clientIp,

            @Schema(description = "시작일시")
            LocalDateTime startDate,

            @Schema(description = "종료일시")
            LocalDateTime endDate
    ) {
        public SearchCondition {
            // 기본값 처리를 위한 컴팩트 생성자
        }

        public static SearchCondition empty() {
            return new SearchCondition(null, null, null, null, null, null);
        }
    }
}
