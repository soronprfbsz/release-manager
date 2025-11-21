package com.ts.rm.domain.releaseversion.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.releaseversion.entity.QReleaseVersion;
import com.ts.rm.domain.releaseversion.entity.QReleaseVersionHierarchy;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ReleaseVersionHierarchy Custom Repository Implementation
 *
 * <p>클로저 테이블 기반 계층 구조 조회 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ReleaseVersionHierarchyRepositoryImpl implements
        ReleaseVersionHierarchyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ReleaseVersion> findAllByReleaseTypeWithHierarchy(String releaseType) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;
        QReleaseVersionHierarchy h = QReleaseVersionHierarchy.releaseVersionHierarchy;

        // 클로저 테이블을 통해 계층 구조를 고려한 조회
        // depth=0 (자기 자신)인 레코드를 통해 모든 버전 조회
        return queryFactory
                .selectDistinct(rv)
                .from(h)
                .innerJoin(h.descendant, rv)
                .where(
                        h.depth.eq(0),  // 자기 자신 관계만
                        rv.releaseType.eq(releaseType)
                )
                .orderBy(
                        rv.majorVersion.asc(),
                        rv.minorVersion.asc(),
                        rv.patchVersion.asc()
                )
                .fetch();
    }

    @Override
    public List<ReleaseVersion> findAllByReleaseTypeAndCustomerWithHierarchy(String releaseType,
            String customerCode) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;
        QReleaseVersionHierarchy h = QReleaseVersionHierarchy.releaseVersionHierarchy;

        return queryFactory
                .selectDistinct(rv)
                .from(h)
                .innerJoin(h.descendant, rv)
                .leftJoin(rv.customer).fetchJoin()
                .where(
                        h.depth.eq(0),
                        rv.releaseType.eq(releaseType),
                        rv.customer.customerCode.eq(customerCode)
                )
                .orderBy(
                        rv.majorVersion.asc(),
                        rv.minorVersion.asc(),
                        rv.patchVersion.asc()
                )
                .fetch();
    }

    @Override
    public List<ReleaseVersion> findAllByMajorMinorWithHierarchy(String releaseType,
            String majorMinor) {
        QReleaseVersion rv = QReleaseVersion.releaseVersion;
        QReleaseVersionHierarchy h = QReleaseVersionHierarchy.releaseVersionHierarchy;

        return queryFactory
                .selectDistinct(rv)
                .from(h)
                .innerJoin(h.descendant, rv)
                .where(
                        h.depth.eq(0),
                        rv.releaseType.eq(releaseType),
                        rv.majorMinor.eq(majorMinor)
                )
                .orderBy(rv.patchVersion.desc())  // 내림차순으로 변경
                .fetch();
    }
}
