package com.ts.rm.domain.release.repository;

import com.ts.rm.domain.release.entity.ReleaseVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ReleaseVersion Repository
 *
 * <p>릴리즈 버전 조회 및 관리를 위한 Repository
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
     * 릴리즈 타입과 버전으로 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param version     버전
     * @return ReleaseVersion
     */
    @Query("SELECT rv FROM ReleaseVersion rv WHERE rv.releaseType = :releaseType AND rv.version = :version")
    Optional<ReleaseVersion> findByReleaseTypeAndVersion(@Param("releaseType") String releaseType,
            @Param("version") String version);

    /**
     * 릴리즈 타입별 버전 목록 조회 (최신순)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @return 버전 목록
     */
    @Query("SELECT rv FROM ReleaseVersion rv WHERE rv.releaseType = :releaseType ORDER BY rv.createdAt DESC")
    List<ReleaseVersion> findAllByReleaseTypeOrderByCreatedAtDesc(
            @Param("releaseType") String releaseType);

    /**
     * 고객사별 커스텀 버전 목록 조회 (최신순)
     *
     * @param customerId 고객사 ID
     * @return 버전 목록
     */
    @Query("SELECT rv FROM ReleaseVersion rv WHERE rv.customer.customerId = :customerId ORDER BY rv.createdAt DESC")
    List<ReleaseVersion> findAllByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    /**
     * Major.Minor 버전으로 버전 목록 조회
     *
     * @param majorMinor Major.Minor 버전 (예: 1.1.x)
     * @return 버전 목록
     */
    List<ReleaseVersion> findAllByMajorMinorOrderByPatchVersionDesc(String majorMinor);

    /**
     * 버전 존재 여부 확인
     *
     * @param version 버전
     * @return 존재 여부
     */
    boolean existsByVersion(String version);
}
