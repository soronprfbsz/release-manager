package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto.FileTreeNode;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHierarchy;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionHierarchyRepository;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReleaseVersion Tree Service
 *
 * <p>릴리즈 버전의 트리/계층 구조 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReleaseVersionTreeService {

    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseVersionHierarchyRepository hierarchyRepository;
    private final ReleaseFileRepository releaseFileRepository;

    /**
     * 표준 릴리즈 버전 트리 조회 (프로젝트별)
     *
     * @param projectId 프로젝트 ID
     * @return 릴리즈 버전 트리
     */
    public ReleaseVersionDto.TreeResponse getStandardReleaseTree(String projectId) {
        log.info("Getting standard release tree for project: {}", projectId);
        return buildReleaseTree(projectId, "STANDARD", null);
    }

    /**
     * 커스텀 릴리즈 버전 트리 조회 (프로젝트별)
     *
     * @param projectId    프로젝트 ID
     * @param customerCode 고객사 코드
     * @return 릴리즈 버전 트리
     */
    public ReleaseVersionDto.TreeResponse getCustomReleaseTree(String projectId, String customerCode) {
        log.info("Getting custom release tree for project: {}, customer: {}", projectId, customerCode);
        return buildReleaseTree(projectId, "CUSTOM", customerCode);
    }

    /**
     * 릴리즈 버전 트리 빌드 (DB 기반)
     *
     * @param projectId    프로젝트 ID
     * @param releaseType  릴리즈 타입 (STANDARD, CUSTOM)
     * @param customerCode 고객사 코드 (CUSTOM인 경우 필수)
     * @return 릴리즈 버전 트리
     */
    private ReleaseVersionDto.TreeResponse buildReleaseTree(String projectId, String releaseType,
            String customerCode) {
        try {
            // 클로저 테이블을 통한 버전 조회
            List<ReleaseVersion> versions;
            if ("CUSTOM".equals(releaseType) && customerCode != null) {
                versions = hierarchyRepository.findAllByProjectIdAndReleaseTypeAndCustomerWithHierarchy(
                        projectId, releaseType, customerCode);
            } else {
                versions = hierarchyRepository.findAllByProjectIdAndReleaseTypeWithHierarchy(
                        projectId, releaseType);
            }

            if (versions.isEmpty()) {
                log.warn("No versions found for projectId: {}, releaseType: {}, customerCode: {}",
                        projectId, releaseType, customerCode);
                return new ReleaseVersionDto.TreeResponse(releaseType, customerCode, List.of());
            }

            // Major.Minor 그룹으로 묶기
            List<ReleaseVersionDto.MajorMinorNode> majorMinorGroups = buildMajorMinorGroupsFromDb(
                    versions);

            return new ReleaseVersionDto.TreeResponse(releaseType, customerCode, majorMinorGroups);

        } catch (Exception e) {
            log.error("Failed to build release tree", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "릴리즈 트리 조회 중 오류가 발생했습니다");
        }
    }

    /**
     * DB에서 조회한 버전들을 Major.Minor로 그룹핑 (DB 기반)
     *
     * @param versions 릴리즈 버전 목록
     * @return Major.Minor 그룹 목록
     */
    public List<ReleaseVersionDto.MajorMinorNode> buildMajorMinorGroupsFromDb(
            List<ReleaseVersion> versions) {

        // Major.Minor로 그룹핑
        java.util.Map<String, List<ReleaseVersion>> groupedByMajorMinor = new java.util.LinkedHashMap<>();

        for (ReleaseVersion version : versions) {
            groupedByMajorMinor.computeIfAbsent(version.getMajorMinor(), k -> new ArrayList<>())
                    .add(version);
        }

        // MajorMinorNode 생성
        List<ReleaseVersionDto.MajorMinorNode> majorMinorNodes = new ArrayList<>();

        for (java.util.Map.Entry<String, List<ReleaseVersion>> entry : groupedByMajorMinor.entrySet()) {
            String majorMinor = entry.getKey();
            List<ReleaseVersion> versionsInGroup = entry.getValue();

            // 그룹 내에서 패치 버전 내림차순 정렬
            versionsInGroup.sort((v1, v2) -> Integer.compare(v2.getPatchVersion(), v1.getPatchVersion()));

            // 각 버전에 대한 VersionNode 생성
            List<ReleaseVersionDto.VersionNode> versionNodes = versionsInGroup.stream()
                    .map(this::buildVersionNodeFromDb)
                    .toList();

            majorMinorNodes.add(new ReleaseVersionDto.MajorMinorNode(majorMinor, versionNodes));
        }

        return majorMinorNodes;
    }

    /**
     * ReleaseVersion 엔티티로부터 VersionNode 생성 (DB 기반)
     *
     * @param version 릴리즈 버전 엔티티
     * @return VersionNode
     */
    public ReleaseVersionDto.VersionNode buildVersionNodeFromDb(ReleaseVersion version) {
        // createdAt을 "YYYY-MM-DD" 형식으로 포맷
        String createdAt = version.getCreatedAt() != null
                ? version.getCreatedAt().toLocalDate().toString()
                : null;

        // fileCategories 조회
        List<FileCategory> fileCategoryEnums = releaseFileRepository
                .findCategoriesByVersionId(version.getReleaseVersionId());
        List<String> fileCategories = fileCategoryEnums.stream()
                .map(FileCategory::getCode)
                .toList();

        // approvedAt 포매팅
        String approvedAt = version.getApprovedAt() != null
                ? version.getApprovedAt().toLocalDate().toString()
                : null;

        return new ReleaseVersionDto.VersionNode(
                version.getReleaseVersionId(),
                version.getVersion(),
                createdAt,
                version.getCreatedBy(),
                version.getComment(),
                version.getIsApproved(),
                version.getApprovedBy(),
                approvedAt,
                fileCategories
        );
    }

    /**
     * 새 버전에 대한 계층 구조 데이터 생성 (클로저 테이블)
     *
     * @param newVersion  새로 생성된 버전
     * @param releaseType 릴리즈 타입
     */
    @Transactional
    public void createHierarchyForNewVersion(ReleaseVersion newVersion, String releaseType) {
        // 1. 자기 자신과의 관계 (depth=0) - 필수
        ReleaseVersionHierarchy selfRelation = ReleaseVersionHierarchy.builder()
                .ancestor(newVersion)
                .descendant(newVersion)
                .depth(0)
                .build();
        hierarchyRepository.save(selfRelation);

        // 2. 이전 버전들과의 관계 설정 (선택적 - 버전 순서 기반)
        List<ReleaseVersion> previousVersions = releaseVersionRepository
                .findAllByReleaseTypeOrderByCreatedAtDesc(releaseType);

        int depth = 1;
        for (ReleaseVersion prevVersion : previousVersions) {
            // 자기 자신은 제외
            if (prevVersion.getReleaseVersionId().equals(newVersion.getReleaseVersionId())) {
                continue;
            }

            // 이전 버전 -> 새 버전 관계 생성
            ReleaseVersionHierarchy relation = ReleaseVersionHierarchy.builder()
                    .ancestor(prevVersion)
                    .descendant(newVersion)
                    .depth(depth++)
                    .build();
            hierarchyRepository.save(relation);
        }

        log.info("Hierarchy data created for version: {}", newVersion.getVersion());
    }

    /**
     * 릴리즈 버전의 파일 트리 구조 조회
     *
     * @param versionId 릴리즈 버전 ID
     * @param version   릴리즈 버전 엔티티
     * @return 파일 트리 응답
     */
    public ReleaseVersionDto.FileTreeResponse getVersionFileTree(Long versionId, ReleaseVersion version) {
        // 모든 파일 조회 (relativePath 순으로 정렬)
        List<ReleaseFile> files = releaseFileRepository.findAllByReleaseVersion_ReleaseVersionIdOrderByExecutionOrderAsc(versionId);

        // 파일 트리 생성
        ReleaseVersionDto.FileTreeNode rootNode = buildFileTree(files);

        return new ReleaseVersionDto.FileTreeResponse(
                version.getReleaseVersionId(),
                version.getVersion(),
                rootNode
        );
    }

    /**
     * ReleaseFile 목록으로부터 파일 트리 구조 생성
     *
     * @param files ReleaseFile 목록
     * @return 루트 FileTreeNode
     */
    public ReleaseVersionDto.FileTreeNode buildFileTree(List<ReleaseFile> files) {
        // 루트 노드 생성
        Map<String, FileTreeNode> nodeMap = new java.util.HashMap<>();

        // 루트 노드를 빈 경로로 시작
        List<ReleaseVersionDto.FileTreeNode> rootChildren = new ArrayList<>();

        for (ReleaseFile file : files) {
            String relativePath = file.getRelativePath();
            if (relativePath == null || relativePath.isEmpty()) {
                continue;
            }

            // 경로를 / 로 분리 (예: /mariadb/01.sql -> ["", "mariadb", "01.sql"])
            String[] parts = relativePath.split("/");

            // 현재 경로 추적
            StringBuilder currentPath = new StringBuilder();
            List<ReleaseVersionDto.FileTreeNode> currentChildren = rootChildren;

            // 각 경로 부분을 순회하며 트리 구축
            for (int i = 1; i < parts.length; i++) {  // i=0은 빈 문자열이므로 건너뜀
                String part = parts[i];
                currentPath.append("/").append(part);
                String pathKey = currentPath.toString();

                // 마지막 부분 (파일)인지 확인
                boolean isFile = (i == parts.length - 1);

                if (isFile) {
                    // 파일 노드 생성
                    ReleaseVersionDto.FileTreeNode fileNode = ReleaseVersionDto.FileTreeNode.file(
                            part,
                            pathKey,
                            file.getFileSize(),
                            file.getReleaseFileId()
                    );
                    currentChildren.add(fileNode);
                } else {
                    // 디렉토리 노드 처리
                    if (!nodeMap.containsKey(pathKey)) {
                        // 새 디렉토리 노드 생성
                        List<ReleaseVersionDto.FileTreeNode> newChildren = new ArrayList<>();
                        ReleaseVersionDto.FileTreeNode dirNode = ReleaseVersionDto.FileTreeNode.directory(
                                part,
                                pathKey,
                                newChildren
                        );
                        nodeMap.put(pathKey, dirNode);
                        currentChildren.add(dirNode);
                        currentChildren = newChildren;
                    } else {
                        // 기존 디렉토리 노드 사용
                        ReleaseVersionDto.FileTreeNode existingNode = nodeMap.get(pathKey);
                        currentChildren = existingNode.children();
                    }
                }
            }
        }

        // 루트 노드 반환
        return ReleaseVersionDto.FileTreeNode.directory("", "/", rootChildren);
    }
}
