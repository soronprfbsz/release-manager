package com.ts.rm.domain.job.repository;

import com.ts.rm.domain.job.entity.BackupFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BackupFile Repository
 */
@Repository
public interface BackupFileRepository extends JpaRepository<BackupFile, Long>,
        BackupFileRepositoryCustom {

    /**
     * 파일명으로 조회
     */
    Optional<BackupFile> findByFileName(String fileName);

    /**
     * 파일 타입별 조회 (페이징, 생성일시 내림차순)
     */
    Page<BackupFile> findByFileTypeOrderByCreatedAtDesc(String fileType, Pageable pageable);

    /**
     * 전체 조회 (생성일시 내림차순)
     */
    List<BackupFile> findAllByOrderByCreatedAtDesc();

    /**
     * 전체 조회 (페이징, 생성일시 내림차순)
     */
    Page<BackupFile> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 파일명 존재 여부 확인
     */
    boolean existsByFileName(String fileName);

    /**
     * 파일 경로 존재 여부 확인
     */
    boolean existsByFilePath(String filePath);
}
