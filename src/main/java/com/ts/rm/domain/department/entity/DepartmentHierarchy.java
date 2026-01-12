package com.ts.rm.domain.department.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 부서 계층 구조 엔티티 (Closure Table 패턴)
 * <p>
 * Closure Table 패턴은 계층 구조를 효율적으로 조회할 수 있는 방식입니다.
 * - 모든 조상-후손 관계를 저장하여 하위/상위 부서 조회가 단일 쿼리로 가능
 * - depth=0: 자기 자신, depth=1: 직계 자식, depth=2: 손자 등
 */
@Entity
@Table(name = "department_hierarchy")
@IdClass(DepartmentHierarchyId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentHierarchy {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor_id")
    private Department ancestor;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant_id")
    private Department descendant;

    @Column(name = "depth", nullable = false)
    private Integer depth;

    /**
     * 자기 참조 계층 생성 (depth=0)
     */
    public static DepartmentHierarchy createSelfReference(Department department) {
        return DepartmentHierarchy.builder()
                .ancestor(department)
                .descendant(department)
                .depth(0)
                .build();
    }

    /**
     * 부모-자식 관계 계층 생성 (depth=1)
     */
    public static DepartmentHierarchy createParentChild(Department parent, Department child) {
        return DepartmentHierarchy.builder()
                .ancestor(parent)
                .descendant(child)
                .depth(1)
                .build();
    }

    /**
     * 지정된 depth로 계층 생성
     */
    public static DepartmentHierarchy createWithDepth(Department ancestor, Department descendant, int depth) {
        return DepartmentHierarchy.builder()
                .ancestor(ancestor)
                .descendant(descendant)
                .depth(depth)
                .build();
    }
}
