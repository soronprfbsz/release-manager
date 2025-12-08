package com.ts.rm.domain.releaseversion.repository;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReleaseVersion Repository
 *
 * <p>릴리즈 버전 조회 및 관리를 위한 Repository
 * <p>Spring Data JPA 메서드 네이밍으로 CRUD 처리
 * <p>복잡한 쿼리(LIMIT 포함 등)는 ReleaseVersionRepositoryCustom에서 QueryDSL로 처리
 */
public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Long>,
        ReleaseVersionRepositoryCustom {

    /**
     * 버전으로 릴리즈 버전 조회
     *
     * @param version 버전 (예: 1.1.0)
     * @return ReleaseVersion
     */
    Optional<ReleaseVersion> findByVersion(String version);

    /**
     * 프로젝트와 버전으로 릴리즈 버전 조회
     *
     * @param projectId 프로젝트 ID
     * @param version   버전 (예: 1.1.0)
     * @return ReleaseVersion
     */
    Optional<ReleaseVersion> findByProject_ProjectIdAndVersion(String projectId, String version);

    /**
     * 릴리즈 타입과 버전으로 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param version     버전
     * @return ReleaseVersion
     */
    Optional<ReleaseVersion> findByReleaseTypeAndVersion(String releaseType, String version);

    /**
     * 프로젝트, 릴리즈 타입, 버전으로 조회
     *
     * @param projectId   프로젝트 ID
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param version     버전
     * @return ReleaseVersion
     */
    Optional<ReleaseVersion> findByProject_ProjectIdAndReleaseTypeAndVersion(
            String projectId, String releaseType, String version);

    /**
     * 릴리즈 타입별 버전 목록 조회 (최신순)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @return 버전 목록
     */
    List<ReleaseVersion> findAllByReleaseTypeOrderByCreatedAtDesc(String releaseType);

    /**
     * 프로젝트별 릴리즈 타입별 버전 목록 조회 (최신순)
     *
     * @param projectId   프로젝트 ID
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @return 버전 목록
     */
    List<ReleaseVersion> findAllByProject_ProjectIdAndReleaseTypeOrderByCreatedAtDesc(
            String projectId, String releaseType);

    /**
     * 고객사별 커스텀 버전 목록 조회 (최신순)
     *
     * @param customerId 고객사 ID
     * @return 버전 목록
     */
    List<ReleaseVersion> findAllByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    /**
     * Major, Minor 버전으로 버전 목록 조회
     *
     * @param majorVersion Major 버전 (예: 1)
     * @param minorVersion Minor 버전 (예: 1)
     * @return 버전 목록
     */
    List<ReleaseVersion> findAllByMajorVersionAndMinorVersionOrderByPatchVersionDesc(
            Integer majorVersion, Integer minorVersion);

    /**
     * 버전 존재 여부 확인
     *
     * @param version 버전
     * @return 존재 여부
     */
    boolean existsByVersion(String version);

    /**
     * 프로젝트 내 버전 존재 여부 확인
     *
     * @param projectId 프로젝트 ID
     * @param version   버전
     * @return 존재 여부
     */
    boolean existsByProject_ProjectIdAndVersion(String projectId, String version);
}
