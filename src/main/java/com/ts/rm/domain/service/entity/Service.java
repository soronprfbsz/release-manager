package com.ts.rm.domain.service.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Service Entity
 *
 * <p>서비스 관리 엔티티 - 복합 서비스의 기본 정보를 관리합니다.
 */
@Entity
@Table(name = "service")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Service extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "service_name", nullable = false, length = 255)
    private String serviceName;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ServiceComponent> components = new ArrayList<>();

    /**
     * 서비스 정보 수정
     *
     * @param serviceName 서비스명
     * @param serviceType 서비스 분류
     * @param description 설명
     * @param updatedBy   수정자
     */
    public void update(String serviceName, String serviceType,
                       String description, String updatedBy) {
        if (serviceName != null && !serviceName.isBlank()) {
            this.serviceName = serviceName;
        }
        if (serviceType != null && !serviceType.isBlank()) {
            this.serviceType = serviceType;
        }
        if (description != null) {
            this.description = description;
        }
        if (updatedBy != null && !updatedBy.isBlank()) {
            this.updatedBy = updatedBy;
        }
    }

    /**
     * 컴포넌트 추가
     *
     * @param component 서비스 컴포넌트
     */
    public void addComponent(ServiceComponent component) {
        this.components.add(component);
        component.setService(this);
    }

    /**
     * 컴포넌트 제거
     *
     * @param component 서비스 컴포넌트
     */
    public void removeComponent(ServiceComponent component) {
        this.components.remove(component);
        component.setService(null);
    }

    /**
     * 모든 컴포넌트 제거
     */
    public void removeAllComponents() {
        this.components.clear();
    }

    /**
     * createdBy 설정 (생성 시 사용)
     *
     * @param createdBy 생성자
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
