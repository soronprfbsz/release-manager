package com.ts.rm.domain.job.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BackupFile Entity
 *
 * <p>백업 파일 메타데이터 관리 엔티티
 */
@Entity
@Table(name = "backup_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "backup_file_id")
    private Long backupFileId;

    /**
     * 파일 카테고리 (MARIADB, CRATEDB)
     */
    @Column(name = "file_category", nullable = false, length = 20)
    private String fileCategory;

    /**
     * 파일 타입 (확장자 대문자, 예: SQL)
     */
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    /**
     * 파일명
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 파일 경로 (job/backup_files/ 하위 상대경로)
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

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
     * 파일 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 생성자
     */
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
}
