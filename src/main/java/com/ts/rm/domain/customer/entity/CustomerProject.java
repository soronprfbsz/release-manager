package com.ts.rm.domain.customer.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import com.ts.rm.domain.project.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CustomerProject Entity
 *
 * <p>고객사-프로젝트 매핑 테이블
 * <p>고객사가 사용 중인 프로젝트와 마지막 패치 버전 정보를 관리
 */
@Entity
@Table(name = "customer_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProject extends BaseEntity {

    @EmbeddedId
    private CustomerProjectId id;

    @MapsId("customerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @MapsId("projectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "last_patched_version", length = 50)
    private String lastPatchedVersion;

    @Column(name = "last_patched_at")
    private LocalDateTime lastPatchedAt;

    /**
     * 마지막 패치 정보 업데이트
     *
     * @param version   패치 버전 (to_version)
     * @param patchedAt 패치 일시
     */
    public void updateLastPatchInfo(String version, LocalDateTime patchedAt) {
        this.lastPatchedVersion = version;
        this.lastPatchedAt = patchedAt;
    }

    /**
     * 정적 팩토리 메서드: 새 CustomerProject 생성
     */
    public static CustomerProject create(Customer customer, Project project) {
        CustomerProjectId id = new CustomerProjectId(customer.getCustomerId(), project.getProjectId());
        return CustomerProject.builder()
                .id(id)
                .customer(customer)
                .project(project)
                .build();
    }
}
