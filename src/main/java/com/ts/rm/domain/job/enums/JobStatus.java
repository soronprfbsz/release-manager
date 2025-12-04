package com.ts.rm.domain.job.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 작업 상태 열거형
 */
@Getter
@RequiredArgsConstructor
public enum JobStatus {

    RUNNING("running", "실행 중"),
    SUCCESS("success", "성공"),
    FAILED("failed", "실패");

    private final String code;
    private final String description;

    /**
     * 코드로 JobStatus 조회
     */
    public static JobStatus fromCode(String code) {
        for (JobStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown job status: " + code);
    }
}
