package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releasefile.util.SubCategoryValidator;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto.FileTreeNode;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHierarchy;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import com.ts.rm.domain.releaseversion.mapper.ReleaseVersionDtoMapper;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionHierarchyRepository;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.domain.releaseversion.util.ReleaseMetadataManager;
import com.ts.rm.domain.releaseversion.util.VersionParser;
import com.ts.rm.domain.releaseversion.util.VersionParser.VersionInfo;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.domain.common.service.FileStorageService;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseVersion Service
 *
 * <p>릴리즈 버전 관리 비즈니스 로직
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
    private final FileStorageService fileStorageService;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String baseReleasePath;

    @Value("${spring.servlet.multipart.max-file-size:1GB}")
    private String maxFileSizeConfig;

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
     * 표준 릴리즈 버전 및 파일 일괄 생성
     *
     * @param request      버전 생성 요청
     * @param mariadbFiles MariaDB SQL 파일들 (선택)
     * @param cratedbFiles CrateDB SQL 파일들 (선택)
     * @return 생성된 버전 상세 정보
     */
    @Transactional
    public ReleaseVersionDto.DetailResponse createStandardVersionWithFiles(
            ReleaseVersionDto.CreateRequest request,
            List<MultipartFile> mariadbFiles,
            List<MultipartFile> cratedbFiles) {

        log.info("Creating standard release version with files: {}", request.version());

        // 0. 파일 검증 먼저 수행 (DB 작업 전, 빠른 실패)
        if (mariadbFiles != null) {
            mariadbFiles.forEach(this::validateFile);
        }
        if (cratedbFiles != null) {
            cratedbFiles.forEach(this::validateFile);
        }

        // 버전 디렉토리 경로 (롤백용)
        String versionDir = null;

        try {
            // 1. 버전 생성
            ReleaseVersionDto.DetailResponse versionResponse = createStandardVersion(request);
            Long versionId = versionResponse.releaseVersionId();

            // 버전 디렉토리 경로 저장 (롤백 시 사용)
            String[] parts = request.version().split("\\.");
            String majorMinor = parts[0] + "." + parts[1] + ".x";
            versionDir = String.format("%s/version/standard/%s/%s",
                    baseReleasePath, majorMinor, request.version());

            // 2. MariaDB 파일 업로드
            if (mariadbFiles != null && !mariadbFiles.isEmpty()) {
                log.info("Uploading {} MariaDB files for version {}", mariadbFiles.size(),
                        request.version());
                uploadFiles(versionId, mariadbFiles, "mariadb");
            }

            // 3. CrateDB 파일 업로드
            if (cratedbFiles != null && !cratedbFiles.isEmpty()) {
                log.info("Uploading {} CrateDB files for version {}", cratedbFiles.size(),
                        request.version());
                uploadFiles(versionId, cratedbFiles, "cratedb");
            }

            log.info("Successfully created version {} with {} MariaDB files and {} CrateDB files",
                    request.version(),
                    mariadbFiles != null ? mariadbFiles.size() : 0,
                    cratedbFiles != null ? cratedbFiles.size() : 0);

            return versionResponse;

        } catch (Exception e) {
            // 예외 발생 시 파일시스템 롤백
            log.error("Error creating version with files, rolling back filesystem changes", e);
            if (versionDir != null) {
                rollbackFileSystem(versionDir, request.version());
            }
            throw e; // DB 트랜잭션 롤백을 위해 예외 재발생
        }
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
            deleteVersionDirectory(version);

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
                .releaseCategory(request.releaseCategory() != null ? request.releaseCategory() : ReleaseCategory.PATCH)
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
        createHierarchyForNewVersion(savedVersion, releaseType);

        // 디렉토리 구조 생성
        createDirectoryStructure(savedVersion, customer);

        // release_metadata.json 업데이트
        metadataManager.addVersionEntry(savedVersion);

        log.info("Release version created successfully with id: {}",
                savedVersion.getReleaseVersionId());
        return mapper.toDetailResponse(savedVersion);
    }

    /**
     * 릴리즈 디렉토리 구조 생성
     *
     * <pre>
     * versions/{type}/{majorMinor}.x/{version}/mariadb/
     * versions/{type}/{majorMinor}.x/{version}/cratedb/
     * </pre>
     */
    private void createDirectoryStructure(ReleaseVersion version, Customer customer) {
        try {
            String basePath;

            if ("STANDARD".equals(version.getReleaseType())) {
                basePath = String.format("versions/standard/%s/%s",
                        version.getMajorMinor(),
                        version.getVersion());
            } else {
                // CUSTOM인 경우 고객사 코드 사용
                String customerCode = customer != null ? customer.getCustomerCode() : "unknown";
                basePath = String.format("versions/custom/%s/%s/%s",
                        customerCode,
                        version.getMajorMinor(),
                        version.getVersion());
            }

            // 디렉토리 생성
            Path mariadbPath = Paths.get(baseReleasePath, basePath, "mariadb");
            Path cratedbPath = Paths.get(baseReleasePath, basePath, "cratedb");

            Files.createDirectories(mariadbPath);
            Files.createDirectories(cratedbPath);

            log.info("릴리즈 디렉토리 구조 생성 완료: {}", basePath);

        } catch (IOException e) {
            log.error("디렉토리 생성 실패: {}", version.getVersion(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "디렉토리 생성 실패: " + e.getMessage());
        }
    }

    /**
     * ReleaseVersion 조회 (존재하지 않으면 예외 발생)
     */
    private ReleaseVersion findVersionById(Long versionId) {
        return releaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND));
    }

    /**
     * 파일 업로드 처리 (내부용)
     *
     * @param versionId    버전 ID
     * @param files        업로드할 파일 목록
     * @param subCategory 하위 카테고리 (mariadb/cratedb)
     */
    private void uploadFiles(Long versionId, List<MultipartFile> files, String subCategory) {
        ReleaseVersion releaseVersion = findVersionById(versionId);

        // 기존 파일 중 최대 실행 순서 조회 (전체 기준)
        int maxOrder = releaseFileRepository
                .findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId)
                .stream()
                .mapToInt(ReleaseFile::getExecutionOrder)
                .max()
                .orElse(0);

        int executionOrder = maxOrder + 1;

        for (MultipartFile file : files) {
            try {
                // 파일 검증
                validateFile(file);

                byte[] content = file.getBytes();
                String checksum = calculateChecksum(content);

                // 파일 경로 생성: versions/{type}/{majorMinor}/{version}/{subCategory}/{fileName}
                String relativePath = String.format("versions/%s/%s/%s/%s/%s",
                        releaseVersion.getReleaseType().toLowerCase(),
                        releaseVersion.getMajorMinor(),
                        releaseVersion.getVersion(),
                        subCategory.toLowerCase(),
                        file.getOriginalFilename());

                // 실제 파일 저장
                fileStorageService.saveFile(file, relativePath);

                // DB에 메타데이터 저장
                ReleaseFile releaseFile = ReleaseFile.builder()
                        .releaseVersion(releaseVersion)
                        .fileCategory(com.ts.rm.domain.releasefile.enums.FileCategory.DATABASE)
                        .subCategory(subCategory)
                        .fileName(file.getOriginalFilename())
                        .filePath(relativePath)
                        .fileSize(file.getSize())
                        .checksum(checksum)
                        .executionOrder(executionOrder++)
                        .description("일괄 생성으로 업로드된 파일")
                        .build();

                releaseFileRepository.save(releaseFile);

                log.info("Release file uploaded: {} -> {}", file.getOriginalFilename(), relativePath);

            } catch (IOException e) {
                log.error("Failed to upload release file: {}", file.getOriginalFilename(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "파일 업로드 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "빈 파일입니다");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".sql")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "SQL 파일만 업로드 가능합니다: " + fileName);
        }

        // 10MB 제한
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "파일 크기는 10MB를 초과할 수 없습니다");
        }
    }

    /**
     * 체크섬 계산 (MD5)
     */
    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("MD5 algorithm not found", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 파일시스템 롤백 (버전 디렉토리 및 release_metadata.json 복원)
     *
     * @param versionDir 생성된 버전 디렉토리 경로
     * @param version    버전 번호
     */
    private void rollbackFileSystem(String versionDir, String version) {
        try {
            // 1. 버전 디렉토리 삭제
            Path versionPath = Paths.get(versionDir);
            if (Files.exists(versionPath)) {
                log.warn("Rolling back: Deleting version directory {}", versionDir);
                deleteDirectory(versionPath);
            }

            // 2. 빈 major.minor 디렉토리 정리
            Path parentPath = versionPath.getParent();
            if (parentPath != null && Files.exists(parentPath) && isDirectoryEmpty(parentPath)) {
                log.warn("Rolling back: Deleting empty major.minor directory {}", parentPath);
                Files.delete(parentPath);
            }

            // 3. release_metadata.json에서 해당 버전 엔트리 제거
            metadataManager.removeVersionEntry("STANDARD", version);
            log.warn("Rolling back: Removed version {} from release_metadata.json", version);

        } catch (Exception e) {
            log.error("Failed to rollback filesystem for version {}", version, e);
            // 롤백 실패는 로그만 남기고 예외를 던지지 않음 (원본 예외가 중요)
        }
    }

    /**
     * 디렉토리 재귀 삭제
     */
    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                log.info("디렉토리 삭제 완료: {}", directory);
            }
        } catch (IOException e) {
            log.error("디렉토리 삭제 실패: {}", directory, e);
        }
    }

    /**
     * 디렉토리가 비어있는지 확인
     */
    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.findAny().isEmpty();
        }
    }

    /**
     * 표준 릴리즈 버전 트리 조회
     *
     * @return 릴리즈 버전 트리
     */
    public ReleaseVersionDto.TreeResponse getStandardReleaseTree() {
        log.info("Getting standard release tree");
        return buildReleaseTree("STANDARD", null);
    }

    /**
     * 커스텀 릴리즈 버전 트리 조회
     *
     * @param customerCode 고객사 코드
     * @return 릴리즈 버전 트리
     */
    public ReleaseVersionDto.TreeResponse getCustomReleaseTree(String customerCode) {
        log.info("Getting custom release tree for customer: {}", customerCode);
        return buildReleaseTree("CUSTOM", customerCode);
    }

    /**
     * 릴리즈 버전 트리 빌드 (DB 기반)
     *
     * @param releaseType  릴리즈 타입 (STANDARD, CUSTOM)
     * @param customerCode 고객사 코드 (CUSTOM인 경우 필수)
     * @return 릴리즈 버전 트리
     */
    private ReleaseVersionDto.TreeResponse buildReleaseTree(String releaseType, String customerCode) {
        try {
            // 클로저 테이블을 통한 버전 조회
            List<ReleaseVersion> versions;
            if ("CUSTOM".equals(releaseType) && customerCode != null) {
                versions = hierarchyRepository.findAllByReleaseTypeAndCustomerWithHierarchy(
                        releaseType, customerCode);
            } else {
                versions = hierarchyRepository.findAllByReleaseTypeWithHierarchy(releaseType);
            }

            if (versions.isEmpty()) {
                log.warn("No versions found for releaseType: {}, customerCode: {}", releaseType,
                        customerCode);
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
     * 릴리즈 경로 결정
     */
    private Path determineReleasePath(String releaseType, String customerCode) {
        if ("STANDARD".equals(releaseType)) {
            return Paths.get(baseReleasePath, "versions/standard");
        } else {
            if (customerCode == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "커스텀 릴리즈는 고객사 코드가 필요합니다");
            }
            return Paths.get(baseReleasePath, "versions/custom", customerCode);
        }
    }

    /**
     * 메이저.마이너 그룹 수집
     */
    private List<ReleaseVersionDto.MajorMinorNode> collectMajorMinorGroups(
            Path releasePath, String releaseType, String customerCode) throws IOException {

        List<ReleaseVersionDto.MajorMinorNode> majorMinorGroups = new ArrayList<>();

        // 메이저.마이너 디렉토리 탐색 (1.1.x, 1.2.x 등)
        try (var majorMinorDirs = Files.list(releasePath)
                .filter(Files::isDirectory)
                .filter(p -> !p.getFileName().toString().equals("release_metadata.json"))
                .sorted((a, b) -> compareVersionPaths(b, a))) { // 내림차순 정렬

            majorMinorDirs.forEach(majorMinorDir -> {
                try {
                    String majorMinor = majorMinorDir.getFileName().toString();

                    // 버전 목록 수집
                    List<ReleaseVersionDto.VersionNode> versions = collectVersions(majorMinorDir, releaseType,
                            customerCode);

                    if (!versions.isEmpty()) {
                        majorMinorGroups.add(new ReleaseVersionDto.MajorMinorNode(majorMinor, versions));
                    }
                } catch (IOException e) {
                    log.error("Failed to collect versions from: {}", majorMinorDir, e);
                }
            });
        }

        return majorMinorGroups;
    }

    /**
     * 버전 목록 수집
     */
    private List<ReleaseVersionDto.VersionNode> collectVersions(
            Path majorMinorDir, String releaseType, String customerCode) throws IOException {

        List<ReleaseVersionDto.VersionNode> versions = new ArrayList<>();

        // 버전 디렉토리 탐색 (1.1.0, 1.1.1 등)
        try (var versionDirs = Files.list(majorMinorDir)
                .filter(Files::isDirectory)
                .sorted((a, b) -> compareVersionPaths(b, a))) { // 내림차순 정렬

            versionDirs.forEach(versionDir -> {
                try {
                    String version = versionDir.getFileName().toString();

                    // release_metadata.json에서 메타데이터 추출
                    var versionMetadata = metadataManager.getVersionMetadata(releaseType, customerCode, version);

                    if (versionMetadata == null) {
                        log.warn("Version metadata not found for: {}", version);
                        return;
                    }

                    // 레거시 파일 시스템 기반 메서드에서는 versionId를 알 수 없으므로 null
                    ReleaseVersionDto.VersionNode versionNode = new ReleaseVersionDto.VersionNode(
                            null,  // versionId - 파일 시스템 기반에서는 알 수 없음
                            version,
                            versionMetadata.createdAt(),
                            versionMetadata.createdBy(),
                            versionMetadata.comment(),
                            List.of()  // fileCategories - 파일 시스템 기반에서는 빈 리스트
                    );

                    versions.add(versionNode);
                } catch (Exception e) {
                    log.error("Failed to collect version: {}", versionDir, e);
                }
            });
        }

        return versions;
    }

    /**
     * 버전 경로 비교 (버전 정렬용) - 파일 시스템 기반 (레거시)
     */
    private int compareVersionPaths(Path a, Path b) {
        try {
            String versionA = a.getFileName().toString();
            String versionB = b.getFileName().toString();

            // x.x.x 형식인 경우 버전 비교
            if (versionA.matches("\\d+\\.\\d+\\.\\d+") && versionB.matches("\\d+\\.\\d+\\.\\d+")) {
                String[] partsA = versionA.split("\\.");
                String[] partsB = versionB.split("\\.");

                for (int i = 0; i < 3; i++) {
                    int numA = Integer.parseInt(partsA[i]);
                    int numB = Integer.parseInt(partsB[i]);
                    if (numA != numB) {
                        return Integer.compare(numA, numB);
                    }
                }
            }

            // 그 외의 경우 문자열 비교
            return versionA.compareTo(versionB);
        } catch (Exception e) {
            return a.toString().compareTo(b.toString());
        }
    }

    /**
     * 새 버전에 대한 계층 구조 데이터 생성 (클로저 테이블)
     *
     * @param newVersion  새로 생성된 버전
     * @param releaseType 릴리즈 타입
     */
    private void createHierarchyForNewVersion(ReleaseVersion newVersion, String releaseType) {
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

    // ============================================================
    // DB 기반 트리 구조 빌드 메서드 (클로저 테이블 사용)
    // ============================================================

    /**
     * DB에서 조회한 버전들을 Major.Minor로 그룹핑 (DB 기반)
     *
     * @param versions 릴리즈 버전 목록
     * @return Major.Minor 그룹 목록
     */
    private List<ReleaseVersionDto.MajorMinorNode> buildMajorMinorGroupsFromDb(
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
    private ReleaseVersionDto.VersionNode buildVersionNodeFromDb(ReleaseVersion version) {
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

        return new ReleaseVersionDto.VersionNode(
                version.getReleaseVersionId(),
                version.getVersion(),
                createdAt,
                version.getCreatedBy(),
                version.getComment(),
                fileCategories
        );
    }

    /**
     * ZIP 파일로 표준 릴리즈 버전 생성
     *
     * @param version    버전 (예: 1.1.3)
     * @param comment    패치 노트 내용
     * @param zipFile    패치 파일이 포함된 ZIP 파일
     * @param createdBy  생성자 이메일 (JWT에서 추출)
     * @return 생성된 버전 응답
     */
    @Transactional
    public ReleaseVersionDto.CreateVersionResponse createStandardVersionWithZip(
            String version, ReleaseCategory releaseCategory, String comment, MultipartFile zipFile, String createdBy) {

        log.info("ZIP 파일로 표준 릴리즈 버전 생성 시작 - version: {}, releaseCategory: {}, createdBy: {}",
                version, releaseCategory, createdBy);

        // 1. 버전 파싱 및 검증
        VersionInfo versionInfo = VersionParser.parse(version);
        validateNewVersion("STANDARD", null, versionInfo);

        // 2. ZIP 파일 검증
        validateZipFile(zipFile);

        Path tempDir = null;
        Path versionPath = null;

        try {
            // 3. 임시 디렉토리에 ZIP 압축 해제
            tempDir = extractZipToTempDirectory(zipFile);

            // 4. ZIP 구조 검증 (mariadb/, cratedb/ 폴더 확인)
            validateZipStructure(tempDir);

            // 5. 버전 디렉토리 생성
            versionPath = createVersionDirectory(versionInfo);

            // 6. 파일 복사 및 DB 저장
            ReleaseVersion savedVersion = copyFilesAndSaveToDb(tempDir, versionPath, versionInfo, releaseCategory, createdBy, comment);

            // 7. release_metadata.json 업데이트
            metadataManager.addVersionEntry(savedVersion);

            log.info("ZIP 파일로 표준 릴리즈 버전 생성 완료 - version: {}, ID: {}", version, savedVersion.getReleaseVersionId());

            // 8. 응답 생성
            return new ReleaseVersionDto.CreateVersionResponse(
                    savedVersion.getReleaseVersionId(),
                    version,
                    versionInfo.getMajorVersion(),
                    versionInfo.getMinorVersion(),
                    versionInfo.getPatchVersion(),
                    versionInfo.getMajorMinor(),
                    createdBy,
                    comment,
                    savedVersion.getCreatedAt(),
                    getCreatedFilesList(savedVersion)
            );

        } catch (BusinessException e) {
            // 생성된 버전 디렉토리 롤백
            if (versionPath != null) {
                deleteDirectory(versionPath);
            }
            throw e;
        } catch (Exception e) {
            // 생성된 버전 디렉토리 롤백
            if (versionPath != null) {
                deleteDirectory(versionPath);
            }
            log.error("ZIP 파일로 버전 생성 실패: {}", version, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "버전 생성 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 임시 디렉토리 정리
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }
    }

    /**
     * ZIP 파일 검증
     */
    private void validateZipFile(MultipartFile zipFile) {
        if (zipFile == null || zipFile.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "ZIP 파일이 비어있습니다");
        }

        // 파일 확장자 확인
        String originalFilename = zipFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "ZIP 파일만 업로드 가능합니다");
        }

        // 파일 크기 제한 (1GB)
        long maxSize = 1L * 1024 * 1024 * 1024;
        if (zipFile.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("파일 크기가 너무 큽니다 (최대 1GB): %d bytes", zipFile.getSize()));
        }
    }

    /**
     * ZIP 파일을 임시 디렉토리에 압축 해제
     */
    private Path extractZipToTempDirectory(MultipartFile zipFile) throws IOException {
        Path tempDir = Files.createTempDirectory("release_upload_");

        try (java.util.zip.ZipInputStream zis =
                     new java.util.zip.ZipInputStream(zipFile.getInputStream())) {

            java.util.zip.ZipEntry entry;
            long totalSize = 0;
            long maxTotalSize = parseFileSize(maxFileSizeConfig); // application.yml 설정값 사용

            while ((entry = zis.getNextEntry()) != null) {
                // 경로 탐색 공격 방지
                Path targetPath = tempDir.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(tempDir)) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                            "유효하지 않은 ZIP 파일 경로입니다: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    // 파일 크기 누적 확인 (ZIP 폭탄 방지)
                    totalSize += entry.getSize();
                    if (totalSize > maxTotalSize) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                                "압축 해제 후 파일 크기가 너무 큽니다 (최대 " + maxFileSizeConfig + ")");
                    }

                    Files.createDirectories(targetPath.getParent());
                    Files.copy(zis, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            deleteDirectory(tempDir);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "ZIP 파일 압축 해제 실패: " + e.getMessage());
        }

        return tempDir;
    }

    /**
     * 파일 크기 문자열을 바이트 단위로 변환
     *
     * @param sizeStr 크기 문자열 (예: "1GB", "500MB", "1024KB")
     * @return 바이트 단위 크기
     */
    private long parseFileSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isEmpty()) {
            return 1024L * 1024 * 1024; // 기본값 1GB
        }

        sizeStr = sizeStr.trim().toUpperCase();

        long multiplier = 1;
        if (sizeStr.endsWith("GB")) {
            multiplier = 1024L * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("MB")) {
            multiplier = 1024L * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("KB")) {
            multiplier = 1024L;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        }

        try {
            return Long.parseLong(sizeStr.trim()) * multiplier;
        } catch (NumberFormatException e) {
            log.warn("파일 크기 파싱 실패: {}, 기본값 1GB 사용", sizeStr);
            return 1024L * 1024 * 1024;
        }
    }

    /**
     * ZIP 구조 검증 (카테고리 폴더 확인)
     */
    private void validateZipStructure(Path tempDir) throws IOException {
        // 최상위 디렉토리에서 유효한 카테고리 폴더가 최소 1개 이상 있는지 확인
        boolean hasValidCategory = Files.list(tempDir)
                .filter(Files::isDirectory)
                .map(path -> path.getFileName().toString().toUpperCase())
                .anyMatch(dirName -> {
                    try {
                        FileCategory.fromCode(dirName);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                });

        if (!hasValidCategory) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "ZIP 파일에 유효한 카테고리 폴더가 없습니다. " +
                    "최소 1개 이상의 폴더 필요: database/, web/, engine/, install/");
        }

        // 파일 확장자 검증 제거: 모든 확장자 허용
    }

    /**
     * 버전 디렉토리 생성
     *
     * @return 생성된 버전 경로
     */
    private Path createVersionDirectory(VersionInfo versionInfo) throws IOException {
        // 경로: resources/release/versions/standard/{major}.{minor}.x/{version}/
        String majorMinor = versionInfo.getMajorMinor();
        String version = versionInfo.getMajorVersion() + "." + versionInfo.getMinorVersion() + "." + versionInfo.getPatchVersion();

        Path versionPath = Paths.get(baseReleasePath, "versions", "standard",
                majorMinor, version);

        Files.createDirectories(versionPath);
        log.info("버전 디렉토리 생성: {}", versionPath);

        return versionPath;
    }

    /**
     * 파일 복사 및 DB 저장
     *
     * @return 저장된 ReleaseVersion 엔티티
     */
    private ReleaseVersion copyFilesAndSaveToDb(Path tempDir, Path versionPath,
                                                 VersionInfo versionInfo, ReleaseCategory releaseCategory, String createdBy, String comment) throws IOException {
        String version = versionInfo.getMajorVersion() + "." + versionInfo.getMinorVersion() + "." + versionInfo.getPatchVersion();

        // ReleaseVersion 생성 및 저장
        final ReleaseVersion savedVersion = releaseVersionRepository.save(
                ReleaseVersion.builder()
                        .releaseType("STANDARD")
                        .releaseCategory(releaseCategory)
                        .version(version)
                        .majorVersion(versionInfo.getMajorVersion())
                        .minorVersion(versionInfo.getMinorVersion())
                        .patchVersion(versionInfo.getPatchVersion())
                        .createdBy(createdBy)
                        .comment(comment)
                        .build()
        );

        log.info("ReleaseVersion 저장 완료 - ID: {}, version: {}", savedVersion.getReleaseVersionId(), version);

        // 클로저 테이블에 계층 구조 데이터 추가
        createHierarchyForNewVersion(savedVersion, "STANDARD");

        // 모든 카테고리 폴더 순회 및 파일 복사
        Files.list(tempDir)
                .filter(Files::isDirectory)
                .forEach(categoryDir -> {
                    String categoryName = categoryDir.getFileName().toString().toUpperCase();

                    try {
                        // FileCategory 검증 및 변환
                        FileCategory fileCategory = FileCategory.fromCode(categoryName);

                        // 타겟 카테고리 디렉토리 생성
                        Path targetCategoryDir = versionPath.resolve(categoryName.toLowerCase());
                        Files.createDirectories(targetCategoryDir);

                        log.info("카테고리 폴더 처리 시작: {} -> {}", categoryName, fileCategory.getDescription());

                        // 카테고리별 파일 복사 (재귀적)
                        processCategoryFiles(categoryDir, targetCategoryDir, savedVersion, fileCategory);

                    } catch (IllegalArgumentException e) {
                        log.warn("알 수 없는 카테고리 폴더 무시: {}", categoryName);
                    } catch (IOException e) {
                        log.error("카테고리 파일 복사 실패: {}", categoryName, e);
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                                "파일 복사 중 오류 발생: " + e.getMessage());
                    }
                });

        return savedVersion;
    }

    /**
     * 카테고리별 파일 처리 (하위 폴더 재귀 처리)
     *
     * @param categorySourceDir 카테고리 소스 디렉토리 (예: tempDir/database)
     * @param categoryTargetDir 카테고리 타겟 디렉토리 (예: versionPath/database)
     * @param releaseVersion    릴리즈 버전 엔티티
     * @param fileCategory      파일 카테고리 (DATABASE, WEB, ENGINE, INSTALL)
     */
    private void processCategoryFiles(Path categorySourceDir, Path categoryTargetDir,
                                       ReleaseVersion releaseVersion, FileCategory fileCategory) throws IOException {

        // 하위 폴더 순회 (예: database/MARIADB, database/CRATEDB, web/build 등)
        Files.list(categorySourceDir)
                .filter(Files::isDirectory)
                .forEach(subDir -> {
                    String subCategory = subDir.getFileName().toString();

                    // DATABASE와 ENGINE 카테고리: CODE 테이블에 존재하는 값만 대문자 검증
                    if (fileCategory == FileCategory.DATABASE || fileCategory == FileCategory.ENGINE) {
                        String upperSubCategory = subCategory.toUpperCase();

                        // CODE 테이블에 대문자 버전이 존재하는지 확인
                        if (SubCategoryValidator.isValid(fileCategory, upperSubCategory)) {
                            // CODE 테이블에 있는 값인 경우 → 반드시 대문자로 작성되어야 함
                            if (!subCategory.equals(upperSubCategory)) {
                                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                                        String.format("CODE 테이블에 등록된 %s 하위 카테고리는 반드시 대문자로 작성해야 합니다. " +
                                                "현재: '%s', 올바른 형식: '%s'",
                                                fileCategory.getCode(), subCategory, upperSubCategory));
                            }
                            // 대문자로 유지
                        } else {
                            // CODE 테이블에 없는 값 → 사용자가 작성한 대로 사용 (대소문자 자유)
                            log.debug("CODE 테이블에 없는 사용자 정의 하위 카테고리: {}/{}", fileCategory.getCode(), subCategory);
                        }
                    } else {
                        // WEB, INSTALL은 소문자로 변환
                        subCategory = subCategory.toLowerCase();
                    }

                    Path targetSubDir = categoryTargetDir.resolve(subCategory);

                    try {
                        Files.createDirectories(targetSubDir);
                        log.debug("하위 폴더 처리: {}/{}", fileCategory.getCode(), subCategory);

                        // 하위 폴더의 파일 복사
                        copyFilesRecursively(subDir, targetSubDir, releaseVersion, fileCategory, subCategory);

                    } catch (IOException e) {
                        log.error("하위 폴더 파일 복사 실패: {}/{}", fileCategory.getCode(), subCategory, e);
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                                "파일 복사 실패: " + e.getMessage());
                    }
                });

        // 카테고리 최상위에 직접 있는 파일도 처리 (sub_category = null)
        Files.list(categorySourceDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Path targetFile = categoryTargetDir.resolve(file.getFileName());
                        Files.copy(file, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                        // ReleaseFile DB 저장 (sub_category = null)
                        saveReleaseFile(file, targetFile, releaseVersion, fileCategory, null,
                                categorySourceDir, 1);

                    } catch (IOException e) {
                        log.error("파일 복사 실패: {}", file.getFileName(), e);
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                                "파일 복사 실패: " + e.getMessage());
                    }
                });
    }

    /**
     * 재귀적 파일 복사 및 ReleaseFile 저장
     *
     * @param sourceDir      소스 디렉토리
     * @param targetDir      타겟 디렉토리
     * @param releaseVersion 릴리즈 버전
     * @param fileCategory   파일 카테고리
     * @param subCategory    하위 카테고리 (예: mariadb, cratedb, build)
     */
    private void copyFilesRecursively(Path sourceDir, Path targetDir,
                                       ReleaseVersion releaseVersion, FileCategory fileCategory,
                                       String subCategory) throws IOException {

        // 모든 파일을 재귀적으로 탐색하여 복사 (확장자 제한 없음)
        List<Path> files = Files.walk(sourceDir)
                .filter(Files::isRegularFile)
                .sorted()
                .toList();

        int executionOrder = 1;
        for (Path file : files) {
            // 상대 경로 계산 (하위 폴더 구조 유지)
            Path relativePath = sourceDir.relativize(file);
            Path targetFile = targetDir.resolve(relativePath);

            // 타겟 디렉토리 생성
            Files.createDirectories(targetFile.getParent());

            // 파일 복사
            Files.copy(file, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // ReleaseFile DB 저장
            saveReleaseFile(file, targetFile, releaseVersion, fileCategory, subCategory,
                    sourceDir, executionOrder++);
        }
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

    /**
     * ReleaseFile 엔티티 생성 및 저장
     */
    private void saveReleaseFile(Path sourceFile, Path targetFile, ReleaseVersion releaseVersion,
                                  FileCategory fileCategory, String subCategory,
                                  Path sourceBaseDir, int executionOrder) throws IOException {

        // 파일 크기 및 체크섬 계산
        byte[] fileContent = Files.readAllBytes(sourceFile);
        long fileSize = fileContent.length;
        String checksum = calculateChecksum(fileContent);

        // 물리 경로 계산 (baseReleasePath 기준)
        Path basePath = Paths.get(baseReleasePath);
        String physicalPath = basePath.relativize(targetFile).toString().replace("\\", "/");

        // ZIP 내부 상대 경로 계산
        String zipInternalPath = "/" + fileCategory.getCode().toLowerCase() + "/";
        if (subCategory != null) {
            zipInternalPath += subCategory + "/";
        }
        zipInternalPath += sourceBaseDir.relativize(sourceFile).toString().replace("\\", "/");

        // 파일 타입 결정
        String fileType = determineFileType(sourceFile.getFileName().toString());

        // ReleaseFile 저장
        ReleaseFile releaseFile = ReleaseFile.builder()
                .releaseVersion(releaseVersion)
                .fileType(fileType)
                .fileCategory(fileCategory)
                .subCategory(subCategory)
                .fileName(sourceFile.getFileName().toString())
                .filePath(physicalPath)
                .relativePath(zipInternalPath)
                .fileSize(fileSize)
                .checksum(checksum)
                .executionOrder(executionOrder)
                .description("ZIP 파일 업로드로 생성된 " + fileCategory.getDescription() + " 파일")
                .build();

        releaseFileRepository.save(releaseFile);

        log.debug("파일 복사 및 저장 완료: {} -> {} (category: {}, size: {}, checksum: {})",
                sourceFile.getFileName(), zipInternalPath, fileCategory.getCode(), fileSize, checksum);
    }

    /**
     * 새 버전 검증 (중복 확인)
     */
    private void validateNewVersion(String releaseType, Long customerId, VersionInfo versionInfo) {
        String version = versionInfo.getMajorVersion() + "." + versionInfo.getMinorVersion() + "." + versionInfo.getPatchVersion();

        // 중복 버전 확인
        boolean exists = releaseVersionRepository.findByReleaseTypeAndVersion(releaseType, version).isPresent();
        if (exists) {
            throw new BusinessException(ErrorCode.RELEASE_VERSION_CONFLICT,
                    "이미 존재하는 버전입니다: " + version);
        }
    }

    /**
     * 생성된 파일 목록 조회
     *
     * @param releaseVersion 릴리즈 버전 엔티티
     * @return 파일 목록 (상대 경로)
     */
    private List<String> getCreatedFilesList(ReleaseVersion releaseVersion) {
        return releaseFileRepository.findAllByReleaseVersionIdOrderByExecutionOrderAsc(
                        releaseVersion.getReleaseVersionId())
                .stream()
                .map(file -> {
                    // DATABASE와 ENGINE은 대문자 유지, 나머지는 소문자 (DB에 이미 대문자로 저장되어 있음)
                    String category = file.getSubCategory() != null ? file.getSubCategory() : "unknown";
                    return category + "/" + file.getFileName();
                })
                .toList();
    }

    /**
     * 버전 디렉토리 삭제
     *
     * @param version 릴리즈 버전 엔티티
     */
    private void deleteVersionDirectory(ReleaseVersion version) {
        Path versionPath;

        if ("STANDARD".equals(version.getReleaseType())) {
            versionPath = Paths.get(baseReleasePath, "versions", "standard",
                    version.getMajorMinor(), version.getVersion());
        } else {
            String customerCode = version.getCustomer() != null
                    ? version.getCustomer().getCustomerCode()
                    : "unknown";
            versionPath = Paths.get(baseReleasePath, "versions", "custom",
                    customerCode, version.getMajorMinor(), version.getVersion());
        }

        if (Files.exists(versionPath)) {
            deleteDirectory(versionPath);
            log.info("버전 디렉토리 삭제 완료: {}", versionPath);

            // 빈 major.minor 디렉토리도 정리
            try {
                Path parentPath = versionPath.getParent();
                if (parentPath != null && Files.exists(parentPath) && isDirectoryEmpty(parentPath)) {
                    Files.delete(parentPath);
                    log.info("빈 major.minor 디렉토리 삭제: {}", parentPath);
                }
            } catch (IOException e) {
                log.warn("major.minor 디렉토리 삭제 실패: {}", versionPath.getParent(), e);
            }
        }
    }

    /**
     * 릴리즈 버전의 파일 트리 구조 조회
     *
     * @param versionId 릴리즈 버전 ID
     * @return 파일 트리 응답
     */
    public ReleaseVersionDto.FileTreeResponse getVersionFileTree(Long versionId) {
        // 버전 조회
        ReleaseVersion version = findVersionById(versionId);

        // 모든 파일 조회 (relativePath 순으로 정렬)
        List<ReleaseFile> files = releaseFileRepository.findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId);

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
    private ReleaseVersionDto.FileTreeNode buildFileTree(List<ReleaseFile> files) {
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

    /**
     * 파일 확장자로 파일 타입 결정
     *
     * @param fileName 파일명
     * @return 파일 타입 코드 (FILE_TYPE_*)
     */
    /**
     * 파일명으로부터 파일 타입 결정
     */
    private String determineFileType(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase();

        if (lowerCaseFileName.endsWith(".sql")) {
            return "SQL";
        } else if (lowerCaseFileName.endsWith(".war")) {
            return "WAR";
        } else if (lowerCaseFileName.endsWith(".jar")) {
            return "JAR";
        } else if (lowerCaseFileName.endsWith(".md")) {
            return "MD";
        } else if (lowerCaseFileName.endsWith(".pdf")) {
            return "PDF";
        } else if (lowerCaseFileName.endsWith(".exe")) {
            return "EXE";
        } else if (lowerCaseFileName.endsWith(".sh")) {
            return "SH";
        } else if (lowerCaseFileName.endsWith(".bat")) {
            return "BAT";
        } else if (lowerCaseFileName.endsWith(".txt")) {
            return "TXT";
        } else if (lowerCaseFileName.endsWith(".yml") || lowerCaseFileName.endsWith(".yaml")) {
            return "YAML";
        } else if (lowerCaseFileName.endsWith(".properties")) {
            return "PROPERTIES";
        } else if (lowerCaseFileName.endsWith(".xml")) {
            return "XML";
        } else {
            return "UNDEFINED";
        }
    }

}
