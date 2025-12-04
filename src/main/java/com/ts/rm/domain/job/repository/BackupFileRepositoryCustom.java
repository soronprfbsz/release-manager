package com.ts.rm.domain.job.repository;

import com.ts.rm.domain.job.entity.BackupFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * BackupFile Custom Repository Interface
 */
public interface BackupFileRepositoryCustom {

    /**
     * 백업 파일 목록 조회 (검색 조건 + 페이징)
     *
     * @param fileCategory 파일 카테고리 (nullable, MARIADB/CRATEDB)
     * @param fileType     파일 타입 (nullable)
     * @param fileName     파일명 (nullable, 부분 일치)
     * @param pageable     페이징 정보
     * @return 백업 파일 목록
     */
    Page<BackupFile> searchBackupFiles(
            String fileCategory,
            String fileType,
            String fileName,
            Pageable pageable);
}
