package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.project.repository.ProjectRepository;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.mapper.ReleaseVersionDtoMapper;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionHierarchyRepository;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.domain.releaseversion.util.VersionParser;
import com.ts.rm.domain.releaseversion.util.VersionParser.VersionInfo;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReleaseVersion Service
 *
 * <p>릴리즈 버전 관리 비즈니스 로직 (CRUD 및 기본 조회)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReleaseVersionService {

    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseFileRepository releaseFileRepository;
    private final ReleaseVersionHierarchyRepository hierarchyRepository;
    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;
    private final ReleaseVersionDtoMapper mapper;

    // 분리된 서비스들
    private final ReleaseVersionFileSystemService fileSystemService;
    private final ReleaseVersionTreeService treeService;

    /**
     * 표준 릴리즈 버전 생성
     *
     * @param request 버전 생성 요청
     * @return 생성된 버전 상세 정보
     */
    @Transactional
    public ReleaseVersionDto.DetailResponse createStandardVersion(
            ReleaseVersionDto.CreateRequest request) {
        log.info("Creating standard release version: {}", request.version());

        // 버전 생성
        return createVersion("STANDARD", null, request);
    }

    /**
     * 커스텀 릴리즈 버전 생성
     *
     * @param request 버전 생성 요청
     * @return 생성된 버전 상세 정보
     */
    @Transactional
    public ReleaseVersionDto.DetailResponse createCustomVersion(
            ReleaseVersionDto.CreateRequest request) {
        log.info("Creating custom release version: {} for customerId: {}",
                request.version(), request.customerId());

        // 고객사 ID 필수 검증
        if (request.customerId() == null) {
            throw new BusinessException(ErrorCode.CUSTOMER_ID_REQUIRED);
        }

        // Customer 조회
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        // 버전 생성
        return createVersion("CUSTOM", customer, request);
    }

    /**
     * 릴리즈 버전 조회 (ID)
     *
     * @param versionId 버전 ID
     * @return 버전 상세 정보
     */
    public ReleaseVersionDto.DetailResponse getVersionById(Long versionId) {
        ReleaseVersion version = findVersionById(versionId);
        return mapper.toDetailResponse(version);
    }

    /**
     * 타입별 버전 목록 조회
     *
     * @param typeName 릴리즈 타입 (standard/custom)
     * @return 버전 목록
     */
    public List<ReleaseVersionDto.SimpleResponse> getVersionsByType(String typeName) {
        String releaseType = typeName.toUpperCase();
        List<ReleaseVersion> versions = releaseVersionRepository
                .findAllByReleaseTypeOrderByCreatedAtDesc(releaseType);
        List<ReleaseVersionDto.SimpleResponse> responses = mapper.toSimpleResponseList(versions);
        return enrichWithCategories(responses);
    }

    /**
     * 버전 범위 조회
     *
     * @param projectId   프로젝트 ID
     * @param typeName    릴리즈 타입
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 버전 목록
     */
    public List<ReleaseVersionDto.SimpleResponse> getVersionsBetween(String projectId, String typeName,
            String fromVersion, String toVersion) {
        String releaseType = typeName.toUpperCase();
        List<ReleaseVersion> versions = releaseVersionRepository.findVersionsBetween(
                projectId, releaseType, fromVersion, toVersion);
        List<ReleaseVersionDto.SimpleResponse> responses = mapper.toSimpleResponseList(versions);
        return enrichWithCategories(responses);
    }

    /**
     * 버전 정보 수정
     *
     * @param versionId 버전 ID
     * @param request   수정 요청
     * @return 수정된 버전 상세 정보
     */
    @Transactional
    public ReleaseVersionDto.DetailResponse updateVersion(Long versionId,
            ReleaseVersionDto.UpdateRequest request) {
        log.info("Updating release version with versionId: {}", versionId);

        // 엔티티 조회
        ReleaseVersion releaseVersion = findVersionById(versionId);

        // Setter를 통한 수정 (JPA Dirty Checking)
        if (request.comment() != null) {
            releaseVersion.setComment(request.comment());
        }

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Release version updated successfully with versionId: {}", versionId);
        return mapper.toDetailResponse(releaseVersion);
    }

    /**
     * 버전 삭제
     *
     * @param versionId 버전 ID
     */
    @Transactional
    public void deleteVersion(Long versionId) {
        log.info("버전 삭제 시작 - versionId: {}", versionId);

        // 1. 버전 존재 검증
        ReleaseVersion version = findVersionById(versionId);
        String versionNumber = version.getVersion();
        String releaseType = version.getReleaseType();

        try {
            // 2. release_file 삭제 (명시적으로)
            List<ReleaseFile> releaseFiles = releaseFileRepository
                    .findAllByReleaseVersion_ReleaseVersionIdOrderByExecutionOrderAsc(versionId);
            releaseFileRepository.deleteAll(releaseFiles);
            log.info("release_file 삭제 완료 - {} 개", releaseFiles.size());

            // 3. release_version_hierarchy 삭제
            hierarchyRepository.deleteByDescendantId(versionId);
            hierarchyRepository.deleteByAncestorId(versionId);
            log.info("release_version_hierarchy 삭제 완료");

            // 4. release_version 삭제
            releaseVersionRepository.delete(version);
            log.info("release_version 삭제 완료");

            // 5. 파일 시스템 삭제
            fileSystemService.deleteVersionDirectory(version);

            log.info("버전 삭제 완료 - version: {}", versionNumber);

        } catch (Exception e) {
            log.error("버전 삭제 실패 - versionId: {}, version: {}", versionId, versionNumber, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "버전 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // === Private Helper Methods ===

    /**
     * 공통 버전 생성 로직
     */
    private ReleaseVersionDto.DetailResponse createVersion(String releaseType,
            Customer customer, ReleaseVersionDto.CreateRequest request) {

        // 프로젝트 ID 필수 검증
        if (request.projectId() == null || request.projectId().isBlank()) {
            throw new BusinessException(ErrorCode.PROJECT_ID_REQUIRED);
        }

        // Project 조회
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 버전 파싱
        VersionInfo versionInfo = VersionParser.parse(request.version());

        // 중복 검증 (프로젝트 내에서 동일 버전 확인)
        if (releaseVersionRepository.existsByProject_ProjectIdAndVersion(request.projectId(), request.version())) {
            throw new BusinessException(ErrorCode.RELEASE_VERSION_CONFLICT);
        }

        // Entity 생성
        ReleaseVersion version = ReleaseVersion.builder()
                .project(project)
                .releaseType(releaseType)
                .releaseCategory(request.releaseCategory() != null ? request.releaseCategory() : com.ts.rm.domain.releaseversion.enums.ReleaseCategory.PATCH)
                .customer(customer)
                .version(request.version())
                .majorVersion(versionInfo.getMajorVersion())
                .minorVersion(versionInfo.getMinorVersion())
                .patchVersion(versionInfo.getPatchVersion())
                .createdBy(request.createdBy())
                .comment(request.comment())
                .isApproved(request.isApproved() != null ? request.isApproved() : false)
                .customMajorVersion(request.customMajorVersion())
                .customMinorVersion(request.customMinorVersion())
                .customPatchVersion(request.customPatchVersion())
                .build();

        ReleaseVersion savedVersion = releaseVersionRepository.save(version);

        // 클로저 테이블에 계층 구조 데이터 추가
        treeService.createHierarchyForNewVersion(savedVersion, releaseType);

        // 디렉토리 구조 생성
        fileSystemService.createDirectoryStructure(savedVersion, customer);

        log.info("Release version created successfully with id: {}, projectId: {}",
                savedVersion.getReleaseVersionId(), project.getProjectId());
        return mapper.toDetailResponse(savedVersion);
    }

    /**
     * ReleaseVersion 조회 (존재하지 않으면 예외 발생)
     */
    public ReleaseVersion findVersionById(Long versionId) {
        return releaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND));
    }

    /**
     * SimpleResponse 리스트에 categories 필드 설정
     */
    private List<ReleaseVersionDto.SimpleResponse> enrichWithCategories(
            List<ReleaseVersionDto.SimpleResponse> responses) {
        return responses.stream()
                .map(this::enrichWithCategories)
                .toList();
    }

    /**
     * 단일 SimpleResponse에 fileCategories 필드 설정
     */
    private ReleaseVersionDto.SimpleResponse enrichWithCategories(
            ReleaseVersionDto.SimpleResponse response) {
        List<FileCategory> fileCategoryEnums = releaseFileRepository
                .findCategoriesByVersionId(response.releaseVersionId());

        List<String> fileCategories = fileCategoryEnums.stream()
                .map(FileCategory::getCode)
                .toList();

        return new ReleaseVersionDto.SimpleResponse(
                response.releaseVersionId(),
                response.projectId(),
                response.releaseType(),
                response.customerCode(),
                response.version(),
                response.hotfixVersion(),      // hotfixVersion
                response.isHotfix(),           // isHotfix
                response.fullVersion(),        // fullVersion
                response.majorMinor(),
                response.createdBy(),
                response.comment(),
                response.isApproved(),
                response.approvedBy(),
                response.approvedAt(),
                fileCategories,
                response.createdAt(),
                response.patchFileCount()
        );
    }

    /**
     * 프로젝트별 표준본 버전 목록 조회 (셀렉트박스용)
     *
     * @param projectId 프로젝트 ID
     * @return 표준본 버전 목록 (value: versionId, name: version)
     */
    public List<ReleaseVersionDto.VersionSelectOption> getStandardVersionsForSelect(String projectId) {
        log.info("표준본 버전 셀렉트박스 목록 조회 - projectId: {}", projectId);

        List<ReleaseVersion> versions = releaseVersionRepository
                .findAllByProject_ProjectIdAndReleaseTypeOrderByCreatedAtDesc(projectId, "STANDARD");

        return versions.stream()
                .map(v -> new ReleaseVersionDto.VersionSelectOption(
                        v.getReleaseVersionId(),  // versionId
                        v.getVersion(),           // version
                        v.getIsApproved()         // isApproved
                ))
                .toList();
    }

    /**
     * 릴리즈 버전 승인
     *
     * @param versionId  버전 ID
     * @param approvedBy 승인자 이메일
     * @return 승인된 버전 상세 정보
     */
    @Transactional
    public ReleaseVersionDto.DetailResponse approveReleaseVersion(Long versionId, String approvedBy) {
        log.info("릴리즈 버전 승인 요청 - versionId: {}, approvedBy: {}", versionId, approvedBy);

        // 엔티티 조회
        ReleaseVersion releaseVersion = findVersionById(versionId);

        // 승인 처리
        releaseVersion.setIsApproved(true);
        releaseVersion.setApprovedBy(approvedBy);
        releaseVersion.setApprovedAt(LocalDateTime.now());

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("릴리즈 버전 승인 완료 - versionId: {}, approvedBy: {}, approvedAt: {}",
                versionId, approvedBy, releaseVersion.getApprovedAt());
        return mapper.toDetailResponse(releaseVersion);
    }

    // ========================================
    // Hotfix 관련 메서드
    // ========================================

    /**
     * 핫픽스 생성
     *
     * @param parentVersionId 원본 버전 ID
     * @param comment         코멘트
     * @param createdBy       생성자
     * @return 생성된 핫픽스 버전 상세 정보
     */
    @Transactional
    public ReleaseVersionDto.CreateHotfixResponse createHotfix(Long parentVersionId, String comment, String createdBy) {
        log.info("핫픽스 생성 요청 - parentVersionId: {}, createdBy: {}", parentVersionId, createdBy);

        // 1. 원본 버전 조회
        ReleaseVersion parentVersion = findVersionById(parentVersionId);

        // 2. 원본 버전이 핫픽스인지 확인 (핫픽스의 핫픽스는 불가)
        if (parentVersion.isHotfix()) {
            throw new BusinessException(ErrorCode.INVALID_HOTFIX_PARENT,
                    "핫픽스 버전에는 추가 핫픽스를 생성할 수 없습니다.");
        }

        // 3. 다음 핫픽스 버전 번호 결정
        Integer maxHotfixVersion = releaseVersionRepository.findMaxHotfixVersionByParentVersionId(parentVersionId);
        int nextHotfixVersion = maxHotfixVersion + 1;

        // 4. 핫픽스 버전 생성
        ReleaseVersion hotfixVersion = ReleaseVersion.builder()
                .project(parentVersion.getProject())
                .releaseType(parentVersion.getReleaseType())
                .releaseCategory(parentVersion.getReleaseCategory())
                .customer(parentVersion.getCustomer())
                .version(parentVersion.getVersion())  // 기본 버전은 동일
                .majorVersion(parentVersion.getMajorVersion())
                .minorVersion(parentVersion.getMinorVersion())
                .patchVersion(parentVersion.getPatchVersion())
                .hotfixVersion(nextHotfixVersion)
                .parentVersion(parentVersion)
                .createdBy(createdBy)
                .comment(comment)
                .isApproved(false)
                .customMajorVersion(parentVersion.getCustomMajorVersion())
                .customMinorVersion(parentVersion.getCustomMinorVersion())
                .customPatchVersion(parentVersion.getCustomPatchVersion())
                .baseVersion(parentVersion.getBaseVersion())
                .build();

        ReleaseVersion savedHotfix = releaseVersionRepository.save(hotfixVersion);

        // 5. 핫픽스용 디렉토리 생성
        fileSystemService.createHotfixDirectoryStructure(savedHotfix, parentVersion);

        log.info("핫픽스 생성 완료 - hotfixVersionId: {}, fullVersion: {}",
                savedHotfix.getReleaseVersionId(), savedHotfix.getFullVersion());

        return new ReleaseVersionDto.CreateHotfixResponse(
                savedHotfix.getReleaseVersionId(),
                savedHotfix.getProject().getProjectId(),
                parentVersionId,
                parentVersion.getVersion(),
                savedHotfix.getMajorVersion(),
                savedHotfix.getMinorVersion(),
                savedHotfix.getPatchVersion(),
                savedHotfix.getHotfixVersion(),
                savedHotfix.getFullVersion(),
                savedHotfix.getMajorMinor(),
                savedHotfix.getCreatedBy(),
                savedHotfix.getComment(),
                savedHotfix.getCreatedAt(),
                List.of()  // 아직 파일이 없음
        );
    }

    /**
     * 특정 버전의 핫픽스 목록 조회
     *
     * @param parentVersionId 원본 버전 ID
     * @return 핫픽스 목록 응답
     */
    public ReleaseVersionDto.HotfixListResponse getHotfixesByParentVersionId(Long parentVersionId) {
        log.info("핫픽스 목록 조회 - parentVersionId: {}", parentVersionId);

        // 1. 원본 버전 조회
        ReleaseVersion parentVersion = findVersionById(parentVersionId);

        // 2. 핫픽스 목록 조회
        List<ReleaseVersion> hotfixes = releaseVersionRepository
                .findAllByParentVersion_ReleaseVersionIdOrderByHotfixVersionAsc(parentVersionId);

        // 3. DTO 변환 및 카테고리 정보 추가
        List<ReleaseVersionDto.HotfixItem> hotfixItems = hotfixes.stream()
                .map(this::toHotfixItemWithCategories)
                .toList();

        return new ReleaseVersionDto.HotfixListResponse(
                parentVersionId,
                parentVersion.getVersion(),
                hotfixItems
        );
    }

    /**
     * 핫픽스 항목 변환 (카테고리 정보 포함)
     */
    private ReleaseVersionDto.HotfixItem toHotfixItemWithCategories(ReleaseVersion hotfix) {
        List<FileCategory> fileCategoryEnums = releaseFileRepository
                .findCategoriesByVersionId(hotfix.getReleaseVersionId());

        List<String> fileCategories = fileCategoryEnums.stream()
                .map(FileCategory::getCode)
                .toList();

        return new ReleaseVersionDto.HotfixItem(
                hotfix.getReleaseVersionId(),
                hotfix.getHotfixVersion(),
                hotfix.getFullVersion(),
                hotfix.getCreatedAt() != null ? hotfix.getCreatedAt().toLocalDate().toString() : null,
                hotfix.getCreatedBy(),
                hotfix.getComment(),
                hotfix.getIsApproved(),
                fileCategories
        );
    }

    /**
     * 버전에 핫픽스가 존재하는지 확인
     *
     * @param parentVersionId 원본 버전 ID
     * @return 핫픽스 존재 여부
     */
    public boolean hasHotfixes(Long parentVersionId) {
        return releaseVersionRepository.existsByParentVersion_ReleaseVersionId(parentVersionId);
    }

    /**
     * 핫픽스 개수 조회
     *
     * @param parentVersionId 원본 버전 ID
     * @return 핫픽스 개수
     */
    public Long getHotfixCount(Long parentVersionId) {
        return releaseVersionRepository.countHotfixesByParentVersionId(parentVersionId);
    }

    /**
     * 버전 범위 조회 (핫픽스 제외)
     *
     * @param projectId   프로젝트 ID
     * @param typeName    릴리즈 타입
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 버전 목록 (핫픽스 제외)
     */
    public List<ReleaseVersionDto.SimpleResponse> getVersionsBetweenExcludingHotfixes(
            String projectId, String typeName, String fromVersion, String toVersion) {
        String releaseType = typeName.toUpperCase();
        List<ReleaseVersion> versions = releaseVersionRepository.findVersionsBetweenExcludingHotfixes(
                projectId, releaseType, fromVersion, toVersion);
        List<ReleaseVersionDto.SimpleResponse> responses = mapper.toSimpleResponseList(versions);
        return enrichWithCategories(responses);
    }
}
