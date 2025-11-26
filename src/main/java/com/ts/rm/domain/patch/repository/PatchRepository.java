package com.ts.rm.domain.patch.repository;

import com.ts.rm.domain.patch.entity.Patch;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Patch Repository
 *
 * <p>누적 패치 조회 및 관리를 위한 Repository
 * <p>업데이트는 JPA Dirty Checking 사용 (Service에서 엔티티 조회 후 setter 호출)
 */
public interface PatchRepository extends JpaRepository<Patch, Long> {

    /**
     * 패치 페이징 조회 (최신순)
     *
     * @param pageable 페이징 정보
     * @return 패치 페이지
     */
    Page<Patch> findAllByOrderByGeneratedAtDesc(Pageable pageable);

    /**
     * 릴리즈 타입별 패치 페이징 조회 (최신순)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param pageable    페이징 정보
     * @return 패치 페이지
     */
    Page<Patch> findAllByReleaseTypeOrderByGeneratedAtDesc(String releaseType, Pageable pageable);

    /**
     * 릴리즈 타입별 패치 조회 (최신순) - 비페이징
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @return 패치 목록
     */
    @Query("SELECT p FROM Patch p WHERE p.releaseType = :releaseType ORDER BY p.generatedAt DESC")
    List<Patch> findAllByReleaseTypeOrderByGeneratedAtDesc(
            @Param("releaseType") String releaseType);

    /**
     * 고객사별 패치 조회 (최신순)
     *
     * @param customerId 고객사 ID
     * @return 패치 목록
     */
    @Query("SELECT p FROM Patch p WHERE p.customer.customerId = :customerId ORDER BY p.generatedAt DESC")
    List<Patch> findAllByCustomerIdOrderByGeneratedAtDesc(@Param("customerId") Long customerId);

    /**
     * 버전 범위로 패치 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 패치 목록
     */
    @Query("SELECT p FROM Patch p WHERE p.releaseType = :releaseType AND p.fromVersion = :fromVersion AND p.toVersion = :toVersion")
    List<Patch> findByVersionRange(@Param("releaseType") String releaseType,
            @Param("fromVersion") String fromVersion, @Param("toVersion") String toVersion);
}
