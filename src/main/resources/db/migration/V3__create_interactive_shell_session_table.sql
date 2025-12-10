-- =========================================================
-- Interactive Shell Session Table
-- =========================================================
-- 대화형 SSH 셸 세션 정보 저장
-- 세션은 메모리에서 관리되지만, 감사 및 추적을 위해 DB에 기록됨
-- =========================================================

CREATE TABLE interactive_shell_session (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '세션 ID (PK)',
    shell_session_identifier VARCHAR(100) NOT NULL UNIQUE COMMENT '셸 세션 식별자 (UUID)',
    host VARCHAR(255) NOT NULL COMMENT '호스트 주소',
    port INT NOT NULL COMMENT 'SSH 포트',
    username VARCHAR(100) NOT NULL COMMENT '사용자명',
    status VARCHAR(20) NOT NULL COMMENT '셸 상태 (CONNECTING, CONNECTED, DISCONNECTED, ERROR)',
    owner_email VARCHAR(100) NOT NULL COMMENT '소유자 이메일',
    last_activity_at DATETIME COMMENT '마지막 활동 시각',
    expires_at DATETIME COMMENT '만료 시각',
    command_count INT DEFAULT 0 COMMENT '실행된 명령어 수',
    disconnected_at DATETIME COMMENT '종료 시각',
    error_message VARCHAR(2000) COMMENT '오류 메시지',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    INDEX idx_shell_session_identifier (shell_session_identifier),
    INDEX idx_owner_email (owner_email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='대화형 셸 세션';
