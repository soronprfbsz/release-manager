package com.ts.rm.domain.resourcefile.repository;

import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * 파일 경로 존재 여부 확인
     */
    boolean existsByFilePath(String filePath);
}
