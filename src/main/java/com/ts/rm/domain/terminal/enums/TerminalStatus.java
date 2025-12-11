package com.ts.rm.domain.terminal.enums;

/**
 * 터미널 세션 상태
 */
public enum TerminalStatus {
    /**
     * SSH 연결 중
     */
    CONNECTING,

    /**
     * 터미널 연결됨 (명령어 입력 가능)
     */
    CONNECTED,

    /**
     * 터미널 연결 종료됨
     */
    DISCONNECTED,

    /**
     * 오류 발생
     */
    ERROR
}
