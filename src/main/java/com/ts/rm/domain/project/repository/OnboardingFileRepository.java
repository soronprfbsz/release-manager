package com.ts.rm.domain.project.repository;

import com.ts.rm.domain.project.entity.OnboardingFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * OnboardingFile Repository
 *
 * <p>온보딩 파일 데이터 액세스
 */
@Repository
public interface OnboardingFileRepository extends JpaRepository<OnboardingFile, Long> {

    /**
     * 프로젝트별 온보딩 파일 목록 조회 (정렬 순서 오름차순)
     */
    List<OnboardingFile> findAllByProject_ProjectIdOrderBySortOrderAscCreatedAtDesc(String projectId);

    /**
     * 프로젝트별, 카테고리별 온보딩 파일 목록 조회
     */
    List<OnboardingFile> findAllByProject_ProjectIdAndFileCategoryOrderBySortOrderAsc(
            String projectId, String fileCategory);

    /**
     * 파일 경로로 온보딩 파일 조회
     */
    Optional<OnboardingFile> findByFilePath(String filePath);

    /**
     * 파일 경로 존재 여부 확인
     */
    boolean existsByFilePath(String filePath);

    /**
     * 프로젝트별 최대 sortOrder 조회
     */
    @Query("SELECT COALESCE(MAX(of.sortOrder), 0) FROM OnboardingFile of WHERE of.project.projectId = :projectId")
    Integer findMaxSortOrderByProjectId(@Param("projectId") String projectId);

    /**
     * 프로젝트별 파일 개수 조회
     */
    long countByProject_ProjectId(String projectId);

    /**
     * 프로젝트별 총 파일 크기 조회
     */
    @Query("SELECT COALESCE(SUM(of.fileSize), 0) FROM OnboardingFile of WHERE of.project.projectId = :projectId")
    Long sumFileSizeByProjectId(@Param("projectId") String projectId);

    /**
     * 프로젝트별 온보딩 파일 전체 삭제
     */
    void deleteAllByProject_ProjectId(String projectId);
}
