package com.ts.rm.domain.shell.enums;

/**
 * 대화형 셸 세션 상태
 */
public enum ShellStatus {
    /**
     * SSH 연결 중
     */
    CONNECTING,

    /**
     * 셸 연결됨 (명령어 입력 가능)
     */
    CONNECTED,

    /**
     * 셸 연결 종료됨
     */
    DISCONNECTED,

    /**
     * 오류 발생
     */
    ERROR
}
