package com.ts.rm.domain.releaseversion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ReleaseVersionHistory Entity
 *
 * <p>패치 스크립트 실행시 자동으로 기록되는 릴리즈 버전 이력 테이블
 */
@Entity
@Table(name = "release_version_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReleaseVersionHistory {

    /**
     * 릴리즈 버전 ID (PK)
     */
    @Id
    @Column(name = "release_version_id", length = 20, nullable = false)
    private String releaseVersionId;

    /**
     * 표준 버전
     */
    @Column(name = "standard_version", length = 20, nullable = false)
    private String standardVersion;

    /**
     * 커스텀 버전
     */
    @Column(name = "custom_version", length = 20)
    private String customVersion;

    /**
     * 버전 생성일시
     */
    @Column(name = "version_created_at", nullable = false)
    private LocalDateTime versionCreatedAt;

    /**
     * 버전 생성자
     */
    @Column(name = "version_created_by", length = 100, nullable = false)
    private String versionCreatedBy;

    /**
     * 시스템 적용자
     */
    @Column(name = "system_applied_by", length = 100)
    private String systemAppliedBy;

    /**
     * 시스템 적용일시
     */
    @Column(name = "system_applied_at")
    private LocalDateTime systemAppliedAt;

    /**
     * 버전 설명
     */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /**
     * 시스템 적용 정보 업데이트
     *
     * @param appliedBy 적용자
     */
    public void updateSystemApplied(String appliedBy) {
        this.systemAppliedBy = appliedBy;
        this.systemAppliedAt = LocalDateTime.now();
    }
}
