package com.ts.rm.domain.patch.repository;

import com.ts.rm.domain.patch.entity.PatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * PatchHistory Repository
 *
 * <p>패치 이력 데이터 접근
 */
@Repository
public interface PatchHistoryRepository extends JpaRepository<PatchHistory, Long>,
        PatchHistoryRepositoryCustom {

}
