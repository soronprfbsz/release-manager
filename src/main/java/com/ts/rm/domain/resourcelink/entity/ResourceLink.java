package com.ts.rm.domain.resourcelink.entity;

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
 * ResourceLink Entity
 *
 * <p>리소스 링크 관리 엔티티 (구글시트, 노션 등 외부 링크)
 */
@Entity
@Table(name = "resource_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_link_id")
    private Long resourceLinkId;

    /**
     * 링크 카테고리 (DOCUMENT, TOOL, ETC)
     */
    @Column(name = "link_category", nullable = false, length = 50)
    private String linkCategory;

    /**
     * 하위 카테고리
     */
    @Column(name = "sub_category", length = 50)
    private String subCategory;

    /**
     * 링크 이름
     */
    @Column(name = "link_name", nullable = false, length = 255)
    private String linkName;

    /**
     * 링크 주소
     */
    @Column(name = "link_url", nullable = false, length = 1000)
    private String linkUrl;

    /**
     * 링크 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 정렬 순서 (link_category 내에서 정렬)
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
     * 생성자 이메일 (계정 삭제 시에도 유지)
     */
    @Column(name = "created_by_email", length = 100)
    private String createdByEmail;

    /**
     * 생성자 이름 반환 헬퍼 메서드
     */
    @Transient
    public String getCreatedByName() {
        return creator != null ? creator.getAccountName() : null;
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
