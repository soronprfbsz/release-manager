package com.ts.rm.domain.job.repository;

import com.ts.rm.domain.job.entity.BackupFileLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * BackupFileLog Repository
 */
@Repository
public interface BackupFileLogRepository extends JpaRepository<BackupFileLog, Long> {

    /**
     * 백업 파일 ID로 로그 목록 조회 (생성일시 내림차순)
     */
    List<BackupFileLog> findByBackupFile_BackupFileIdOrderByCreatedAtDesc(Long backupFileId);

    /**
     * 백업 파일 ID와 로그 타입으로 로그 목록 조회 (생성일시 내림차순)
     */
    List<BackupFileLog> findByBackupFile_BackupFileIdAndLogTypeOrderByCreatedAtDesc(
            Long backupFileId, String logType);

    /**
     * 로그 파일명으로 조회
     */
    List<BackupFileLog> findByLogFileName(String logFileName);

    /**
     * 백업 파일 ID로 로그 삭제
     */
    void deleteByBackupFile_BackupFileId(Long backupFileId);

    /**
     * 백업 파일 ID 존재 여부 확인
     */
    boolean existsByBackupFile_BackupFileId(Long backupFileId);
}
