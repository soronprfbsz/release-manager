package com.ts.rm.domain.release.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.release.entity.QReleaseFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ReleaseFile Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ReleaseFileRepositoryImpl implements ReleaseFileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long updateReleaseFileInfo(Long releaseFileId, String description, Integer executionOrder) {
        QReleaseFile rf = QReleaseFile.releaseFile;

        return queryFactory
                .update(rf)
                .set(rf.description, description)
                .set(rf.executionOrder, executionOrder)
                .where(rf.releaseFileId.eq(releaseFileId))
                .execute();
    }

    @Override
    public long updateChecksum(Long releaseFileId, String checksum) {
        QReleaseFile rf = QReleaseFile.releaseFile;

        return queryFactory
                .update(rf)
                .set(rf.checksum, checksum)
                .where(rf.releaseFileId.eq(releaseFileId))
                .execute();
    }
}
