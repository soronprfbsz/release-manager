package com.ts.rm.domain.release.repository;

/**
 * ReleaseFile Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
 */
public interface ReleaseFileRepositoryCustom {

    /**
     * 릴리즈 파일 정보 업데이트
     *
     * @param releaseFileId  릴리즈 파일 ID
     * @param description    설명
     * @param executionOrder 실행 순서
     * @return 업데이트된 행 수
     */
    long updateReleaseFileInfo(Long releaseFileId, String description, Integer executionOrder);

    /**
     * 체크섬 업데이트
     *
     * @param releaseFileId 릴리즈 파일 ID
     * @param checksum      체크섬
     * @return 업데이트된 행 수
     */
    long updateChecksum(Long releaseFileId, String checksum);
}
