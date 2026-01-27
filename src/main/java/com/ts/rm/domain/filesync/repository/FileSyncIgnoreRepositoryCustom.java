package com.ts.rm.domain.filesync.repository;

import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.List;
import java.util.Set;

/**
 * FileSyncIgnore Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 파일 동기화 무시 목록 쿼리 인터페이스
 */
public interface FileSyncIgnoreRepositoryCustom {

    /**
     * 대상 유형별 무시된 파일 경로 목록 조회 (분석 시 필터링용)
     *
     * @param targetType 대상 유형
     * @return 무시된 파일 경로 Set
     */
    Set<String> findIgnoredPathsByTargetType(FileSyncTarget targetType);

    /**
     * 여러 대상 유형의 무시된 파일 경로 목록 조회
     *
     * @param targetTypes 대상 유형 목록
     * @return 무시된 파일 경로 Set
     */
    Set<String> findIgnoredPathsByTargetTypes(List<FileSyncTarget> targetTypes);
}
