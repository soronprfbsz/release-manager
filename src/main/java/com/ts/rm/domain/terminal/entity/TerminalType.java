package com.ts.rm.domain.terminal.entity;

/**
 * 터미널 세션 타입
 */
public enum TerminalType {
    /**
     * 스크립트 실행 터미널
     * - 지정된 .sh 파일을 실행
     * - 대화형 입출력 지원
     */
    SCRIPT,

    /**
     * 순수 셸 터미널
     * - /bin/bash 직접 실행
     * - 모든 명령어 실행 가능
     */
    SHELL
}
