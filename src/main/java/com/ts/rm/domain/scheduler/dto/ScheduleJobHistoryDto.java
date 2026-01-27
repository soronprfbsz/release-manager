package com.ts.rm.domain.scheduler.dto;

import com.ts.rm.domain.scheduler.enums.JobExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * ScheduleJobHistory DTO
 */
public final class ScheduleJobHistoryDto {

    private ScheduleJobHistoryDto() {
    }

    /**
     * 실행 이력 응답
     */
    @Schema(description = "스케줄 실행 이력 응답")
    public record Response(
            @Schema(description = "이력 ID", example = "1")
            Long historyId,

            @Schema(description = "작업 ID", example = "1")
            Long jobId,

            @Schema(description = "작업명", example = "daily-backup")
            String jobName,

            @Schema(description = "시작 시각")
            LocalDateTime startedAt,

            @Schema(description = "종료 시각")
            LocalDateTime finishedAt,

            @Schema(description = "실행 시간 (밀리초)", example = "1234")
            Long executionTimeMs,

            @Schema(description = "실행 상태", example = "SUCCESS")
            JobExecutionStatus status,

            @Schema(description = "HTTP 응답 코드", example = "200")
            Integer responseCode,

            @Schema(description = "응답 본문")
            String responseBody,

            @Schema(description = "에러 메시지")
            String errorMessage,

            @Schema(description = "시도 횟수", example = "1")
            Integer attemptNumber
    ) {

    }

    /**
     * 실행 이력 목록 응답 (간략)
     */
    @Schema(description = "스케줄 실행 이력 목록 응답")
    public record ListResponse(
            @Schema(description = "이력 ID", example = "1")
            Long historyId,

            @Schema(description = "작업명", example = "daily-backup")
            String jobName,

            @Schema(description = "시작 시각")
            LocalDateTime startedAt,

            @Schema(description = "실행 시간 (밀리초)", example = "1234")
            Long executionTimeMs,

            @Schema(description = "실행 상태", example = "SUCCESS")
            JobExecutionStatus status,

            @Schema(description = "HTTP 응답 코드", example = "200")
            Integer responseCode,

            @Schema(description = "시도 횟수", example = "1")
            Integer attemptNumber
    ) {

    }
}
