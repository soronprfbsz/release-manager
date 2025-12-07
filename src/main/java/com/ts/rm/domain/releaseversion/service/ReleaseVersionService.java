package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.mapper.ReleaseVersionDtoMapper;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionHierarchyRepository;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.domain.releaseversion.util.ReleaseMetadataManager;
import com.ts.rm.domain.releaseversion.util.VersionParser;
import com.ts.rm.domain.releaseversion.util.VersionParser.VersionInfo;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
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
    private final ReleaseVersionDtoMapper mapper;
    private final ReleaseMetadataManager metadataManager;

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
     * @param typeName    릴리즈 타입
     * @param fromVersion 시작 버전
     * @param toVersion   종료 버전
     * @return 버전 목록
     */
    public List<ReleaseVersionDto.SimpleResponse> getVersionsBetween(String typeName,
            String fromVersion, String toVersion) {
        String releaseType = typeName.toUpperCase();
        List<ReleaseVersion> versions = releaseVersionRepository.findVersionsBetween(
                releaseType, fromVersion, toVersion);
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
                    .findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId);
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

            // 6. release_metadata.json에서 버전 정보 제거
            metadataManager.removeVersionEntry(releaseType, versionNumber);

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

        // 버전 파싱
        VersionInfo versionInfo = VersionParser.parse(request.version());

        // 중복 검증
        if (releaseVersionRepository.existsByVersion(request.version())) {
            throw new BusinessException(ErrorCode.RELEASE_VERSION_CONFLICT);
        }

        // Entity 생성
        ReleaseVersion version = ReleaseVersion.builder()
                .releaseType(releaseType)
                .releaseCategory(request.releaseCategory() != null ? request.releaseCategory() : com.ts.rm.domain.releaseversion.enums.ReleaseCategory.PATCH)
                .customer(customer)
                .version(request.version())
                .majorVersion(versionInfo.getMajorVersion())
                .minorVersion(versionInfo.getMinorVersion())
                .patchVersion(versionInfo.getPatchVersion())
                .createdBy(request.createdBy())
                .comment(request.comment())
                .customVersion(request.customVersion())
                .build();

        ReleaseVersion savedVersion = releaseVersionRepository.save(version);

        // 클로저 테이블에 계층 구조 데이터 추가
        treeService.createHierarchyForNewVersion(savedVersion, releaseType);

        // 디렉토리 구조 생성
        fileSystemService.createDirectoryStructure(savedVersion, customer);

        // release_metadata.json 업데이트
        metadataManager.addVersionEntry(savedVersion);

        log.info("Release version created successfully with id: {}",
                savedVersion.getReleaseVersionId());
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
                response.releaseType(),
                response.customerCode(),
                response.version(),
                response.majorMinor(),
                response.createdBy(),
                response.comment(),
                fileCategories,  // fileCategories
                response.createdAt(),
                response.patchFileCount()
        );
    }
}
