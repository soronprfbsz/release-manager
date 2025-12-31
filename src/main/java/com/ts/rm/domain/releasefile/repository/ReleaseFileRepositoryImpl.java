package com.ts.rm.domain.releasefile.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.releasefile.entity.QReleaseFile;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import java.util.List;
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
    public List<ReleaseFile> findReleaseFilesBetweenVersionsExcludingInstall(String projectId, String fromVersion, String toVersion) {
        QReleaseFile rf = QReleaseFile.releaseFile;

        return queryFactory
                .selectFrom(rf)
                .where(
                        rf.releaseVersion.project.projectId.eq(projectId),  // 프로젝트 ID 필터링 추가
                        rf.releaseVersion.version.goe(fromVersion),
                        rf.releaseVersion.version.loe(toVersion),
                        rf.releaseVersion.releaseCategory.isNull()
                                .or(rf.releaseVersion.releaseCategory.ne(ReleaseCategory.INSTALL))
                )
                .orderBy(
                        rf.releaseVersion.version.asc(),
                        rf.executionOrder.asc()
                )
                .fetch();
    }

    @Override
    public List<ReleaseFile> findReleaseFilesBetweenVersionsBySubCategory(String projectId, String fromVersion, String toVersion, String subCategory) {
        QReleaseFile rf = QReleaseFile.releaseFile;

        return queryFactory
                .selectFrom(rf)
                .where(
                        rf.releaseVersion.project.projectId.eq(projectId),  // 프로젝트 ID 필터링 추가
                        rf.releaseVersion.version.goe(fromVersion),
                        rf.releaseVersion.version.loe(toVersion),
                        rf.subCategory.equalsIgnoreCase(subCategory)
                )
                .orderBy(
                        rf.releaseVersion.version.asc(),
                        rf.executionOrder.asc()
                )
                .fetch();
    }

    @Override
    public List<ReleaseFile> findBuildArtifactsBetweenVersions(String projectId, String fromVersion, String toVersion) {
        QReleaseFile rf = QReleaseFile.releaseFile;

        return queryFactory
                .selectFrom(rf)
                .where(
                        rf.releaseVersion.project.projectId.eq(projectId),  // 프로젝트 ID 필터링 추가
                        rf.releaseVersion.version.goe(fromVersion),
                        rf.releaseVersion.version.loe(toVersion),
                        rf.fileCategory.in(FileCategory.WEB, FileCategory.ENGINE)
                )
                .orderBy(
                        rf.releaseVersion.version.desc(),
                        rf.executionOrder.asc()
                )
                .fetch();
    }

    @Override
    public List<FileCategory> findCategoriesByVersionId(Long releaseVersionId) {
        QReleaseFile rf = QReleaseFile.releaseFile;

        return queryFactory
                .selectDistinct(rf.fileCategory)
                .from(rf)
                .where(
                        rf.releaseVersion.releaseVersionId.eq(releaseVersionId),
                        rf.fileCategory.isNotNull()
                )
                .orderBy(rf.fileCategory.asc())
                .fetch();
    }
}
