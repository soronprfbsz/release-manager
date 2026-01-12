package com.ts.rm.domain.department.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DepartmentHierarchy 복합 기본키 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentHierarchyId implements Serializable {

    private Long ancestor;
    private Long descendant;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentHierarchyId that = (DepartmentHierarchyId) o;
        return Objects.equals(ancestor, that.ancestor) && Objects.equals(descendant, that.descendant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ancestor, descendant);
    }
}
