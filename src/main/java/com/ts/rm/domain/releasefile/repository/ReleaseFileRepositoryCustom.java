package com.ts.rm.domain.releasefile.repository;

import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import java.util.List;

/**
 * ReleaseFile Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현을 위한 인터페이스
 */
public interface ReleaseFileRepositoryCustom {

    /**
     * 버전 범위 내 릴리즈 파일 목록 조회 (INSTALL 카테고리 버전 제외)
     *
     * @param projectId   프로젝트 ID
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 릴리즈 파일 목록
     */
    List<ReleaseFile> findReleaseFilesBetweenVersionsExcludingInstall(String projectId, String fromVersion, String toVersion);

    /**
     * 버전 범위 내 특정 하위 카테고리 파일 목록 조회
     *
     * @param projectId   프로젝트 ID
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @param subCategory 하위 카테고리 (대소문자 무시)
     * @return 릴리즈 파일 목록
     */
    List<ReleaseFile> findReleaseFilesBetweenVersionsBySubCategory(String projectId, String fromVersion, String toVersion, String subCategory);

    /**
     * 버전 범위 내 빌드 산출물 파일 목록 조회
     * (fileCategory가 WEB 또는 ENGINE인 파일)
     *
     * @param projectId   프로젝트 ID
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 릴리즈 파일 목록
     */
    List<ReleaseFile> findBuildArtifactsBetweenVersions(String projectId, String fromVersion, String toVersion);

    /**
     * 릴리즈 버전 ID로 포함된 파일 카테고리 목록 조회 (중복 제거)
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @return 파일 카테고리 목록
     */
    List<FileCategory> findCategoriesByVersionId(Long releaseVersionId);
}
