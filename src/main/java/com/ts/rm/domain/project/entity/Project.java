package com.ts.rm.domain.project.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project Entity
 *
 * <p>프로젝트(제품) 정보 관리
 * <p>각 프로젝트별로 릴리즈 버전과 패치를 분리하여 관리
 */
@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @Column(name = "project_id", length = 50)
    private String projectId;

    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "created_by", nullable = false, length = 100)
    @Builder.Default
    private String createdBy = "SYSTEM";
}
