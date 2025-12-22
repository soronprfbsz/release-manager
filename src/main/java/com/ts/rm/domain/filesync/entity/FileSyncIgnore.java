package com.ts.rm.domain.filesync.entity;

import com.ts.rm.domain.filesync.enums.FileSyncStatus;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 파일 동기화 무시 목록 엔티티
 *
 * <p>파일 동기화 분석 시 제외할 파일 경로를 관리합니다.
 */
@Entity
@Table(name = "file_sync_ignore")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileSyncIgnore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ignore_id")
    private Long ignoreId;

    /**
     * 파일 경로 (상대 경로)
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 대상 유형 (RELEASE_FILE, RESOURCE_FILE, BACKUP_FILE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private FileSyncTarget targetType;

    /**
     * 무시 당시 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private FileSyncStatus status;

    /**
     * 무시 처리자
     */
    @Column(name = "ignored_by", nullable = false, length = 100)
    private String ignoredBy;

    /**
     * 생성일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
