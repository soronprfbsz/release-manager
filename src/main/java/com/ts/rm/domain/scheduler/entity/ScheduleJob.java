package com.ts.rm.domain.scheduler.entity;

import com.ts.rm.domain.account.entity.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * ScheduleJob Entity
 *
 * <p>스케줄 작업 정의 테이블
 */
@Entity
@Table(name = "schedule_job", indexes = {
        @Index(name = "idx_sj_job_name", columnList = "job_name", unique = true),
        @Index(name = "idx_sj_job_group", columnList = "job_group"),
        @Index(name = "idx_sj_is_enabled", columnList = "is_enabled"),
        @Index(name = "idx_sj_next_execution", columnList = "next_execution_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "job_name", nullable = false, length = 100, unique = true)
    private String jobName;

    @Column(name = "job_group", nullable = false, length = 50)
    @Builder.Default
    private String jobGroup = "DEFAULT";

    @Column(name = "description", length = 500)
    private String description;

    // API 호출 정보
    @Column(name = "api_url", nullable = false, length = 500)
    private String apiUrl;

    @Column(name = "http_method", nullable = false, length = 10)
    @Builder.Default
    private String httpMethod = "POST";

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    // 스케줄 설정
    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Asia/Seoul";

    // 실행 설정
    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "timeout_seconds", nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 30;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "retry_delay_seconds", nullable = false)
    @Builder.Default
    private Integer retryDelaySeconds = 5;

    // 메타 정보
    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "next_execution_at")
    private LocalDateTime nextExecutionAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account creator;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
