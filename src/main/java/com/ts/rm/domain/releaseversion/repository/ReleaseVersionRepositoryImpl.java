package com.ts.rm.domain.releaseversion.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.releaseversion.entity.QReleaseVersion;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ReleaseVersion Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ReleaseVersionRepositoryImpl implements ReleaseVersionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReleaseVersion> findVersionsBetween(String releaseType, String fromVersion,
            String toVersion) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;

        // From 버전 파싱
        String[] fromParts = fromVersion.split("\\.");
        int fromMajor = Integer.parseInt(fromParts[0]);
        int fromMinor = Integer.parseInt(fromParts[1]);
        int fromPatch = Integer.parseInt(fromParts[2]);

        // To 버전 파싱
        String[] toParts = toVersion.split("\\.");
        int toMajor = Integer.parseInt(toParts[0]);
        int toMinor = Integer.parseInt(toParts[1]);
        int toPatch = Integer.parseInt(toParts[2]);

        return queryFactory
                .selectFrom(rv)
                .where(rv.releaseType.eq(releaseType)
                        .and(
                                // fromVersion < version <= toVersion
                                rv.majorVersion.gt(fromMajor)
                                        .or(rv.majorVersion.eq(fromMajor)
                                                .and(rv.minorVersion.gt(fromMinor)))
                                        .or(rv.majorVersion.eq(fromMajor)
                                                .and(rv.minorVersion.eq(fromMinor))
                                                .and(rv.patchVersion.gt(fromPatch)))
                        )
                        .and(
                                // version <= toVersion
                                rv.majorVersion.lt(toMajor)
                                        .or(rv.majorVersion.eq(toMajor)
                                                .and(rv.minorVersion.lt(toMinor)))
                                        .or(rv.majorVersion.eq(toMajor)
                                                .and(rv.minorVersion.eq(toMinor))
                                                .and(rv.patchVersion.loe(toPatch)))
                        )
                )
                .orderBy(
                        rv.majorVersion.asc(),
                        rv.minorVersion.asc(),
                        rv.patchVersion.asc()
                )
                .fetch();
    }

    @Override
    public Optional<ReleaseVersion> findLatestByReleaseTypeAndCategory(String releaseType,
            ReleaseCategory releaseCategory) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;

        ReleaseVersion result = queryFactory
                .selectFrom(rv)
                .where(
                        rv.releaseType.eq(releaseType),
                        rv.releaseCategory.eq(releaseCategory)
                )
                .orderBy(rv.createdAt.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<ReleaseVersion> findRecentByReleaseTypeAndCategory(String releaseType,
            ReleaseCategory releaseCategory, int limit) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;

        return queryFactory
                .selectFrom(rv)
                .where(
                        rv.releaseType.eq(releaseType),
                        rv.releaseCategory.eq(releaseCategory)
                )
                .orderBy(rv.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<ReleaseVersion> findLatestByProjectIdAndReleaseTypeAndCategory(String projectId,
            String releaseType, ReleaseCategory releaseCategory) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;

        ReleaseVersion result = queryFactory
                .selectFrom(rv)
                .where(
                        rv.project.projectId.eq(projectId),
                        rv.releaseType.eq(releaseType),
                        rv.releaseCategory.eq(releaseCategory)
                )
                .orderBy(rv.createdAt.desc())
                .limit(1)
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<ReleaseVersion> findRecentByProjectIdAndReleaseTypeAndCategory(String projectId,
            String releaseType, ReleaseCategory releaseCategory, int limit) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;

        return queryFactory
                .selectFrom(rv)
                .where(
                        rv.project.projectId.eq(projectId),
                        rv.releaseType.eq(releaseType),
                        rv.releaseCategory.eq(releaseCategory)
                )
                .orderBy(rv.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
