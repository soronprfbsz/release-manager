package com.ts.rm.global.logging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * ApiLog Entity
 *
 * <p>API 요청/응답 로그 테이블
 */
@Entity
@Table(name = "api_log", indexes = {
        @Index(name = "idx_al_created_at", columnList = "created_at"),
        @Index(name = "idx_al_request_uri", columnList = "request_uri"),
        @Index(name = "idx_al_http_method", columnList = "http_method"),
        @Index(name = "idx_al_response_status", columnList = "response_status"),
        @Index(name = "idx_al_account_id", columnList = "account_id"),
        @Index(name = "idx_al_request_id", columnList = "request_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    // 요청 정보
    @Column(name = "request_id", nullable = false, length = 36)
    private String requestId;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_uri", nullable = false, length = 500)
    private String requestUri;

    @Column(name = "query_string", length = 2000)
    private String queryString;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "request_content_type", length = 100)
    private String requestContentType;

    // 응답 정보
    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_content_type", length = 100)
    private String responseContentType;

    // 클라이언트 정보
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // 사용자 정보
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_email", length = 100)
    private String accountEmail;

    // 성능 정보
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    // 타임스탬프
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
