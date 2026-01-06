package com.ts.rm.domain.service.entity;

import com.ts.rm.domain.service.enums.ComponentType;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ServiceComponent Entity
 *
 * <p>서비스 컴포넌트 엔티티 - 서비스의 접속 정보(Web, DB, Engine 등)를 관리합니다.
 */
@Entity
@Table(name = "service_component")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ServiceComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "component_id")
    private Long componentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 50)
    private ComponentType componentType;

    @Column(name = "component_name", nullable = false, length = 255)
    private String componentName;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ssh_port")
    private Integer sshPort;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 컴포넌트 정보 수정
     *
     * @param componentType 컴포넌트 타입
     * @param componentName 컴포넌트명
     * @param host          호스트
     * @param port          포트
     * @param url           URL
     * @param description   설명
     * @param sshPort       SSH 포트
     * @param updatedBy     수정자
     */
    public void update(ComponentType componentType, String componentName,
                       String host, Integer port, String url,
                       String description, Integer sshPort,
                       String updatedBy) {
        if (componentType != null) {
            this.componentType = componentType;
        }
        if (componentName != null && !componentName.isBlank()) {
            this.componentName = componentName;
        }
        // null 허용 필드들도 업데이트
        this.host = host;
        this.port = port;
        this.url = url;
        this.description = description;
        this.sshPort = sshPort;

        if (updatedBy != null && !updatedBy.isBlank()) {
            this.updatedBy = updatedBy;
        }
    }

    /**
     * 정렬 순서 설정 (자동 계산용)
     *
     * @param sortOrder 정렬 순서
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
