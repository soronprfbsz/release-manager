package com.ts.rm.domain.department.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ts.rm.domain.department.entity.DepartmentHierarchy;
import com.ts.rm.domain.department.entity.DepartmentHierarchyId;

/**
 * 부서 계층 구조 Repository
 */
public interface DepartmentHierarchyRepository extends JpaRepository<DepartmentHierarchy, DepartmentHierarchyId> {

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

    /**
     * 특정 부서 관련 모든 계층 데이터 삭제 (조상 또는 후손으로 참조되는 경우)
     */
    @Modifying
    @Query("DELETE FROM DepartmentHierarchy dh WHERE dh.ancestor.departmentId = :departmentId OR dh.descendant.departmentId = :departmentId")
    void deleteByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 특정 부서를 후손으로 하는 모든 계층 삭제 (부서 이동 시 기존 관계 제거용)
     */
    @Modifying
    @Query("DELETE FROM DepartmentHierarchy dh WHERE dh.descendant.departmentId = :descendantId AND dh.depth > 0")
    void deleteAncestorRelationships(@Param("descendantId") Long descendantId);

    /**
     * 특정 부서의 모든 하위 부서 ID 목록 조회 (자기 자신 제외)
     */
    @Query("SELECT dh.descendant.departmentId FROM DepartmentHierarchy dh WHERE dh.ancestor.departmentId = :ancestorId AND dh.depth > 0")
    List<Long> findDescendantIds(@Param("ancestorId") Long ancestorId);

    /**
     * 루트 부서 조회 (다른 부서의 자식이 아닌 부서)
     * 자기 참조(depth=0)만 있는 부서가 루트
     */
    @Query("SELECT dh.descendant FROM DepartmentHierarchy dh WHERE dh.ancestor = dh.descendant " +
           "AND dh.descendant.departmentId NOT IN " +
           "(SELECT dh2.descendant.departmentId FROM DepartmentHierarchy dh2 WHERE dh2.depth = 1)")
    List<com.ts.rm.domain.department.entity.Department> findRootDepartments();
}
