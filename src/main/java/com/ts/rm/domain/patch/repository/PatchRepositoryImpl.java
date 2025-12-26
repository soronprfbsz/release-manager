package com.ts.rm.domain.patch.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.entity.QPatch;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Patch Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class PatchRepositoryImpl implements PatchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Patch> findRecentByProjectIdAndReleaseType(String projectId, String releaseType, int limit) {
        QPatch p = QPatch.patch;

        return queryFactory
                .selectFrom(p)
                .where(
                        p.project.projectId.eq(projectId),
                        p.releaseType.eq(releaseType)
                )
                .orderBy(p.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
