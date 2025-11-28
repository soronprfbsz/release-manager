package com.ts.rm.domain.releaseversion.repository;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHierarchy;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHierarchy.HierarchyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ReleaseVersionHierarchy Repository
 *
 * <p>릴리즈 버전 계층 구조 관리 (Closure Table)
 */
public interface ReleaseVersionHierarchyRepository
        extends JpaRepository<ReleaseVersionHierarchy, HierarchyId>,
        ReleaseVersionHierarchyRepositoryCustom {

    /**
     * Descendant ID로 계층 구조 삭제
     *
     * @param descendantId Descendant 버전 ID
     */
    @Modifying
    @Query("DELETE FROM ReleaseVersionHierarchy h WHERE h.descendant.releaseVersionId = :descendantId")
    void deleteByDescendantId(@Param("descendantId") Long descendantId);

    /**
     * Ancestor ID로 계층 구조 삭제
     *
     * @param ancestorId Ancestor 버전 ID
     */
    @Modifying
    @Query("DELETE FROM ReleaseVersionHierarchy h WHERE h.ancestor.releaseVersionId = :ancestorId")
    void deleteByAncestorId(@Param("ancestorId") Long ancestorId);
}
