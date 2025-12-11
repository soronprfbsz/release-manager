package com.ts.rm.domain.job.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BackupFileLog Entity
 *
 * <p>백업 파일 로그 메타데이터 관리 엔티티
 */
@Entity
@Table(name = "backup_file_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupFileLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "backup_file_log_id")
    private Long backupFileLogId;

    /**
     * 백업 파일 (FK)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_file_id", nullable = false)
    private BackupFile backupFile;

    /**
     * 로그 타입 (BACKUP, RESTORE)
     */
    @Column(name = "log_type", nullable = false, length = 20)
    private String logType;

    /**
     * 로그 파일명
     */
    @Column(name = "log_file_name", nullable = false, length = 255)
    private String logFileName;

    /**
     * 로그 파일 경로 (job/{fileCategory}/logs/ 하위 상대경로)
     */
    @Column(name = "log_file_path", nullable = false, length = 500)
    private String logFilePath;

    /**
     * 파일 크기 (bytes)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 파일 체크섬 (SHA-256)
     */
    @Column(length = 64)
    private String checksum;

    /**
     * 로그 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 생성자
     */
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
}
