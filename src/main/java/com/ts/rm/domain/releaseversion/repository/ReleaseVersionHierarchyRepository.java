package com.ts.rm.domain.releaseversion.repository;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHierarchy;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHierarchy.HierarchyId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ReleaseVersionHierarchy Repository
 *
 * <p>릴리즈 버전 계층 구조 관리 (Closure Table)
 */
public interface ReleaseVersionHierarchyRepository
        extends JpaRepository<ReleaseVersionHierarchy, HierarchyId>,
        ReleaseVersionHierarchyRepositoryCustom {
}
