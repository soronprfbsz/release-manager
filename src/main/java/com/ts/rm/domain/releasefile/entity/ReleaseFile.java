package com.ts.rm.domain.releasefile.entity;

import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * ReleaseFile Entity
 */
@Entity
@Table(name = "release_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "release_file_id")
    private Long releaseFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_version_id", nullable = false)
    private ReleaseVersion releaseVersion;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    /**
     * 파일 카테고리 (DATABASE, WEB, INSTALL, ENGINE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_category", length = 50)
    private FileCategory fileCategory;

    /**
     * 하위 카테고리 (MARIADB, CRATEDB, BUILD, SH, IMAGE, METADATA, ETC 등)
     */
    @Column(name = "sub_category", length = 50)
    private String subCategory;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "relative_path", length = 500)
    private String relativePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(length = 64)
    private String checksum;

    @Column(name = "execution_order", nullable = false)
    private Integer executionOrder;

    /**
     * 빌드 산출물 여부
     */
    @Column(name = "is_build_artifact")
    @Builder.Default
    private Boolean isBuildArtifact = false;

    @Column(length = 500)
    private String description;

    /**
     * 이 파일이 패치 생성 시 제외되어야 하는지 확인
     *
     * @return INSTALL 카테고리이면 true
     */
    public boolean isExcludedFromPatch() {
        return fileCategory != null && fileCategory.isExcludedFromPatch();
    }
}
