package com.ts.rm.domain.scheduler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * ScheduleJob DTO
 */
public final class ScheduleJobDto {

    private ScheduleJobDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 스케줄 작업 생성 요청
     */
    @Builder
    @Schema(description = "스케줄 작업 생성 요청")
    public record CreateRequest(
            @Schema(description = "작업명 (고유)", example = "daily-backup")
            @NotBlank(message = "작업명은 필수입니다")
            @Size(max = 100, message = "작업명은 100자 이하여야 합니다")
            String jobName,

            @Schema(description = "작업 그룹", example = "MAINTENANCE")
            @Size(max = 50, message = "작업 그룹은 50자 이하여야 합니다")
            String jobGroup,

            @Schema(description = "작업 설명", example = "매일 새벽 3시 데이터베이스 백업")
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description,

            @Schema(description = "호출할 API URL", example = "http://localhost:8081/api/maintenance/backup")
            @NotBlank(message = "API URL은 필수입니다")
            @Size(max = 500, message = "API URL은 500자 이하여야 합니다")
            String apiUrl,

            @Schema(description = "HTTP 메서드", example = "POST", allowableValues = {"GET", "POST", "PUT", "DELETE"})
            String httpMethod,

            @Schema(description = "요청 본문 (JSON)", example = "{\"retention_days\": 30}")
            String requestBody,

            @Schema(description = "요청 헤더 (JSON)", example = "{\"X-API-Key\": \"secret\"}")
            String requestHeaders,

            @Schema(description = "Cron 표현식", example = "0 0 3 * * *")
            @NotBlank(message = "Cron 표현식은 필수입니다")
            @Size(max = 100, message = "Cron 표현식은 100자 이하여야 합니다")
            String cronExpression,

            @Schema(description = "타임존", example = "Asia/Seoul")
            String timezone,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled,

            @Schema(description = "타임아웃 (초)", example = "30")
            Integer timeoutSeconds,

            @Schema(description = "재시도 횟수", example = "3")
            Integer retryCount,

            @Schema(description = "재시도 간격 (초)", example = "5")
            Integer retryDelaySeconds
    ) {

    }

    /**
     * 스케줄 작업 수정 요청
     */
    @Builder
    @Schema(description = "스케줄 작업 수정 요청")
    public record UpdateRequest(
            @Schema(description = "작업 그룹", example = "MAINTENANCE")
            @Size(max = 50, message = "작업 그룹은 50자 이하여야 합니다")
            String jobGroup,

            @Schema(description = "작업 설명", example = "매일 새벽 3시 데이터베이스 백업")
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description,

            @Schema(description = "호출할 API URL", example = "http://localhost:8081/api/maintenance/backup")
            @Size(max = 500, message = "API URL은 500자 이하여야 합니다")
            String apiUrl,

            @Schema(description = "HTTP 메서드", example = "POST")
            String httpMethod,

            @Schema(description = "요청 본문 (JSON)", example = "{\"retention_days\": 30}")
            String requestBody,

            @Schema(description = "요청 헤더 (JSON)", example = "{\"X-API-Key\": \"secret\"}")
            String requestHeaders,

            @Schema(description = "Cron 표현식", example = "0 0 3 * * *")
            @Size(max = 100, message = "Cron 표현식은 100자 이하여야 합니다")
            String cronExpression,

            @Schema(description = "타임존", example = "Asia/Seoul")
            String timezone,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled,

            @Schema(description = "타임아웃 (초)", example = "30")
            Integer timeoutSeconds,

            @Schema(description = "재시도 횟수", example = "3")
            Integer retryCount,

            @Schema(description = "재시도 간격 (초)", example = "5")
            Integer retryDelaySeconds
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 스케줄 작업 상세 응답
     */
    @Schema(description = "스케줄 작업 상세 응답")
    public record Response(
            @Schema(description = "작업 ID", example = "1")
            Long jobId,

            @Schema(description = "작업명", example = "daily-backup")
            String jobName,

            @Schema(description = "작업 그룹", example = "MAINTENANCE")
            String jobGroup,

            @Schema(description = "작업 설명", example = "매일 새벽 3시 데이터베이스 백업")
            String description,

            @Schema(description = "호출할 API URL", example = "http://localhost:8081/api/maintenance/backup")
            String apiUrl,

            @Schema(description = "HTTP 메서드", example = "POST")
            String httpMethod,

            @Schema(description = "요청 본문 (JSON)")
            String requestBody,

            @Schema(description = "요청 헤더 (JSON)")
            String requestHeaders,

            @Schema(description = "Cron 표현식", example = "0 0 3 * * *")
            String cronExpression,

            @Schema(description = "타임존", example = "Asia/Seoul")
            String timezone,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled,

            @Schema(description = "타임아웃 (초)", example = "30")
            Integer timeoutSeconds,

            @Schema(description = "재시도 횟수", example = "3")
            Integer retryCount,

            @Schema(description = "재시도 간격 (초)", example = "5")
            Integer retryDelaySeconds,

            @Schema(description = "마지막 실행 시각")
            LocalDateTime lastExecutedAt,

            @Schema(description = "다음 실행 예정 시각")
            LocalDateTime nextExecutionAt,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }

    /**
     * 스케줄 작업 목록 응답
     */
    @Schema(description = "스케줄 작업 목록 응답")
    public record ListResponse(
            @Schema(description = "작업 ID", example = "1")
            Long jobId,

            @Schema(description = "작업명", example = "daily-backup")
            String jobName,

            @Schema(description = "작업 그룹", example = "MAINTENANCE")
            String jobGroup,

            @Schema(description = "작업 설명", example = "매일 새벽 3시 데이터베이스 백업")
            String description,

            @Schema(description = "API URL", example = "http://localhost:8081/api/maintenance/backup")
            String apiUrl,

            @Schema(description = "HTTP 메서드", example = "POST")
            String httpMethod,

            @Schema(description = "Cron 표현식", example = "0 0 3 * * *")
            String cronExpression,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled,

            @Schema(description = "마지막 실행 시각")
            LocalDateTime lastExecutedAt,

            @Schema(description = "다음 실행 예정 시각")
            LocalDateTime nextExecutionAt
    ) {

    }
}
