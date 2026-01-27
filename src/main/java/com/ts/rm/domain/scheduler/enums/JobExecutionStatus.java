package com.ts.rm.domain.scheduler.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스케줄 작업 실행 상태
 */
@Getter
@RequiredArgsConstructor
public enum JobExecutionStatus {

    RUNNING("실행 중"),
    SUCCESS("성공"),
    FAILED("실패"),
    TIMEOUT("타임아웃");

    private final String description;
}
