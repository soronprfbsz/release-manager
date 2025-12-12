package com.ts.rm.domain.resourcefile.repository;

import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ResourceFile Repository
 */
@Repository
public interface ResourceFileRepository extends JpaRepository<ResourceFile, Long> {

    /**
     * 파일 경로로 조회
     */
    Optional<ResourceFile> findByFilePath(String filePath);

    /**
     * 파일 타입으로 조회 (생성일시 내림차순)
     */
    List<ResourceFile> findByFileTypeOrderByCreatedAtDesc(String fileType);

    /**
     * 전체 조회 (생성일시 내림차순)
     */
    List<ResourceFile> findAllByOrderByCreatedAtDesc();

    /**
     * 파일 경로 패턴으로 조회 (예: resource/script/%)
     */
    List<ResourceFile> findByFilePathStartingWithOrderByCreatedAtDesc(String pathPrefix);

    /**
     * 파일 카테고리로 조회 (생성일시 내림차순)
     */
    List<ResourceFile> findByFileCategoryOrderByCreatedAtDesc(String fileCategory);

    /**
     * 파일 카테고리로 조회 (sortOrder 오름차순, 생성일시 내림차순)
     */
    List<ResourceFile> findByFileCategoryOrderBySortOrderAscCreatedAtDesc(String fileCategory);

    /**
     * 전체 조회 (sortOrder 오름차순, 생성일시 내림차순)
     */
    List<ResourceFile> findAllByOrderBySortOrderAscCreatedAtDesc();

    /**
     * 파일 카테고리별 최대 sortOrder 조회 (자동 채번용)
     *
     * @param fileCategory 파일 카테고리
     * @return 최대 sortOrder (없으면 0)
     */
    @Query("SELECT COALESCE(MAX(rf.sortOrder), 0) FROM ResourceFile rf WHERE rf.fileCategory = :fileCategory")
    Integer findMaxSortOrderByFileCategory(@Param("fileCategory") String fileCategory);

    /**
     * 파일 경로 존재 여부 확인
     */
    boolean existsByFilePath(String filePath);
}
