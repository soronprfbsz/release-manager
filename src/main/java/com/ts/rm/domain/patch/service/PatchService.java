package com.ts.rm.domain.patch.service;

import com.ts.rm.domain.patch.dto.PatchDto;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.repository.PatchRepository;
import com.ts.rm.domain.patch.util.ScriptGenerator;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.file.ZipUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Patch Service
 *
 * <p>패치 생성 및 관리를 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatchService {

    private final PatchRepository patchRepository;
    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseFileRepository releaseFileRepository;
    private final ScriptGenerator scriptGenerator;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String releaseBasePath;

    /**
     * 패치 생성 (버전 문자열 기반)
     *
     * @param releaseType  릴리즈 타입 (STANDARD/CUSTOM)
     * @param customerId   고객사 ID (CUSTOM인 경우)
     * @param fromVersion  From 버전 (예: 1.0.0)
     * @param toVersion    To 버전 (예: 1.1.1)
     * @param createdBy    생성자
     * @param description  설명 (선택)
     * @param patchedBy    패치 담당자 (선택)
     * @param patchName    패치 이름 (선택, 미입력 시 자동 생성)
     * @return 생성된 패치
     */
    @Transactional
    public Patch generatePatchByVersion(String releaseType, Long customerId,
            String fromVersion, String toVersion, String createdBy, String description,
            String patchedBy, String patchName) {

        // 버전 조회
        ReleaseVersion from = releaseVersionRepository.findByReleaseTypeAndVersion(
                        releaseType.toUpperCase(), fromVersion)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                        "From 버전을 찾을 수 없습니다: " + fromVersion));

        ReleaseVersion to = releaseVersionRepository.findByReleaseTypeAndVersion(
                        releaseType.toUpperCase(), toVersion)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                        "To 버전을 찾을 수 없습니다: " + toVersion));

        return generatePatch(from.getReleaseVersionId(), to.getReleaseVersionId(),
                createdBy, description, patchedBy, patchName);
    }

    /**
     * 패치 생성 (버전 ID 기반)
     *
     * @param fromVersionId From 버전 ID
     * @param toVersionId   To 버전 ID
     * @param createdBy     생성자
     * @param description   설명 (선택)
     * @param patchedBy     패치 담당자 (선택)
     * @param patchName     패치 이름 (선택, 미입력 시 자동 생성)
     * @return 생성된 패치
     */
    @Transactional
    public Patch generatePatch(Long fromVersionId, Long toVersionId,
            String createdBy, String description, String patchedBy, String patchName) {
        try {
            // 1. 버전 조회 및 검증
            ReleaseVersion fromVersion = releaseVersionRepository.findById(fromVersionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                            "From 버전을 찾을 수 없습니다: " + fromVersionId));

            ReleaseVersion toVersion = releaseVersionRepository.findById(toVersionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                            "To 버전을 찾을 수 없습니다: " + toVersionId));

            validateVersionRange(fromVersion, toVersion);

            // 2. 중간 버전 목록 조회 (fromVersion < version <= toVersion)
            List<ReleaseVersion> betweenVersions = releaseVersionRepository.findVersionsBetween(
                    fromVersion.getReleaseType(),
                    fromVersion.getVersion(),
                    toVersion.getVersion()
            );

            if (betweenVersions.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        String.format("From %s와 To %s 사이에 패치할 버전이 없습니다.",
                                fromVersion.getVersion(), toVersion.getVersion()));
            }

            log.info("패치 생성 시작 - From: {}, To: {}, 포함 버전: {}",
                    fromVersion.getVersion(), toVersion.getVersion(),
                    betweenVersions.stream().map(ReleaseVersion::getVersion).toList());

            // 3. 패치 이름 결정 (입력값이 없으면 자동 생성: YYYYMMDDHHMMSS_fromversion_toversion)
            String resolvedPatchName = resolvePatchName(patchName, fromVersion.getVersion(), toVersion.getVersion());

            // 4. 출력 디렉토리 생성 (패치 이름으로)
            String outputPath = createOutputDirectory(resolvedPatchName);

            // 5. SQL 파일 복사
            copySqlFiles(betweenVersions, outputPath);

            // 6. 패치 스크립트 생성
            generatePatchScripts(fromVersion, toVersion, betweenVersions, outputPath);

            // 7. README 생성
            generateReadme(fromVersion, toVersion, betweenVersions, outputPath);

            // 8. 패치 저장
            Patch patch = Patch.builder()
                    .releaseType(fromVersion.getReleaseType())
                    .customer(fromVersion.getCustomer())
                    .fromVersion(fromVersion.getVersion())
                    .toVersion(toVersion.getVersion())
                    .patchName(resolvedPatchName)
                    .outputPath(outputPath)
                    .createdBy(createdBy)
                    .description(description)
                    .patchedBy(patchedBy)
                    .build();

            Patch saved = patchRepository.save(patch);

            log.info("패치 생성 완료 - ID: {}, Path: {}", saved.getPatchId(),
                    outputPath);

            return saved;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("패치 생성 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "패치 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 패치 이름 결정
     *
     * @param patchName   입력된 패치 이름 (nullable)
     * @param fromVersion From 버전
     * @param toVersion   To 버전
     * @return 최종 패치 이름 (형식: YYYYMMDDHHmm_fromVersion_toVersion)
     */
    private String resolvePatchName(String patchName, String fromVersion, String toVersion) {
        if (StringUtils.hasText(patchName)) {
            return patchName;
        }
        // 기본값: 날짜시분_fromversion_toversion (예: 202511271430_1.0.0_1.1.1)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        return String.format("%s_%s_%s", timestamp, fromVersion, toVersion);
    }

    /**
     * 버전 범위 검증
     */
    private void validateVersionRange(ReleaseVersion fromVersion, ReleaseVersion toVersion) {
        // 버전 비교: fromVersion < toVersion
        if (compareVersions(fromVersion, toVersion) >= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("From 버전은 To 버전보다 작아야 합니다. (From: %s, To: %s)",
                            fromVersion.getVersion(), toVersion.getVersion()));
        }

        // 릴리즈 타입 일치 검증
        if (!fromVersion.getReleaseType().equals(toVersion.getReleaseType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "From 버전과 To 버전의 릴리즈 타입이 일치하지 않습니다.");
        }
    }

    /**
     * 버전 비교 (v1 < v2 이면 -1, v1 == v2 이면 0, v1 > v2 이면 1)
     */
    private int compareVersions(ReleaseVersion v1, ReleaseVersion v2) {
        if (v1.getMajorVersion() != v2.getMajorVersion()) {
            return Integer.compare(v1.getMajorVersion(), v2.getMajorVersion());
        }
        if (v1.getMinorVersion() != v2.getMinorVersion()) {
            return Integer.compare(v1.getMinorVersion(), v2.getMinorVersion());
        }
        return Integer.compare(v1.getPatchVersion(), v2.getPatchVersion());
    }

    /**
     * 출력 디렉토리 생성
     *
     * @param patchName 패치 이름 (디렉토리명으로 사용)
     * @return 상대 경로 (예: patches/20251127143025_1.0.0_1.1.1)
     */
    private String createOutputDirectory(String patchName) {
        try {
            // 출력 경로: patches/{patchName}
            String relativePath = String.format("patches/%s", patchName);

            Path outputDir = Paths.get(releaseBasePath, relativePath);

            // 루트 디렉토리만 생성 (하위 디렉토리는 파일 복사 시 동적 생성)
            Files.createDirectories(outputDir);

            log.info("출력 디렉토리 생성 완료: {}", outputDir.toAbsolutePath());

            return relativePath;

        } catch (IOException e) {
            log.error("출력 디렉토리 생성 실패: releaseBasePath={}, patchName={}",
                    releaseBasePath, patchName, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "출력 디렉토리 생성 실패: " + releaseBasePath + "/patches/" + patchName);
        }
    }

    /**
     * 모든 파일 복사 (버전별 디렉토리 구조 유지)
     * <p>⚠️ Phase 5: install 카테고리 제외 처리
     */
    private void copySqlFiles(List<ReleaseVersion> versions, String outputPath) {
        try {
            Path outputDir = Paths.get(releaseBasePath, outputPath);

            for (ReleaseVersion version : versions) {
                // 모든 파일 조회
                List<ReleaseFile> allFiles = releaseFileRepository
                        .findAllByReleaseVersionIdOrderByExecutionOrderAsc(
                                version.getReleaseVersionId());

                // Phase 5: install 카테고리 제외
                List<ReleaseFile> files = allFiles.stream()
                        .filter(file -> !file.isExcludedFromPatch())
                        .toList();

                if (files.isEmpty()) {
                    log.warn("버전 {}의 패치 대상 파일이 없습니다.", version.getVersion());
                    continue;
                }

                log.info("버전 {} 패치 대상 파일: {} / {} (install 제외)",
                        version.getVersion(), files.size(), allFiles.size());

                for (ReleaseFile file : files) {
                    copyFileByCategory(file, version, outputDir);
                }

                log.info("버전 {} 파일 복사 완료 - {}개", version.getVersion(), files.size());
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일 복사 실패: " + e.getMessage());
        }
    }

    /**
     * 개별 파일 복사 (카테고리 기반)
     * <p>Phase 5: 파일 카테고리별 디렉토리 구조 생성
     */
    private void copyFileByCategory(ReleaseFile file, ReleaseVersion version, Path outputDir) {
        try {
            // 원본 파일 경로
            Path sourcePath = Paths.get(releaseBasePath, file.getFilePath());

            if (!Files.exists(sourcePath)) {
                log.warn("파일이 존재하지 않습니다: {}", sourcePath);
                return;
            }

            // 대상 파일 경로 결정 (카테고리별)
            Path targetPath = determineTargetPath(file, version, outputDir);

            // 대상 디렉토리 생성
            Files.createDirectories(targetPath.getParent());

            // 파일 복사
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("파일 복사: {} -> {}", sourcePath.getFileName(), targetPath);

        } catch (IOException e) {
            log.error("파일 복사 실패: {}", file.getFileName(), e);
        }
    }

    /**
     * 대상 파일 경로 결정 (카테고리 기반)
     * <p>디렉토리 구조:
     * <ul>
     *   <li>DATABASE: {db_type}/{version}/{file_name}</li>
     *   <li>WEB/API/BATCH: {category}/{version}/{file_name}</li>
     *   <li>CONFIG: config/{file_name}</li>
     * </ul>
     */
    private Path determineTargetPath(ReleaseFile file, ReleaseVersion version, Path outputDir) {
        FileCategory category = file.getFileCategory();

        if (category == null) {
            return outputDir.resolve(
                    String.format("etc/%s/%s",
                            version.getVersion(),
                            file.getFileName())
            );
        }

        switch (category) {
            case DATABASE:
                // sub_category를 소문자로 변환
                String subCategory = file.getSubCategory() != null
                        ? file.getSubCategory().toLowerCase()
                        : "database";
                return outputDir.resolve(
                        String.format("database/%s/%s/%s",
                                subCategory,
                                version.getVersion(),
                                file.getFileName())
                );

            case WEB:
            case ENGINE:
                // category를 소문자로 변환
                return outputDir.resolve(
                        String.format("%s/%s/%s",
                                category.getCode().toLowerCase(),
                                version.getVersion(),
                                file.getFileName())
                );

            case INSTALL:
                log.warn("INSTALL 카테고리 파일이 복사 대상에 포함되었습니다: {}", file.getFileName());
                return outputDir.resolve(
                        String.format("install/%s", file.getFileName())
                );

            default:
                return outputDir.resolve(
                        String.format("etc/%s", file.getFileName())
                );
        }
    }

    private void generatePatchScripts(ReleaseVersion fromVersion, ReleaseVersion toVersion,
            List<ReleaseVersion> versions, String outputPath) {
        try {
            List<ReleaseFile> mariadbFiles = releaseFileRepository.findReleaseFilesBetweenVersionsBySubCategory(
                    versions.get(0).getVersion(),
                    versions.get(versions.size() - 1).getVersion(),
                    "MARIADB"
            );

            List<ReleaseFile> cratedbFiles = releaseFileRepository.findReleaseFilesBetweenVersionsBySubCategory(
                    versions.get(0).getVersion(),
                    versions.get(versions.size() - 1).getVersion(),
                    "CRATEDB"
            );

            if (!mariadbFiles.isEmpty()) {
                scriptGenerator.generateMariaDBPatchScript(fromVersion.getVersion(),
                        toVersion.getVersion(), versions, mariadbFiles, outputPath);
                log.info("MariaDB 패치 스크립트 생성 완료: {}/mariadb_patch.sh", outputPath);
            } else {
                log.warn("MariaDB 파일이 없어 스크립트를 생성하지 않습니다.");
            }

            if (!cratedbFiles.isEmpty()) {
                scriptGenerator.generateCrateDBPatchScript(fromVersion.getVersion(),
                        toVersion.getVersion(), versions, cratedbFiles, outputPath);
                log.info("CrateDB 패치 스크립트 생성 완료: {}/cratedb_patch.sh", outputPath);
            } else {
                log.warn("CrateDB 파일이 없어 스크립트를 생성하지 않습니다.");
            }

        } catch (Exception e) {
            log.error("패치 스크립트 생성 실패: {}", outputPath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "패치 스크립트 생성 실패: " + e.getMessage());
        }
    }


    /**
     * README.md 생성
     */
    private void generateReadme(ReleaseVersion fromVersion, ReleaseVersion toVersion,
            List<ReleaseVersion> includedVersions, String outputPath) {
        try {
            Path readmePath = Paths.get(releaseBasePath, outputPath, "README.md");

            StringBuilder content = new StringBuilder();
            content.append(String.format("# 누적 패치: from-%s to %s\n\n",
                    fromVersion.getVersion(), toVersion.getVersion()));
            content.append("## 개요\n");
            content.append(String.format(
                    "이 패치는 **%s** 버전에서 **%s** 버전으로 업그레이드하기 위한 누적 패치입니다.\n\n",
                    fromVersion.getVersion(), toVersion.getVersion()));

            content.append("## 생성 정보\n");
            content.append(String.format("- **생성일**: %s\n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            content.append(String.format("- **From Version**: %s\n", fromVersion.getVersion()));
            content.append(String.format("- **To Version**: %s\n", toVersion.getVersion()));
            content.append("- **포함된 버전**: ");
            content.append(includedVersions.stream()
                    .map(ReleaseVersion::getVersion)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            content.append("\n\n");

            content.append("## 디렉토리 구조\n");
            content.append("```\n");
            content.append(".\n");
            content.append("├── mariadb_patch.sh            # MariaDB 패치 실행 스크립트\n");
            content.append("├── cratedb_patch.sh            # CrateDB 패치 실행 스크립트\n");
            content.append("├── database/\n");
            content.append("│   ├── mariadb/\n");
            content.append("│   │   ├── {version}/          # 누적된 SQL 파일들\n");
            content.append("│   │   │   └── *.sql\n");
            content.append("│   └── cratedb/\n");
            content.append("│       ├── {version}/          # 누적된 SQL 파일들\n");
            content.append("│       │   └── *.sql\n");
            content.append("└── README.md                   # 이 파일\n");
            content.append("```\n\n");

            content.append("## 주의사항\n");
            content.append("⚠️ **중요**: 이 패치는 여러 버전의 변경사항을 누적한 것입니다.\n");
            content.append("- 패치 실행 전 반드시 백업을 수행하세요.\n");
            content.append("- 패치 실행 중 오류 발생 시 로그를 확인하세요.\n\n");

            content.append("---\n");
            content.append("CREATED BY. Infraeye2 누적 패치 생성기 (Java)\n");

            Files.writeString(readmePath, content.toString());

            log.info("README.md 생성 완료: {}", readmePath);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "README 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 패치 조회
     */
    @Transactional(readOnly = true)
    public Patch getPatch(Long patchId) {
        return patchRepository.findById(patchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "패치를 찾을 수 없습니다: " + patchId));
    }

    /**
     * 패치 목록 페이징 조회
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM, null이면 전체)
     * @param pageable    페이징 정보
     * @return 패치 페이지
     */
    @Transactional(readOnly = true)
    public Page<Patch> listPatchesWithPaging(String releaseType, Pageable pageable) {
        if (releaseType != null) {
            return patchRepository.findAllByReleaseTypeOrderByCreatedAtDesc(
                    releaseType.toUpperCase(), pageable);
        }
        return patchRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 패치 ZIP 다운로드
     *
     * @param patchId 패치 ID
     * @return ZIP 파일 바이트 배열
     */
    @Transactional(readOnly = true)
    public byte[] downloadPatchAsZip(Long patchId) {
        Patch patch = getPatch(patchId);

        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (!Files.exists(patchDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "패치 디렉토리를 찾을 수 없습니다: " + patch.getOutputPath());
        }

        return ZipUtil.compressDirectory(patchDir);
    }

    /**
     * 패치 ZIP 파일명 생성
     *
     * @param patchId 패치 ID
     * @return ZIP 파일명 (예: 202511271430_1.0.0_1.1.1.zip)
     */
    public String getZipFileName(Long patchId) {
        Patch patch = getPatch(patchId);
        return patch.getPatchName() + ".zip";
    }

    /**
     * 패치 ZIP 파일 내부 구조 조회
     *
     * @param patchId 패치 ID
     * @return ZIP 파일 구조 (재귀적인 DirectoryNode)
     */
    @Transactional(readOnly = true)
    public PatchDto.DirectoryNode getZipFileStructure(Long patchId) {
        Patch patch = getPatch(patchId);

        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (!Files.exists(patchDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "패치 디렉토리를 찾을 수 없습니다: " + patch.getOutputPath());
        }

        try {
            // 루트 디렉토리 구조 생성
            return buildDirectoryNode(patchDir, patchDir);

        } catch (IOException e) {
            log.error("패치 디렉토리 구조 조회 실패: {}", patchDir, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "패치 디렉토리 구조를 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 디렉토리 구조를 재귀적으로 생성
     *
     * @param directory 조회할 디렉토리
     * @param basePath  기준 경로
     * @return DirectoryNode (재귀 구조)
     */
    private PatchDto.DirectoryNode buildDirectoryNode(Path directory, Path basePath)
            throws IOException {

        String name = directory.getFileName() != null
                ? directory.getFileName().toString()
                : directory.toString();
        String relativePath = basePath.relativize(directory).toString().replace("\\", "/");

        // 빈 경로인 경우 "." 으로 표시
        if (relativePath.isEmpty()) {
            relativePath = ".";
        }

        java.util.List<PatchDto.FileNode> children = new java.util.ArrayList<>();

        try (var stream = Files.list(directory)) {
            stream.sorted().forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        // 하위 디렉토리 재귀 조회
                        PatchDto.DirectoryNode childDir = buildDirectoryNode(path, basePath);

                        // 빈 디렉토리는 제외 (children이 비어있으면 추가하지 않음)
                        if (!childDir.children().isEmpty()) {
                            children.add(childDir);
                        }
                    } else {
                        // 파일 정보 추가
                        children.add(buildFileInfo(path, basePath));
                    }
                } catch (IOException e) {
                    log.warn("파일/디렉토리 정보 조회 실패: {}", path, e);
                }
            });
        }

        return new PatchDto.DirectoryNode(name, "directory", relativePath, children);
    }

    /**
     * 파일 정보 생성
     */
    private PatchDto.FileInfo buildFileInfo(Path filePath, Path basePath) throws IOException {
        String name = filePath.getFileName().toString();
        long size = Files.size(filePath);
        String relativePath = basePath.relativize(filePath).toString().replace("\\", "/");

        return new PatchDto.FileInfo(name, size, "file", relativePath);
    }

    /**
     * 패치 삭제 (DB 레코드 + 실제 파일)
     *
     * @param patchId 패치 ID
     */
    @Transactional
    public void deletePatch(Long patchId) {
        // 1. 패치 조회
        Patch patch = getPatch(patchId);

        // 2. 실제 파일 디렉토리 삭제
        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (Files.exists(patchDir)) {
            try {
                deleteDirectoryRecursively(patchDir);
                log.info("패치 디렉토리 삭제 완료: {}", patchDir.toAbsolutePath());
            } catch (IOException e) {
                log.error("패치 디렉토리 삭제 실패: {}", patchDir, e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "패치 파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
            }
        } else {
            log.warn("패치 디렉토리가 존재하지 않습니다: {}", patchDir);
        }

        // 3. DB 레코드 삭제
        patchRepository.delete(patch);

        log.info("패치 삭제 완료 - ID: {}, Name: {}", patchId, patch.getPatchName());
    }

    /**
     * 디렉토리 재귀적 삭제
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (var stream = Files.walk(directory)) {
            stream.sorted((p1, p2) -> -p1.compareTo(p2)) // 역순 정렬 (하위 항목부터 삭제)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.debug("파일/디렉토리 삭제: {}", path);
                        } catch (IOException e) {
                            log.warn("파일/디렉토리 삭제 실패: {}", path, e);
                        }
                    });
        }
    }

    /**
     * 패치 파일 내용 조회
     *
     * @param patchId      패치 ID
     * @param relativePath 파일 상대 경로 (예: mariadb/source_files/1.1.1/1.patch_mariadb_ddl.sql)
     * @return 파일 내용
     */
    @Transactional(readOnly = true)
    public PatchDto.FileContentResponse getFileContent(Long patchId, String relativePath) {
        Patch patch = getPatch(patchId);

        // 패치 디렉토리 경로
        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (!Files.exists(patchDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "패치 디렉토리를 찾을 수 없습니다: " + patch.getOutputPath());
        }

        // 상대 경로 검증 및 파일 경로 생성
        Path filePath = validateAndResolvePath(patchDir, relativePath);

        // 파일 존재 확인
        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "파일을 찾을 수 없습니다: " + relativePath);
        }

        if (!Files.isRegularFile(filePath)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "디렉토리는 조회할 수 없습니다: " + relativePath);
        }

        try {
            // 파일 크기 확인 (10MB 제한)
            long fileSize = Files.size(filePath);
            if (fileSize > 10 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "파일 크기가 너무 큽니다 (최대 10MB): " + fileSize + " bytes");
            }

            // 파일 내용 읽기 (UTF-8)
            String content = Files.readString(filePath);

            String fileName = filePath.getFileName().toString();

            return new PatchDto.FileContentResponse(
                    patchId,
                    relativePath,
                    fileName,
                    fileSize,
                    content
            );

        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", filePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일을 읽을 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 경로 검증 및 해석 (경로 탐색 공격 방지)
     *
     * @param baseDir      기준 디렉토리
     * @param relativePath 상대 경로
     * @return 해석된 절대 경로
     * @throws BusinessException 경로가 기준 디렉토리 외부를 가리키는 경우
     */
    private Path validateAndResolvePath(Path baseDir, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "파일 경로가 비어있습니다");
        }

        try {
            // 상대 경로를 절대 경로로 변환
            Path resolvedPath = baseDir.resolve(relativePath).normalize();

            // 경로 탐색 공격 방지: 해석된 경로가 기준 디렉토리 내부에 있는지 확인
            if (!resolvedPath.startsWith(baseDir)) {
                log.warn("경로 탐색 공격 시도 감지: baseDir={}, relativePath={}, resolved={}",
                        baseDir, relativePath, resolvedPath);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "유효하지 않은 파일 경로입니다");
            }

            return resolvedPath;

        } catch (Exception e) {
            log.error("경로 해석 실패: baseDir={}, relativePath={}",
                    baseDir, relativePath, e);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "유효하지 않은 파일 경로입니다: " + e.getMessage());
        }
    }
}
