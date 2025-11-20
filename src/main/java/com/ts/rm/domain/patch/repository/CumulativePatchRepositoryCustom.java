package com.ts.rm.domain.patch.repository;

/**
 * CumulativePatch Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
 */
public interface CumulativePatchRepositoryCustom {

    /**
     * 누적 패치 상태 업데이트
     *
     * @param cumulativePatchId 누적 패치 ID
     * @param status            상태
     * @return 업데이트된 행 수
     */
    long updateStatus(Long cumulativePatchId, String status);

    /**
     * 누적 패치 실패 정보 업데이트
     *
     * @param cumulativePatchId 누적 패치 ID
     * @param errorMessage      에러 메시지
     * @return 업데이트된 행 수
     */
    long updateError(Long cumulativePatchId, String errorMessage);
}
