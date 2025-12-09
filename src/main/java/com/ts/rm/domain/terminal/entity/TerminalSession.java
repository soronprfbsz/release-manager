package com.ts.rm.domain.terminal.entity;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 터미널 세션 정보
 * <p>
 * 메모리에만 존재하며 DB에 저장되지 않습니다.
 * 각 세션은 독립된 Bash 프로세스를 실행하고 I/O 스트림을 관리합니다.
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminalSession {

    /**
     * 세션 고유 ID (예: term_20251209_130012_abc123)
     */
    private String sessionId;

    /**
     * 세션 타입 (SCRIPT: 스크립트 실행, SHELL: 순수 셸)
     */
    private TerminalType type;

    /**
     * 세션 소유자 이메일
     */
    private String ownerEmail;

    /**
     * 실행 중인 스크립트 경로 (SCRIPT 타입) 또는 셸 경로 (SHELL 타입: "/bin/bash")
     */
    private String scriptPath;

    /**
     * 작업 디렉토리
     */
    private String workingDirectory;

    /**
     * 실행 중인 프로세스
     */
    private Process process;

    /**
     * 표준 출력 스트림 (프로세스 → 클라이언트)
     */
    private InputStream stdout;

    /**
     * 표준 에러 스트림 (프로세스 → 클라이언트)
     */
    private InputStream stderr;

    /**
     * 표준 입력 스트림 (클라이언트 → 프로세스)
     */
    private OutputStream stdin;

    /**
     * 세션 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 마지막 활동 시각
     */
    private LocalDateTime lastActivityAt;

    /**
     * 세션 만료 시각
     */
    private LocalDateTime expiresAt;

    /**
     * 프로세스 실행 여부
     */
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    /**
     * 마지막 활동 시각 업데이트
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * 세션 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
