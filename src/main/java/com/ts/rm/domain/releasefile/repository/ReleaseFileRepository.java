package com.ts.rm.domain.releasefile.repository;

import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReleaseFile Repository
 *
 * <p>릴리즈 파일 조회 및 관리를 위한 Repository
 * <p>Spring Data JPA 메서드 네이밍으로 CRUD 처리
 * <p>복잡한 쿼리(버전 범위, DISTINCT 등)는 ReleaseFileRepositoryCustom에서 QueryDSL로 처리
 */
public interface ReleaseFileRepository extends JpaRepository<ReleaseFile, Long>, ReleaseFileRepositoryCustom {

    /**
     * 릴리즈 버전 ID로 릴리즈 파일 목록 조회 (실행 순서 오름차순)
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @return 릴리즈 파일 목록
     */
    List<ReleaseFile> findAllByReleaseVersion_ReleaseVersionIdOrderByExecutionOrderAsc(Long releaseVersionId);

    /**
     * 파일명으로 릴리즈 파일 조회
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param fileName         파일명
     * @return 릴리즈 파일
     */
    Optional<ReleaseFile> findByReleaseVersion_ReleaseVersionIdAndFileName(Long releaseVersionId, String fileName);

    /**
     * 파일명 존재 여부 확인
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param fileName         파일명
     * @return 존재 여부
     */
    boolean existsByReleaseVersion_ReleaseVersionIdAndFileName(Long releaseVersionId, String fileName);

    /**
     * 파일 경로로 릴리즈 파일 조회
     *
     * @param filePath 파일 경로
     * @return 릴리즈 파일
     */
    Optional<ReleaseFile> findByFilePath(String filePath);

    /**
     * 릴리즈 버전 ID와 파일 카테고리로 파일 목록 조회
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param fileCategory     파일 카테고리
     * @return 릴리즈 파일 목록
     */
    List<ReleaseFile> findByReleaseVersion_ReleaseVersionIdAndFileCategoryOrderByExecutionOrderAsc(
            Long releaseVersionId, FileCategory fileCategory);

    /**
     * 릴리즈 버전 ID와 하위 카테고리로 파일 목록 조회
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param subCategory      하위 카테고리
     * @return 릴리즈 파일 목록
     */
    List<ReleaseFile> findByReleaseVersion_ReleaseVersionIdAndSubCategoryOrderByExecutionOrderAsc(
            Long releaseVersionId, String subCategory);

    /**
     * 릴리즈 버전 ID로 파일 조회 (특정 카테고리 제외)
     *
     * @param releaseVersionId     릴리즈 버전 ID
     * @param excludedFileCategory 제외할 파일 카테고리
     * @return 릴리즈 파일 목록
     */
    List<ReleaseFile> findByReleaseVersion_ReleaseVersionIdAndFileCategoryNotOrderByExecutionOrderAsc(
            Long releaseVersionId, FileCategory excludedFileCategory);
}
