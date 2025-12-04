package com.ts.rm.domain.job.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ts.rm.domain.job.enums.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작업 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "작업 응답")
public class JobResponse {

    @Schema(description = "작업 ID", example = "backup_20251203_171500")
    private String jobId;

    @Schema(description = "작업 상태", example = "RUNNING")
    private JobStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "시작 시간", example = "2025-12-03 17:15:00")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "종료 시간", example = "2025-12-03 17:20:00")
    private LocalDateTime endTime;

    @Schema(description = "메시지", example = "작업이 시작되었습니다.")
    private String message;

    @Schema(description = "파일명", example = "backup_20251203_171500.sql")
    private String fileName;

    @Schema(description = "파일 크기 (바이트)", example = "131072000")
    private Long fileSize;

    @Schema(description = "로그 파일 경로", example = "logs/backup_mariadb_20251203_171500.log")
    private String logFile;

    @Schema(description = "오류 메시지")
    private String errorMessage;

    /**
     * 실행 중 상태로 생성
     */
    public static JobResponse createRunning(String jobId, String fileName, String logFile) {
        return JobResponse.builder()
                .jobId(jobId)
                .status(JobStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .message("작업이 시작되었습니다.")
                .fileName(fileName)
                .logFile(logFile)
                .build();
    }

    /**
     * 성공 상태로 생성
     */
    public static JobResponse createSuccess(String jobId, String fileName, Long fileSize, String logFile) {
        return JobResponse.builder()
                .jobId(jobId)
                .status(JobStatus.SUCCESS)
                .endTime(LocalDateTime.now())
                .message("작업이 성공적으로 완료되었습니다.")
                .fileName(fileName)
                .fileSize(fileSize)
                .logFile(logFile)
                .build();
    }

    /**
     * 실패 상태로 생성
     */
    public static JobResponse createFailed(String jobId, String fileName, String logFile,
            String errorMessage) {
        return JobResponse.builder()
                .jobId(jobId)
                .status(JobStatus.FAILED)
                .endTime(LocalDateTime.now())
                .message("작업이 실패했습니다.")
                .fileName(fileName)
                .logFile(logFile)
                .errorMessage(errorMessage)
                .build();
    }
}
