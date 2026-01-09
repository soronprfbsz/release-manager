package com.ts.rm.domain.resourcefile.entity;

import com.ts.rm.domain.account.entity.Account;
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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ResourceFile Entity
 *
 * <p>스크립트, 문서 등 리소스 파일 관리 엔티티
 */
@Entity
@Table(name = "resource_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_file_id")
    private Long resourceFileId;

    /**
     * 파일 타입 (확장자 대문자, 예: SH, PDF, MD)
     */
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    /**
     * 파일 카테고리 (SCRIPT, DOCUMENT, ETC)
     */
    @Column(name = "file_category", nullable = false, length = 50)
    private String fileCategory;

    /**
     * 하위 카테고리
     */
    @Column(name = "sub_category", length = 50)
    private String subCategory;

    /**
     * 리소스 파일 관리용 이름
     */
    @Column(name = "resource_file_name", nullable = false, length = 255)
    private String resourceFileName;

    /**
     * 파일명
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /**
     * 파일 경로 (resource/ 하위 상대경로)
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
     * 정렬 순서 (file_category 내에서 정렬)
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 생성자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account creator;

    /**
     * 생성자 이메일 반환 헬퍼 메서드
     */
    @Transient
    public String getCreatedByEmail() {
        return creator != null ? creator.getEmail() : null;
    }

    /**
     * sortOrder 설정
     *
     * @param sortOrder 정렬 순서
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
