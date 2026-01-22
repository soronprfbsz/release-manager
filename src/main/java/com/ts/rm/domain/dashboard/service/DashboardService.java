package com.ts.rm.domain.dashboard.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.dashboard.dto.DashboardDto;
import com.ts.rm.domain.dashboard.dto.DashboardDto.RecentCustomVersion;
import com.ts.rm.domain.dashboard.dto.DashboardDto.RecentPatch;
import com.ts.rm.domain.dashboard.dto.DashboardDto.RecentVersion;
import com.ts.rm.domain.patch.entity.PatchHistory;
import com.ts.rm.domain.patch.repository.PatchHistoryRepository;
import com.ts.rm.domain.patch.repository.PatchRepository;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final PatchHistoryRepository patchHistoryRepository;
    private final PatchRepository patchRepository;

    private static final String RELEASE_TYPE_STANDARD = "STANDARD";
    private static final String RELEASE_TYPE_CUSTOM = "CUSTOM";

    /**
     * 표준본 최신 릴리즈 버전 조회
     *
     * @param projectId 프로젝트 ID
     * @param limit     조회 개수
     * @return 표준본 최신 릴리즈 버전 응답
     */
    public DashboardDto.RecentStandardResponse getRecentStandardVersions(String projectId, int limit) {
        log.info("표준본 최신 릴리즈 버전 조회 - projectId: {}, limit: {}", projectId, limit);

        List<RecentVersion> versions = releaseVersionRepository
                .findRecentByProjectIdAndReleaseType(projectId, RELEASE_TYPE_STANDARD, limit)
                .stream()
                .map(this::toRecentVersion)
                .toList();

        log.info("표준본 최신 릴리즈 버전 조회 완료 - {}개", versions.size());
        return new DashboardDto.RecentStandardResponse(versions);
    }

    /**
     * 커스텀본 최신 릴리즈 버전 조회
     *
     * @param projectId 프로젝트 ID
     * @param limit     조회 개수
     * @return 커스텀본 최신 릴리즈 버전 응답
     */
    public DashboardDto.RecentCustomResponse getRecentCustomVersions(String projectId, int limit) {
        log.info("커스텀본 최신 릴리즈 버전 조회 - projectId: {}, limit: {}", projectId, limit);

        List<RecentCustomVersion> versions = releaseVersionRepository
                .findRecentByProjectIdAndReleaseType(projectId, RELEASE_TYPE_CUSTOM, limit)
                .stream()
                .map(this::toRecentCustomVersion)
                .toList();

        log.info("커스텀본 최신 릴리즈 버전 조회 완료 - {}개", versions.size());
        return new DashboardDto.RecentCustomResponse(versions);
    }

    /**
     * 최근 생성 패치 조회 (표준+커스텀)
     *
     * @param projectId 프로젝트 ID
     * @param limit     조회 개수
     * @return 최근 생성 패치 응답
     */
    public DashboardDto.RecentPatchResponse getRecentPatches(String projectId, int limit) {
        log.info("최근 생성 패치 조회 (표준+커스텀) - projectId: {}, limit: {}", projectId, limit);

        // 1. 최근 패치 이력 조회 (표준+커스텀)
        List<PatchHistory> patchHistories = patchHistoryRepository
                .findRecentByProjectId(projectId, limit);

        // 2. 패치 파일 존재 여부 확인
        Set<String> patchNames = patchHistories.stream()
                .map(PatchHistory::getPatchName)
                .collect(Collectors.toSet());
        Set<String> existingPatchNames = patchRepository.findExistingPatchNames(patchNames);

        // 3. RecentPatch 변환
        List<RecentPatch> patches = patchHistories.stream()
                .map(ph -> toRecentPatch(ph, existingPatchNames))
                .toList();

        log.info("최근 생성 패치 조회 완료 - {}개", patches.size());
        return new DashboardDto.RecentPatchResponse(patches);
    }

    /**
     * ReleaseVersion -> RecentVersion 변환
     */
    private RecentVersion toRecentVersion(ReleaseVersion rv) {
        // 해당 버전의 파일 카테고리 목록 조회 (fileCategory가 null인 경우 제외)
        List<String> fileCategories = rv.getReleaseFiles().stream()
                .filter(rf -> rf.getFileCategory() != null)
                .map(rf -> rf.getFileCategory().name())
                .distinct()
                .sorted()
                .toList();

        return new RecentVersion(
                rv.getReleaseVersionId(),
                rv.getVersion(),
                rv.getReleaseType(),
                rv.getCreatedAt(),
                rv.getComment(),
                fileCategories,
                rv.getCreatedByName(),
                rv.getCreatedByEmail(),
                rv.getCreatedByAvatarStyle(),
                rv.getCreatedByAvatarSeed()
        );
    }

    /**
     * ReleaseVersion -> RecentCustomVersion 변환 (고객사 정보 포함)
     */
    private RecentCustomVersion toRecentCustomVersion(ReleaseVersion rv) {
        // 해당 버전의 파일 카테고리 목록 조회 (fileCategory가 null인 경우 제외)
        List<String> fileCategories = rv.getReleaseFiles().stream()
                .filter(rf -> rf.getFileCategory() != null)
                .map(rf -> rf.getFileCategory().name())
                .distinct()
                .sorted()
                .toList();

        // 고객사 정보
        Customer customer = rv.getCustomer();
        Long customerId = customer != null ? customer.getCustomerId() : null;
        String customerCode = customer != null ? customer.getCustomerCode() : null;
        String customerName = customer != null ? customer.getCustomerName() : null;

        return new RecentCustomVersion(
                rv.getReleaseVersionId(),
                rv.getVersion(),
                rv.getReleaseType(),
                rv.getCreatedAt(),
                rv.getComment(),
                fileCategories,
                customerId,
                customerCode,
                customerName,
                rv.getCreatedByName(),
                rv.getCreatedByEmail(),
                rv.getCreatedByAvatarStyle(),
                rv.getCreatedByAvatarSeed()
        );
    }

    /**
     * PatchHistory -> RecentPatch 변환
     *
     * <p>patch_file 테이블은 용량 문제로 삭제될 수 있으므로
     * patch_history 테이블을 사용하여 최근 패치 정보 조회
     *
     * @param patchHistory       패치 이력
     * @param existingPatchNames 존재하는 패치명 Set (patch_file 테이블)
     * @return RecentPatch DTO
     */
    private RecentPatch toRecentPatch(PatchHistory patchHistory, Set<String> existingPatchNames) {
        // 고객사 정보
        Customer customer = patchHistory.getCustomer();
        Long customerId = customer != null ? customer.getCustomerId() : null;
        String customerCode = customer != null ? customer.getCustomerCode() : null;
        String customerName = customer != null ? customer.getCustomerName() : null;

        // 담당자 정보 (아바타 포함)
        Account assignee = patchHistory.getAssignee();
        String assigneeAvatarStyle = assignee != null ? assignee.getAvatarStyle() : null;
        String assigneeAvatarSeed = assignee != null ? assignee.getAvatarSeed() : null;

        // 생성자 정보 (아바타 포함)
        Account creator = patchHistory.getCreator();
        String createdByAvatarStyle = creator != null ? creator.getAvatarStyle() : null;
        String createdByAvatarSeed = creator != null ? creator.getAvatarSeed() : null;

        // 파일 삭제 여부 (patch_file 테이블에 존재하지 않으면 삭제됨)
        boolean fileDeleted = !existingPatchNames.contains(patchHistory.getPatchName());

        return new RecentPatch(
                patchHistory.getHistoryId(),
                patchHistory.getPatchName(),
                patchHistory.getFromVersion(),
                patchHistory.getToVersion(),
                patchHistory.getReleaseType(),
                patchHistory.getCreatedAt(),
                patchHistory.getDescription(),
                fileDeleted,
                customerId,
                customerCode,
                customerName,
                patchHistory.getAssigneeName(),
                patchHistory.getAssigneeEmail(),
                assigneeAvatarStyle,
                assigneeAvatarSeed,
                patchHistory.getCreatedByName(),
                patchHistory.getCreatedByEmail(),
                createdByAvatarStyle,
                createdByAvatarSeed
        );
    }
}
