package com.ts.rm.domain.patch.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.patch.entity.QCumulativePatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * CumulativePatch Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class CumulativePatchRepositoryImpl implements CumulativePatchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long updateStatus(Long cumulativePatchId, String status) {
        QCumulativePatch cp = QCumulativePatch.cumulativePatch;

        return queryFactory
                .update(cp)
                .set(cp.status, status)
                .where(cp.cumulativePatchId.eq(cumulativePatchId))
                .execute();
    }

    @Override
    public long updateError(Long cumulativePatchId, String errorMessage) {
        QCumulativePatch cp = QCumulativePatch.cumulativePatch;

        return queryFactory
                .update(cp)
                .set(cp.status, "FAILED")
                .set(cp.errorMessage, errorMessage)
                .where(cp.cumulativePatchId.eq(cumulativePatchId))
                .execute();
    }
}
