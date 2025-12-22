package com.ts.rm.domain.filesync.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 파일 동기화 적용 액션
 *
 * <p>불일치 항목에 대해 수행할 수 있는 액션을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum FileSyncAction {
    REGISTER("등록", "미등록 파일을 DB에 등록합니다"),
    UPDATE_METADATA("메타데이터 갱신", "실제 파일 정보로 DB 메타데이터를 갱신합니다"),
    DELETE_METADATA("메타데이터 삭제", "DB에서 메타데이터 레코드를 삭제합니다"),
    DELETE_FILE("파일 삭제", "파일시스템에서 파일을 삭제합니다"),
    IGNORE("무시", "이 항목에 대해 아무 작업도 수행하지 않습니다");

    /** 표시 이름 */
    private final String displayName;

    /** 액션 설명 */
    private final String description;
}
