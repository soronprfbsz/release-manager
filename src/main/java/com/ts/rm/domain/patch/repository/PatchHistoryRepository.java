package com.ts.rm.domain.patch.repository;

import com.ts.rm.domain.patch.entity.PatchHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * PatchHistory Repository
 *
 * <p>패치 이력 조회 및 관리를 위한 Repository
 * <p>업데이트는 JPA Dirty Checking 사용 (Service에서 엔티티 조회 후 setter 호출)
 */
public interface PatchHistoryRepository extends JpaRepository<PatchHistory, Long> {

    /**
     * 릴리즈 타입별 패치 이력 조회 (최신순)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @return 패치 이력 목록
     */
    @Query("SELECT ph FROM PatchHistory ph WHERE ph.releaseType = :releaseType ORDER BY ph.generatedAt DESC")
    List<PatchHistory> findAllByReleaseTypeOrderByGeneratedAtDesc(
            @Param("releaseType") String releaseType);

    /**
     * 고객사별 패치 이력 조회 (최신순)
     *
     * @param customerId 고객사 ID
     * @return 패치 이력 목록
     */
    @Query("SELECT ph FROM PatchHistory ph WHERE ph.customer.customerId = :customerId ORDER BY ph.generatedAt DESC")
    List<PatchHistory> findAllByCustomerIdOrderByGeneratedAtDesc(@Param("customerId") Long customerId);

    /**
     * 상태별 패치 이력 조회
     *
     * @param status 상태 (SUCCESS/FAILED/IN_PROGRESS)
     * @return 패치 이력 목록
     */
    List<PatchHistory> findAllByStatus(String status);

    /**
     * 버전 범위로 패치 이력 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 패치 이력 목록
     */
    @Query("SELECT ph FROM PatchHistory ph WHERE ph.releaseType = :releaseType AND ph.fromVersion = :fromVersion AND ph.toVersion = :toVersion")
    List<PatchHistory> findByVersionRange(@Param("releaseType") String releaseType,
            @Param("fromVersion") String fromVersion, @Param("toVersion") String toVersion);
}
