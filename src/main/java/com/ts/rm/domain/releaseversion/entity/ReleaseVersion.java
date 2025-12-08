package com.ts.rm.domain.releaseversion.entity;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "release_version")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "release_version_id")
    private Long releaseVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "release_type", nullable = false, length = 20)
    private String releaseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_category", nullable = false, length = 20)
    private ReleaseCategory releaseCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(name = "major_version", nullable = false)
    private Integer majorVersion;

    @Column(name = "minor_version", nullable = false)
    private Integer minorVersion;

    @Column(name = "patch_version", nullable = false)
    private Integer patchVersion;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "custom_version", length = 50)
    private String customVersion;

    @OneToMany(mappedBy = "releaseVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReleaseFile> releaseFiles = new ArrayList<>();

    /**
     * 릴리즈 파일 추가
     */
    public void addReleaseFile(ReleaseFile releaseFile) {
        this.releaseFiles.add(releaseFile);
        releaseFile.setReleaseVersion(this);
    }

    /**
     * 버전 키 생성 (예: standard/1.1.0 또는 custom/company_a/1.0.0)
     */
    public String getVersionKey() {
        StringBuilder key = new StringBuilder();
        key.append(releaseType.toLowerCase()).append("/");
        if (customer != null) {
            key.append(customer.getCustomerCode()).append("/");
        }
        key.append(version);
        return key.toString();
    }

    /**
     * Major.Minor 버전 계산 (예: 1.1.x)
     *
     * <p>DB 컬럼 없이 majorVersion, minorVersion으로 동적 계산
     */
    @Transient
    public String getMajorMinor() {
        return majorVersion + "." + minorVersion + ".x";
    }
}
