package com.ts.rm.domain.filesync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 동기화 상태
 *
 * <p>파일시스템과 DB 메타데이터 간의 동기화 상태를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum FileSyncStatus {
    SYNCED("동기화됨", "파일과 메타데이터가 일치합니다"),
    UNREGISTERED("미등록", "파일시스템에만 존재 (DB 메타데이터 없음)"),
    FILE_MISSING("파일 없음", "DB에만 존재 (실제 파일 없음)"),
    SIZE_MISMATCH("크기 불일치", "파일 크기가 DB 메타데이터와 다릅니다"),
    CHECKSUM_MISMATCH("체크섬 불일치", "파일 체크섬이 DB 메타데이터와 다릅니다");

    /** 표시 이름 */
    private final String displayName;

    /** 상태 설명 */
    private final String description;
}
