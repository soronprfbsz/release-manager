package com.ts.rm.domain.filesync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 동기화 대상 유형
 *
 * <p>동기화 가능한 파일 유형을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum FileSyncTarget {
    RELEASE_FILE("릴리즈 파일", "versions", "release_file"),
    RESOURCE_FILE("리소스 파일", "resource", "resource_file"),
    BACKUP_FILE("백업 파일", "job", "backup_file");

    /** 표시 이름 */
    private final String displayName;

    /** 스캔 기준 경로 (상대 경로) */
    private final String basePath;

    /** 테이블명 */
    private final String tableName;
}
