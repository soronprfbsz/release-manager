package com.ts.rm.domain.shell.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import com.ts.rm.domain.shell.enums.ShellStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 대화형 셸 세션 엔티티
 * <p>
 * 대화형 SSH 셸 세션 정보를 저장합니다.
 * 세션은 메모리에서 관리되지만, 감사 및 추적을 위해 DB에 기록됩니다.
 * </p>
 */
@Entity
@Table(name = "interactive_shell_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractiveShellSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    /**
     * 셸 세션 식별자 (UUID 형식)
     */
    @Column(name = "shell_session_identifier", nullable = false, unique = true, length = 100)
    private String shellSessionIdentifier;

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
     * 셸 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShellStatus status;

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
     * 실행된 명령어 수
     */
    @Column(name = "command_count")
    private Integer commandCount;

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
