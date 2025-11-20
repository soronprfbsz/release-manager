package com.ts.rm.domain.release.entity;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "release_version")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "release_version_id")
    private Long releaseVersionId;

    @Column(name = "release_type", nullable = false, length = 20)
    private String releaseType;

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

    @Column(name = "major_minor", nullable = false, length = 10)
    private String majorMinor;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "custom_version", length = 50)
    private String customVersion;

    @Column(name = "is_install")
    private Boolean isInstall;

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
}
