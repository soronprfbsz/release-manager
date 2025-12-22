package com.ts.rm.domain.patch.entity;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.engineer.entity.Engineer;
import com.ts.rm.domain.project.entity.Project;
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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Patch Entity
 *
 * <p>패치 파일 테이블 - 패치 생성 기록 관리
 */
@Entity
@Table(name = "patch_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patch_id")
    private Long patchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "release_type", nullable = false, length = 20)
    private String releaseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "from_version", nullable = false, length = 50)
    private String fromVersion;

    @Column(name = "to_version", nullable = false, length = 50)
    private String toVersion;

    @Column(name = "patch_name", nullable = false, length = 100)
    private String patchName;

    @Column(name = "output_path", nullable = false, length = 500)
    private String outputPath;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 패치 담당자 (엔지니어)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id")
    private Engineer engineer;
}
