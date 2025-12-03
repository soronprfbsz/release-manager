package com.ts.rm.domain.releasefile.repository;

import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ReleaseFile Repository
 */
public interface ReleaseFileRepository extends JpaRepository<ReleaseFile, Long> {

    /**
     * 릴리즈 버전 ID로 릴리즈 파일 목록 조회 (실행 순서 오름차순)
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId ORDER BY rf.executionOrder ASC")
    List<ReleaseFile> findAllByReleaseVersionIdOrderByExecutionOrderAsc(
            @Param("releaseVersionId") Long releaseVersionId);

    /**
     * 파일명으로 릴리즈 파일 조회
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.fileName = :fileName")
    Optional<ReleaseFile> findByReleaseVersionIdAndFileName(
            @Param("releaseVersionId") Long releaseVersionId,
            @Param("fileName") String fileName);

    /**
     * 파일명 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(rf) > 0 THEN true ELSE false END FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.fileName = :fileName")
    boolean existsByReleaseVersionIdAndFileName(@Param("releaseVersionId") Long releaseVersionId,
            @Param("fileName") String fileName);

    /**
     * 파일 경로로 릴리즈 파일 조회
     */
    Optional<ReleaseFile> findByFilePath(String filePath);

    /**
     * 릴리즈 버전 ID와 파일 카테고리로 파일 목록 조회
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.fileCategory = :fileCategory ORDER BY rf.executionOrder ASC")
    List<ReleaseFile> findByReleaseVersionIdAndFileCategory(
            @Param("releaseVersionId") Long releaseVersionId,
            @Param("fileCategory") FileCategory fileCategory);

    /**
     * 릴리즈 버전 ID와 하위 카테고리로 파일 목록 조회
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.subCategory = :subCategory ORDER BY rf.executionOrder ASC")
    List<ReleaseFile> findByReleaseVersionIdAndSubCategory(
            @Param("releaseVersionId") Long releaseVersionId,
            @Param("subCategory") String subCategory);

    /**
     * 릴리즈 버전 ID로 파일 조회 (특정 카테고리 제외)
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.fileCategory != :excludedFileCategory ORDER BY rf.executionOrder ASC")
    List<ReleaseFile> findByReleaseVersionIdAndFileCategoryNot(
            @Param("releaseVersionId") Long releaseVersionId,
            @Param("excludedFileCategory") FileCategory excludedFileCategory);

    /**
     * 버전 범위 내 릴리즈 파일 목록 조회 (INSTALL 카테고리 버전 제외)
     */
    @Query("SELECT rf FROM ReleaseFile rf " +
            "WHERE rf.releaseVersion.version >= :fromVersion " +
            "AND rf.releaseVersion.version <= :toVersion " +
            "AND (rf.releaseVersion.releaseCategory IS NULL " +
            "     OR rf.releaseVersion.releaseCategory != com.ts.rm.domain.releaseversion.enums.ReleaseCategory.INSTALL) " +
            "ORDER BY rf.releaseVersion.version ASC, rf.executionOrder ASC")
    List<ReleaseFile> findReleaseFilesBetweenVersionsExcludingInstall(
            @Param("fromVersion") String fromVersion,
            @Param("toVersion") String toVersion);

    /**
     * 버전 범위 내 특정 하위 카테고리 파일 목록 조회
     */
    @Query("SELECT rf FROM ReleaseFile rf " +
            "WHERE rf.releaseVersion.version >= :fromVersion " +
            "AND rf.releaseVersion.version <= :toVersion " +
            "AND UPPER(rf.subCategory) = UPPER(:subCategory) " +
            "ORDER BY rf.releaseVersion.version ASC, rf.executionOrder ASC")
    List<ReleaseFile> findReleaseFilesBetweenVersionsBySubCategory(
            @Param("fromVersion") String fromVersion,
            @Param("toVersion") String toVersion,
            @Param("subCategory") String subCategory);

    /**
     * 버전 범위 내 빌드 산출물 파일 목록 조회
     * (fileCategory가 WEB 또는 ENGINE인 파일)
     */
    @Query("SELECT rf FROM ReleaseFile rf " +
            "WHERE rf.releaseVersion.version >= :fromVersion " +
            "AND rf.releaseVersion.version <= :toVersion " +
            "AND rf.fileCategory IN (com.ts.rm.domain.releasefile.enums.FileCategory.WEB, com.ts.rm.domain.releasefile.enums.FileCategory.ENGINE) " +
            "ORDER BY rf.releaseVersion.version DESC, rf.executionOrder ASC")
    List<ReleaseFile> findBuildArtifactsBetweenVersions(
            @Param("fromVersion") String fromVersion,
            @Param("toVersion") String toVersion);

    /**
     * 릴리즈 버전 ID로 포함된 파일 카테고리 목록 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT rf.fileCategory FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.fileCategory IS NOT NULL ORDER BY rf.fileCategory")
    List<FileCategory> findCategoriesByVersionId(@Param("releaseVersionId") Long releaseVersionId);
}
