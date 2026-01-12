package com.ts.rm.domain.department.entity;

import com.ts.rm.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "department")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", nullable = false, unique = true, length = 100)
    private String departmentName;

    @Column(name = "department_type", length = 50)
    private String departmentType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 부서 정보 업데이트
     */
    public void update(String departmentName, String departmentType, String description, Integer sortOrder) {
        if (departmentName != null) {
            this.departmentName = departmentName;
        }
        if (departmentType != null) {
            this.departmentType = departmentType;
        }
        if (description != null) {
            this.description = description;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
    }
}
