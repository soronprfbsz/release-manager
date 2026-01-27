package com.ts.rm.domain.department.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ts.rm.domain.department.entity.DepartmentHierarchy;
import com.ts.rm.domain.department.entity.DepartmentHierarchyId;

/**
 * 부서 계층 구조 Repository
 */
public interface DepartmentHierarchyRepository extends JpaRepository<DepartmentHierarchy, DepartmentHierarchyId>,
        DepartmentHierarchyRepositoryCustom {

    /**
     * 특정 부서의 모든 하위 부서 계층 조회 (자기 자신 포함)
     */
    List<DepartmentHierarchy> findByAncestorDepartmentId(Long ancestorId);

    /**
     * 특정 부서의 직계 자식 부서만 조회 (depth=1)
     */
    List<DepartmentHierarchy> findByAncestorDepartmentIdAndDepth(Long ancestorId, Integer depth);

    /**
     * 특정 부서의 모든 상위 부서 계층 조회 (자기 자신 포함)
     */
    List<DepartmentHierarchy> findByDescendantDepartmentId(Long descendantId);

    /**
     * 특정 부서의 직계 부모 조회 (depth=1)
     */
    Optional<DepartmentHierarchy> findByDescendantDepartmentIdAndDepth(Long descendantId, Integer depth);

    /**
     * 특정 부서가 다른 부서의 조상인지 확인
     */
    boolean existsByAncestorDepartmentIdAndDescendantDepartmentId(Long ancestorId, Long descendantId);

    /**
     * 특정 부서의 직계 자식 수 조회
     */
    long countByAncestorDepartmentIdAndDepth(Long ancestorId, Integer depth);
}
