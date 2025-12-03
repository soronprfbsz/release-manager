package com.ts.rm.domain.remote.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 백업 작업 상태 열거형
 */
@Getter
@RequiredArgsConstructor
public enum BackupJobStatus {

    RUNNING("running", "실행 중"),
    SUCCESS("success", "성공"),
    FAILED("failed", "실패");

    private final String code;
    private final String description;

    /**
     * 코드로 BackupJobStatus 조회
     */
    public static BackupJobStatus fromCode(String code) {
        for (BackupJobStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown backup job status: " + code);
    }
}
