package com.ts.rm.domain.scheduler.entity;

import com.ts.rm.domain.scheduler.enums.JobExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ScheduleJobHistory Entity
 *
 * <p>스케줄 실행 이력 테이블
 */
@Entity
@Table(name = "schedule_job_history", indexes = {
        @Index(name = "idx_sjh_job_id", columnList = "job_id"),
        @Index(name = "idx_sjh_started_at", columnList = "started_at"),
        @Index(name = "idx_sjh_status", columnList = "status"),
        @Index(name = "idx_sjh_job_started", columnList = "job_id, started_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleJobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private ScheduleJob job;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    // 실행 정보
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    // 결과 정보
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobExecutionStatus status;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // 재시도 정보
    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private Integer attemptNumber = 1;

    /**
     * 실행 완료 처리
     */
    public void complete(JobExecutionStatus status, Integer responseCode, String responseBody, String errorMessage) {
        this.finishedAt = LocalDateTime.now();
        this.executionTimeMs = java.time.Duration.between(this.startedAt, this.finishedAt).toMillis();
        this.status = status;
        this.responseCode = responseCode;
        this.responseBody = truncate(responseBody, 10000);
        this.errorMessage = truncate(errorMessage, 2000);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
