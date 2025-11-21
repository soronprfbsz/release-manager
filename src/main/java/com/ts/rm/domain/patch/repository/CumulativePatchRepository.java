package com.ts.rm.domain.patch.repository;

import com.ts.rm.domain.patch.entity.CumulativePatch;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * CumulativePatch Repository
 *
 * <p>누적 패치 생성 이력 조회 및 관리를 위한 Repository
 * <p>업데이트는 JPA Dirty Checking 사용 (Service에서 엔티티 조회 후 setter 호출)
 */
public interface CumulativePatchRepository extends JpaRepository<CumulativePatch, Long> {

    /**
     * 릴리즈 타입별 누적 패치 이력 조회 (최신순)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @return 누적 패치 이력 목록
     */
    @Query("SELECT cp FROM CumulativePatch cp WHERE cp.releaseType = :releaseType ORDER BY cp.generatedAt DESC")
    List<CumulativePatch> findAllByReleaseTypeOrderByGeneratedAtDesc(
            @Param("releaseType") String releaseType);

    /**
     * 고객사별 누적 패치 이력 조회 (최신순)
     *
     * @param customerId 고객사 ID
     * @return 누적 패치 이력 목록
     */
    @Query("SELECT cp FROM CumulativePatch cp WHERE cp.customer.customerId = :customerId ORDER BY cp.generatedAt DESC")
    List<CumulativePatch> findAllByCustomerIdOrderByGeneratedAtDesc(@Param("customerId") Long customerId);

    /**
     * 상태별 누적 패치 이력 조회
     *
     * @param status 상태 (SUCCESS/FAILED/IN_PROGRESS)
     * @return 누적 패치 이력 목록
     */
    List<CumulativePatch> findAllByStatus(String status);

    /**
     * 버전 범위로 누적 패치 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 누적 패치 목록
     */
    @Query("SELECT cp FROM CumulativePatch cp WHERE cp.releaseType = :releaseType AND cp.fromVersion = :fromVersion AND cp.toVersion = :toVersion")
    List<CumulativePatch> findByVersionRange(@Param("releaseType") String releaseType,
            @Param("fromVersion") String fromVersion, @Param("toVersion") String toVersion);
}
