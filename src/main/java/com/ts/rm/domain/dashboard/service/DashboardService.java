package com.ts.rm.domain.dashboard.service;

import com.ts.rm.domain.dashboard.dto.DashboardDto;
import com.ts.rm.domain.dashboard.dto.DashboardDto.LatestInstallVersion;
import com.ts.rm.domain.dashboard.dto.DashboardDto.RecentPatch;
import com.ts.rm.domain.dashboard.dto.DashboardDto.RecentVersion;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.repository.PatchRepository;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 대시보드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ReleaseVersionRepository releaseVersionRepository;
    private final PatchRepository patchRepository;

    private static final String RELEASE_TYPE_STANDARD = "STANDARD";

    /**
     * 프로젝트별 대시보드 최근 데이터 조회
     *
     * @param projectId    프로젝트 ID
     * @param versionLimit 최근 릴리즈 버전 조회 개수
     * @param patchLimit   최근 생성 패치 조회 개수
     * @return 대시보드 응답
     */
    public DashboardDto.Response getRecentData(String projectId, int versionLimit, int patchLimit) {
        log.info("프로젝트별 대시보드 최근 데이터 조회 시작 - projectId: {}, 버전: {}개, 패치: {}개",
                projectId, versionLimit, patchLimit);

        // 1. 최신 설치본 조회 (프로젝트 + STANDARD + INSTALL)
        LatestInstallVersion latestInstall = releaseVersionRepository
                .findLatestByProjectIdAndReleaseTypeAndCategory(projectId, RELEASE_TYPE_STANDARD,
                        ReleaseCategory.INSTALL)
                .map(this::toLatestInstallVersion)
                .orElse(null);

        // 2. 최근 릴리즈 버전 조회 (프로젝트 + STANDARD + PATCH)
        List<RecentVersion> recentVersions = releaseVersionRepository
                .findRecentByProjectIdAndReleaseTypeAndCategory(projectId, RELEASE_TYPE_STANDARD,
                        ReleaseCategory.PATCH, versionLimit)
                .stream()
                .map(this::toRecentVersion)
                .toList();

        // 3. 최근 생성 패치 조회 (프로젝트 + STANDARD)
        List<RecentPatch> recentPatches = patchRepository
                .findRecentByProjectIdAndReleaseType(projectId, RELEASE_TYPE_STANDARD, patchLimit)
                .stream()
                .map(this::toRecentPatch)
                .toList();

        log.info("프로젝트별 대시보드 최근 데이터 조회 완료 - 설치본: {}, 릴리즈 버전: {}개, 패치: {}개",
                latestInstall != null ? "존재" : "없음",
                recentVersions.size(),
                recentPatches.size());

        return new DashboardDto.Response(latestInstall, recentVersions, recentPatches);
    }

    /**
     * ReleaseVersion -> LatestInstallVersion 변환
     */
    private LatestInstallVersion toLatestInstallVersion(ReleaseVersion rv) {
        return new LatestInstallVersion(
                rv.getReleaseVersionId(),
                rv.getVersion(),
                rv.getReleaseType(),
                rv.getReleaseCategory().name(),
                rv.getCreatedAt(),
                rv.getCreatedBy(),
                rv.getComment()
        );
    }

    /**
     * ReleaseVersion -> RecentVersion 변환
     */
    private RecentVersion toRecentVersion(ReleaseVersion rv) {
        // 해당 버전의 파일 카테고리 목록 조회
        List<String> fileCategories = rv.getReleaseFiles().stream()
                .map(rf -> rf.getFileCategory().name())
                .distinct()
                .sorted()
                .toList();

        return new RecentVersion(
                rv.getReleaseVersionId(),
                rv.getVersion(),
                rv.getReleaseType(),
                rv.getReleaseCategory().name(),
                rv.getCreatedAt(),
                rv.getCreatedBy(),
                rv.getComment(),
                fileCategories
        );
    }

    /**
     * Patch -> RecentPatch 변환
     */
    private RecentPatch toRecentPatch(Patch patch) {
        return new RecentPatch(
                patch.getPatchId(),
                patch.getPatchName(),
                patch.getFromVersion(),
                patch.getToVersion(),
                patch.getReleaseType(),
                patch.getCreatedAt(),
                patch.getCreatedBy(),
                patch.getDescription()
        );
    }
}
