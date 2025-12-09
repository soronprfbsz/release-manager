package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.common.service.FileStorageService;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.project.repository.ProjectRepository;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releasefile.util.SubCategoryValidator;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import com.ts.rm.domain.releaseversion.mapper.ReleaseVersionDtoMapper;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.domain.releaseversion.util.ReleaseMetadataManager;
import com.ts.rm.domain.releaseversion.util.VersionParser;
import com.ts.rm.domain.releaseversion.util.VersionParser.VersionInfo;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseVersion Upload Service
 *
 * <p>릴리즈 버전의 파일 업로드 및 ZIP 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseVersionUploadService {

    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseFileRepository releaseFileRepository;
    private final ProjectRepository projectRepository;
    private final ReleaseMetadataManager metadataManager;
    private final FileStorageService fileStorageService;
    private final ReleaseVersionFileSystemService fileSystemService;
    private final ReleaseVersionTreeService treeService;
    private final ReleaseVersionDtoMapper mapper;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String baseReleasePath;

    @Value("${spring.servlet.multipart.max-file-size:1GB}")
    private String maxFileSizeConfig;

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
            ReleaseVersion savedVersion,
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
            Long versionId = savedVersion.getReleaseVersionId();

            // 버전 디렉토리 경로 저장 (롤백 시 사용)
            String projectId = savedVersion.getProject() != null ? savedVersion.getProject().getProjectId() : "infraeye2";
            String[] parts = request.version().split("\\.");
            String majorMinor = parts[0] + "." + parts[1] + ".x";
            versionDir = String.format("%s/versions/%s/standard/%s/%s",
                    baseReleasePath, projectId, majorMinor, request.version());

            // 2. MariaDB 파일 업로드
            if (mariadbFiles != null && !mariadbFiles.isEmpty()) {
                log.info("Uploading {} MariaDB files for version {}", mariadbFiles.size(),
                        request.version());
                uploadFiles(savedVersion, mariadbFiles, "mariadb");
            }

            // 3. CrateDB 파일 업로드
            if (cratedbFiles != null && !cratedbFiles.isEmpty()) {
                log.info("Uploading {} CrateDB files for version {}", cratedbFiles.size(),
                        request.version());
                uploadFiles(savedVersion, cratedbFiles, "cratedb");
            }

            log.info("Successfully created version {} with {} MariaDB files and {} CrateDB files",
                    request.version(),
                    mariadbFiles != null ? mariadbFiles.size() : 0,
                    cratedbFiles != null ? cratedbFiles.size() : 0);

            // DetailResponse 반환을 위한 조회
            return mapper.toDetailResponse(savedVersion);

        } catch (Exception e) {
            // 예외 발생 시 파일시스템 롤백
            log.error("Error creating version with files, rolling back filesystem changes", e);
            if (versionDir != null) {
                String projectId = savedVersion.getProject() != null
                        ? savedVersion.getProject().getProjectId()
                        : "infraeye2";
                fileSystemService.rollbackFileSystem(versionDir, projectId, request.version());
            }
            throw e; // DB 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    /**
     * ZIP 파일로 표준 릴리즈 버전 생성
     *
     * @param projectId       프로젝트 ID
     * @param version         버전 (예: 1.1.3)
     * @param releaseCategory 릴리즈 카테고리
     * @param comment         패치 노트 내용
     * @param zipFile         패치 파일이 포함된 ZIP 파일
     * @param createdBy       생성자 이메일 (JWT에서 추출)
     * @return 생성된 버전 응답
     */
    @Transactional
    public ReleaseVersionDto.CreateVersionResponse createStandardVersionWithZip(
            String projectId, String version, ReleaseCategory releaseCategory, String comment, MultipartFile zipFile, String createdBy) {

        log.info("ZIP 파일로 표준 릴리즈 버전 생성 시작 - projectId: {}, version: {}, releaseCategory: {}, createdBy: {}",
                projectId, version, releaseCategory, createdBy);

        // 0. 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND,
                        "프로젝트를 찾을 수 없습니다: " + projectId));

        // 1. 버전 파싱 및 검증
        VersionInfo versionInfo = VersionParser.parse(version);
        validateNewVersion(projectId, "STANDARD", versionInfo);

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
            versionPath = fileSystemService.createVersionDirectory(versionInfo, projectId);

            // 6. 파일 복사 및 DB 저장
            ReleaseVersion savedVersion = copyFilesAndSaveToDb(project, tempDir, versionPath, versionInfo, releaseCategory, createdBy, comment);

            // 7. release_metadata.json 업데이트
            metadataManager.addVersionEntry(savedVersion);

            log.info("ZIP 파일로 표준 릴리즈 버전 생성 완료 - projectId: {}, version: {}, ID: {}", projectId, version, savedVersion.getReleaseVersionId());

            // 8. 응답 생성
            return new ReleaseVersionDto.CreateVersionResponse(
                    savedVersion.getReleaseVersionId(),
                    projectId,
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
                fileSystemService.deleteDirectory(versionPath);
            }
            throw e;
        } catch (Exception e) {
            // 생성된 버전 디렉토리 롤백
            if (versionPath != null) {
                fileSystemService.deleteDirectory(versionPath);
            }
            log.error("ZIP 파일로 버전 생성 실패: {}", version, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "버전 생성 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // 임시 디렉토리 정리
            if (tempDir != null) {
                fileSystemService.deleteDirectory(tempDir);
            }
        }
    }

    /**
     * 파일 업로드 처리 (내부용)
     *
     * @param releaseVersion 릴리즈 버전
     * @param files          업로드할 파일 목록
     * @param subCategory    하위 카테고리 (mariadb/cratedb)
     */
    private void uploadFiles(ReleaseVersion releaseVersion, List<MultipartFile> files, String subCategory) {
        Long versionId = releaseVersion.getReleaseVersionId();

        // 기존 파일 중 최대 실행 순서 조회 (전체 기준)
        int maxOrder = releaseFileRepository
                .findAllByReleaseVersion_ReleaseVersionIdOrderByExecutionOrderAsc(versionId)
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

                // 파일 경로 생성: versions/{projectId}/{type}/{majorMinor}/{version}/{subCategory}/{fileName}
                String projectId = releaseVersion.getProject() != null ? releaseVersion.getProject().getProjectId() : "infraeye2";
                String relativePath = String.format("versions/%s/%s/%s/%s/%s/%s",
                        projectId,
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
    public void validateFile(MultipartFile file) {
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
     * ZIP 파일 검증
     */
    public void validateZipFile(MultipartFile zipFile) {
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
    public Path extractZipToTempDirectory(MultipartFile zipFile) throws IOException {
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
            fileSystemService.deleteDirectory(tempDir);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "ZIP 파일 압축 해제 실패: " + e.getMessage());
        }

        return tempDir;
    }

    /**
     * ZIP 구조 검증 (카테고리 폴더 확인)
     */
    public void validateZipStructure(Path tempDir) throws IOException {
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
     * 파일 복사 및 DB 저장
     *
     * @return 저장된 ReleaseVersion 엔티티
     */
    public ReleaseVersion copyFilesAndSaveToDb(Project project, Path tempDir, Path versionPath,
                                                VersionInfo versionInfo, ReleaseCategory releaseCategory, String createdBy, String comment) throws IOException {
        String version = versionInfo.getMajorVersion() + "." + versionInfo.getMinorVersion() + "." + versionInfo.getPatchVersion();

        // ReleaseVersion 생성 및 저장
        final ReleaseVersion savedVersion = releaseVersionRepository.save(
                ReleaseVersion.builder()
                        .project(project)
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
        treeService.createHierarchyForNewVersion(savedVersion, "STANDARD");

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
    public void processCategoryFiles(Path categorySourceDir, Path categoryTargetDir,
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
    public void copyFilesRecursively(Path sourceDir, Path targetDir,
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
     * 체크섬 계산 (MD5)
     */
    public String calculateChecksum(byte[] content) {
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
     * 파일 크기 문자열을 바이트 단위로 변환
     *
     * @param sizeStr 크기 문자열 (예: "1GB", "500MB", "1024KB")
     * @return 바이트 단위 크기
     */
    public long parseFileSize(String sizeStr) {
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
     * 새 버전 검증 (중복 확인)
     */
    public void validateNewVersion(String projectId, String releaseType, VersionInfo versionInfo) {
        String version = versionInfo.getMajorVersion() + "." + versionInfo.getMinorVersion() + "." + versionInfo.getPatchVersion();

        // 중복 버전 확인 (프로젝트 내에서)
        boolean exists = releaseVersionRepository.existsByProject_ProjectIdAndVersion(projectId, version);
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
    public List<String> getCreatedFilesList(ReleaseVersion releaseVersion) {
        return releaseFileRepository.findAllByReleaseVersion_ReleaseVersionIdOrderByExecutionOrderAsc(
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
     * 파일명으로부터 파일 타입 결정
     */
    public String determineFileType(String fileName) {
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
