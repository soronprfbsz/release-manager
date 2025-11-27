package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersionHierarchy;
import com.ts.rm.domain.releaseversion.mapper.ReleaseVersionDtoMapper;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionHierarchyRepository;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.domain.releaseversion.util.PatchNoteManager;
import com.ts.rm.domain.releaseversion.util.VersionParser;
import com.ts.rm.domain.releaseversion.util.VersionParser.VersionInfo;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import com.ts.rm.global.file.FileStorageService;
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
    private final PatchNoteManager patchNoteManager;
    private final FileStorageService fileStorageService;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String baseReleasePath;

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
                uploadFiles(versionId, mariadbFiles, "MARIADB");
            }

            // 3. CrateDB 파일 업로드
            if (cratedbFiles != null && !cratedbFiles.isEmpty()) {
                log.info("Uploading {} CrateDB files for version {}", cratedbFiles.size(),
                        request.version());
                uploadFiles(versionId, cratedbFiles, "CRATEDB");
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
        return mapper.toSimpleResponseList(versions);
    }

    /**
     * 고객사별 버전 목록 조회
     *
     * @param customerId 고객사 ID
     * @return 버전 목록
     */
    public List<ReleaseVersionDto.SimpleResponse> getVersionsByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        List<ReleaseVersion> versions = releaseVersionRepository
                .findAllByCustomerIdOrderByCreatedAtDesc(customerId);
        return mapper.toSimpleResponseList(versions);
    }

    /**
     * Major.Minor 버전 목록 조회
     *
     * @param typeName   릴리즈 타입
     * @param majorMinor 메이저.마이너 (예: 1.1.x)
     * @return 버전 목록
     */
    public List<ReleaseVersionDto.SimpleResponse> getVersionsByMajorMinor(String typeName,
            String majorMinor) {
        // majorMinor 파싱 (예: "1.1.x" -> major=1, minor=1)
        String[] parts = majorMinor.replace(".x", "").split("\\.");
        Integer majorVersion = Integer.parseInt(parts[0]);
        Integer minorVersion = Integer.parseInt(parts[1]);

        List<ReleaseVersion> versions = releaseVersionRepository
                .findAllByMajorMinorOrderByPatchVersionDesc(majorVersion, minorVersion);
        return mapper.toSimpleResponseList(versions);
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
        return mapper.toSimpleResponseList(versions);
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
        if (request.isInstall() != null) {
            releaseVersion.setIsInstall(request.isInstall());
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
        log.info("Deleting release version with versionId: {}", versionId);

        // 버전 존재 검증
        ReleaseVersion version = findVersionById(versionId);
        releaseVersionRepository.delete(version);

        log.info("Release version deleted successfully with versionId: {}", versionId);
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
                .customer(customer)
                .version(request.version())
                .majorVersion(versionInfo.getMajorVersion())
                .minorVersion(versionInfo.getMinorVersion())
                .patchVersion(versionInfo.getPatchVersion())
                .createdBy(request.createdBy())
                .comment(request.comment())
                .customVersion(request.customVersion())
                .isInstall(request.isInstall() != null ? request.isInstall() : false)
                .build();

        ReleaseVersion savedVersion = releaseVersionRepository.save(version);

        // 클로저 테이블에 계층 구조 데이터 추가
        createHierarchyForNewVersion(savedVersion, releaseType);

        // 디렉토리 구조 생성
        createDirectoryStructure(savedVersion, customer);

        // patch_note.md 업데이트
        patchNoteManager.addVersionEntry(savedVersion);

        log.info("Release version created successfully with id: {}",
                savedVersion.getReleaseVersionId());
        return mapper.toDetailResponse(savedVersion);
    }

    /**
     * 릴리즈 디렉토리 구조 생성
     *
     * <pre>
     * versions/{type}/{majorMinor}.x/{version}/patch/mariadb/
     * versions/{type}/{majorMinor}.x/{version}/patch/cratedb/
     * </pre>
     */
    private void createDirectoryStructure(ReleaseVersion version, Customer customer) {
        try {
            String basePath;

            if ("STANDARD".equals(version.getReleaseType())) {
                basePath = String.format("versions/standard/%s/%s/patch",
                        version.getMajorMinor(),
                        version.getVersion());
            } else {
                // CUSTOM인 경우 고객사 코드 사용
                String customerCode = customer != null ? customer.getCustomerCode() : "unknown";
                basePath = String.format("versions/custom/%s/%s/%s/patch",
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
     * @param databaseType 데이터베이스 타입 (MARIADB/CRATEDB)
     */
    private void uploadFiles(Long versionId, List<MultipartFile> files, String databaseType) {
        ReleaseVersion releaseVersion = findVersionById(versionId);

        // 기존 파일 중 최대 실행 순서 조회 (동일 DB 타입 기준)
        int maxOrder = releaseFileRepository
                .findByVersionAndDatabaseType(versionId, databaseType)
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

                // 파일 경로 생성: versions/{type}/{majorMinor}/{version}/patch/{dbType}/{fileName}
                String relativePath = String.format("versions/%s/%s/%s/patch/%s/%s",
                        releaseVersion.getReleaseType().toLowerCase(),
                        releaseVersion.getMajorMinor(),
                        releaseVersion.getVersion(),
                        databaseType.toLowerCase(),
                        file.getOriginalFilename());

                // 실제 파일 저장
                fileStorageService.saveFile(file, relativePath);

                // DB에 메타데이터 저장
                ReleaseFile releaseFile = ReleaseFile.builder()
                        .releaseVersion(releaseVersion)
                        .databaseType(databaseType)
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
     * 파일시스템 롤백 (버전 디렉토리 및 patch_note.md 복원)
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

            // 3. patch_note.md에서 해당 버전 엔트리 제거
            patchNoteManager.removeVersionEntry("STANDARD", version);
            log.warn("Rolling back: Removed version {} from patch_note.md", version);

        } catch (Exception e) {
            log.error("Failed to rollback filesystem for version {}", version, e);
            // 롤백 실패는 로그만 남기고 예외를 던지지 않음 (원본 예외가 중요)
        }
    }

    /**
     * 디렉토리 재귀 삭제
     */
    private void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
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
                .filter(p -> !p.getFileName().toString().equals("patch_note.md"))
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

                    // patch_note.md에서 메타데이터 추출
                    var versionMetadata = patchNoteManager.getVersionMetadata(releaseType, customerCode, version);

                    if (versionMetadata == null) {
                        log.warn("Version metadata not found for: {}", version);
                        return;
                    }

                    // 데이터베이스 파일 목록 수집
                    List<ReleaseVersionDto.DatabaseNode> databases = collectDatabases(versionDir);

                    // 레거시 파일 시스템 기반 메서드에서는 versionId를 알 수 없으므로 null
                    ReleaseVersionDto.VersionNode versionNode = new ReleaseVersionDto.VersionNode(
                            null,  // versionId - 파일 시스템 기반에서는 알 수 없음
                            version,
                            versionMetadata.createdAt(),
                            versionMetadata.createdBy(),
                            versionMetadata.comment(),
                            versionMetadata.isInstall(),
                            databases
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
     * 데이터베이스 파일 목록 수집
     */
    private List<ReleaseVersionDto.DatabaseNode> collectDatabases(Path versionDir) throws IOException {
        List<ReleaseVersionDto.DatabaseNode> databases = new ArrayList<>();

        Path patchDir = versionDir.resolve("patch");
        if (!Files.exists(patchDir)) {
            return databases;
        }

        // mariadb, cratedb 디렉토리 탐색
        try (var dbDirs = Files.list(patchDir)
                .filter(Files::isDirectory)) {

            dbDirs.forEach(dbDir -> {
                try {
                    String dbType = dbDir.getFileName().toString().toUpperCase();

                    // SQL 파일 목록 수집
                    List<String> files = new ArrayList<>();
                    try (var sqlFiles = Files.list(dbDir)
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".sql"))
                            .sorted()) {

                        sqlFiles.forEach(sqlFile -> {
                            files.add(sqlFile.getFileName().toString());
                        });
                    }

                    if (!files.isEmpty()) {
                        databases.add(new ReleaseVersionDto.DatabaseNode(dbType, files));
                    }
                } catch (IOException e) {
                    log.error("Failed to collect database files from: {}", dbDir, e);
                }
            });
        }

        return databases;
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
        // 파일 목록 조회
        List<ReleaseFile> files = releaseFileRepository.findAllByReleaseVersionIdOrderByExecutionOrderAsc(
                version.getReleaseVersionId());

        // 데이터베이스 타입별로 그룹핑
        java.util.Map<String, List<String>> filesByDatabase = new java.util.LinkedHashMap<>();

        for (ReleaseFile file : files) {
            filesByDatabase.computeIfAbsent(file.getDatabaseType(), k -> new ArrayList<>())
                    .add(file.getFileName());
        }

        // DatabaseNode 생성
        List<ReleaseVersionDto.DatabaseNode> databases = filesByDatabase.entrySet().stream()
                .map(entry -> new ReleaseVersionDto.DatabaseNode(entry.getKey(), entry.getValue()))
                .toList();

        // createdAt을 "YYYY-MM-DD" 형식으로 포맷
        String createdAt = version.getCreatedAt() != null
                ? version.getCreatedAt().toLocalDate().toString()
                : null;

        return new ReleaseVersionDto.VersionNode(
                version.getReleaseVersionId(),  // versionId 추가!
                version.getVersion(),
                createdAt,
                version.getCreatedBy(),
                version.getComment(),
                version.getIsInstall(),
                databases
        );
    }
}
