package com.ts.rm.domain.terminal.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import com.ts.rm.domain.terminal.enums.TerminalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 터미널 엔티티
 * <p>
 * 웹 터미널 세션 정보를 저장합니다.
 * 세션은 메모리에서 관리되지만, 감사 및 추적을 위해 DB에 기록됩니다.
 * </p>
 */
@Entity
@Table(name = "terminal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terminal extends BaseEntity {

    /**
     * 터미널 ID (세션 단위, UUID 형식)
     */
    @Id
    @Column(name = "terminal_id", nullable = false, length = 100)
    private String terminalId;

    /**
     * 호스트 주소
     */
    @Column(name = "host", nullable = false, length = 255)
    private String host;

    /**
     * SSH 포트
     */
    @Column(name = "port", nullable = false)
    private Integer port;

    /**
     * 사용자명
     */
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /**
     * 터미널 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TerminalStatus status;

    /**
     * 소유자 이메일
     */
    @Column(name = "owner_email", nullable = false, length = 100)
    private String ownerEmail;

    /**
     * 마지막 활동 시각
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    /**
     * 만료 시각
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 종료 시각
     */
    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    /**
     * 오류 메시지
     */
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
