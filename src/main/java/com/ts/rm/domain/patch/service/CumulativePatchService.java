package com.ts.rm.domain.patch.service;

import com.ts.rm.domain.patch.entity.CumulativePatch;
import com.ts.rm.domain.patch.repository.CumulativePatchRepository;
import com.ts.rm.domain.patch.util.ScriptGenerator;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CumulativePatch Service
 *
 * <p>누적 패치 생성 및 관리를 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CumulativePatchService {

    private final CumulativePatchRepository cumulativePatchRepository;
    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseFileRepository releaseFileRepository;
    private final ScriptGenerator scriptGenerator;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String releaseBasePath;

    /**
     * 누적 패치 생성 (버전 문자열 기반)
     *
     * @param releaseType  릴리즈 타입 (STANDARD/CUSTOM)
     * @param customerId   고객사 ID (CUSTOM인 경우)
     * @param fromVersion  From 버전 (예: 1.0.0)
     * @param toVersion    To 버전 (예: 1.1.1)
     * @param createdBy    생성자
     * @return 생성된 누적 패치 이력
     */
    @Transactional
    public CumulativePatch generateCumulativePatchByVersion(String releaseType, Long customerId,
            String fromVersion, String toVersion, String createdBy) {

        // 버전 조회
        ReleaseVersion from = releaseVersionRepository.findByReleaseTypeAndVersion(
                        releaseType.toUpperCase(), fromVersion)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                        "From 버전을 찾을 수 없습니다: " + fromVersion));

        ReleaseVersion to = releaseVersionRepository.findByReleaseTypeAndVersion(
                        releaseType.toUpperCase(), toVersion)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                        "To 버전을 찾을 수 없습니다: " + toVersion));

        return generateCumulativePatch(from.getReleaseVersionId(), to.getReleaseVersionId(),
                createdBy);
    }

    /**
     * 누적 패치 생성 (버전 ID 기반)
     *
     * @param fromVersionId From 버전 ID
     * @param toVersionId   To 버전 ID
     * @param createdBy     생성자
     * @return 생성된 누적 패치 이력
     */
    @Transactional
    public CumulativePatch generateCumulativePatch(Long fromVersionId, Long toVersionId,
            String createdBy) {
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

            log.info("누적 패치 생성 시작 - From: {}, To: {}, 포함 버전: {}",
                    fromVersion.getVersion(), toVersion.getVersion(),
                    betweenVersions.stream().map(ReleaseVersion::getVersion).toList());

            // 3. 출력 디렉토리 생성
            String outputPath = createOutputDirectory(fromVersion, toVersion);

            // 4. SQL 파일 복사
            copySqlFiles(betweenVersions, outputPath);

            // 5. 패치 스크립트 생성
            generatePatchScripts(fromVersion, toVersion, betweenVersions, outputPath);

            // 6. README 생성
            generateReadme(fromVersion, toVersion, betweenVersions, outputPath);

            // 7. 누적 패치 이력 저장
            CumulativePatch cumulativePatch = CumulativePatch.builder()
                    .releaseType(fromVersion.getReleaseType())
                    .customer(fromVersion.getCustomer())
                    .fromVersion(fromVersion.getVersion())
                    .toVersion(toVersion.getVersion())
                    .patchName("from-" + fromVersion.getVersion())
                    .outputPath(outputPath)
                    .generatedAt(LocalDateTime.now())
                    .generatedBy(createdBy)
                    .status("SUCCESS")
                    .build();

            CumulativePatch saved = cumulativePatchRepository.save(cumulativePatch);

            log.info("누적 패치 생성 완료 - ID: {}, Path: {}", saved.getCumulativePatchId(),
                    outputPath);

            return saved;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("누적 패치 생성 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "누적 패치 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
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
     */
    private String createOutputDirectory(ReleaseVersion fromVersion, ReleaseVersion toVersion) {
        try {
            // 출력 경로: releases/{type}/{majorMinor}.x/{toVersion}/from-{fromVersion}
            String relativePath = String.format("releases/%s/%s.x/%s/from-%s",
                    fromVersion.getReleaseType().toLowerCase(),
                    toVersion.getMajorMinor(),
                    toVersion.getVersion(),
                    fromVersion.getVersion());

            Path outputDir = Paths.get(releaseBasePath, relativePath);

            // 디렉토리 구조 생성
            Files.createDirectories(outputDir);
            Files.createDirectories(outputDir.resolve("mariadb/source_files"));
            Files.createDirectories(outputDir.resolve("cratedb/source_files"));

            log.info("출력 디렉토리 생성 완료: {}", outputDir.toAbsolutePath());

            return relativePath;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "출력 디렉토리 생성 실패: " + e.getMessage());
        }
    }

    /**
     * SQL 파일 복사 (버전별 디렉토리 구조 유지)
     */
    private void copySqlFiles(List<ReleaseVersion> versions, String outputPath) {
        try {
            Path outputDir = Paths.get(releaseBasePath, outputPath);

            for (ReleaseVersion version : versions) {
                List<ReleaseFile> files = releaseFileRepository
                        .findAllByReleaseVersionIdOrderByExecutionOrderAsc(
                                version.getReleaseVersionId());

                if (files.isEmpty()) {
                    log.warn("버전 {}의 파일이 없습니다.", version.getVersion());
                    continue;
                }

                for (ReleaseFile file : files) {
                    copyFile(file, version, outputDir);
                }

                log.info("버전 {} 파일 복사 완료 - {}개", version.getVersion(), files.size());
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "SQL 파일 복사 실패: " + e.getMessage());
        }
    }

    /**
     * 개별 파일 복사
     */
    private void copyFile(ReleaseFile file, ReleaseVersion version, Path outputDir) {
        try {
            // 원본 파일 경로
            Path sourcePath = Paths.get(releaseBasePath, file.getFilePath());

            if (!Files.exists(sourcePath)) {
                log.warn("파일이 존재하지 않습니다: {}", sourcePath);
                return;
            }

            // 대상 파일 경로: {db_type}/source_files/{version}/{file_name}
            Path targetPath = outputDir.resolve(
                    String.format("%s/source_files/%s/%s",
                            file.getDatabaseType(),
                            version.getVersion(),
                            file.getFileName())
            );

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
     * 패치 스크립트 생성 (mariadb_patch.sh, cratedb_patch.sh)
     */
    private void generatePatchScripts(ReleaseVersion fromVersion, ReleaseVersion toVersion,
            List<ReleaseVersion> versions, String outputPath) {
        try {
            // MariaDB용 파일 조회
            List<ReleaseFile> mariadbFiles = releaseFileRepository.findReleaseFilesBetweenVersions(
                    versions.get(0).getVersion(),
                    versions.get(versions.size() - 1).getVersion(),
                    "MARIADB"
            );

            // CrateDB용 파일 조회
            List<ReleaseFile> cratedbFiles = releaseFileRepository.findReleaseFilesBetweenVersions(
                    versions.get(0).getVersion(),
                    versions.get(versions.size() - 1).getVersion(),
                    "CRATEDB"
            );

            // MariaDB 스크립트 생성
            if (!mariadbFiles.isEmpty()) {
                scriptGenerator.generateMariaDBPatchScript(fromVersion.getVersion(),
                        toVersion.getVersion(), versions, mariadbFiles, outputPath);
                log.info("MariaDB 패치 스크립트 생성 완료: {}/mariadb_patch.sh", outputPath);
            } else {
                log.warn("MariaDB 파일이 없어 스크립트를 생성하지 않습니다.");
            }

            // CrateDB 스크립트 생성
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
            content.append(String.format("from-%s/\n", fromVersion.getVersion()));
            content.append("├── mariadb/\n");
            content.append("│   ├── mariadb_patch.sh        # MariaDB 패치 실행 스크립트\n");
            content.append("│   └── source_files/           # 누적된 SQL 파일들\n");
            content.append("├── cratedb/\n");
            content.append("│   ├── cratedb_patch.sh        # CrateDB 패치 실행 스크립트\n");
            content.append("│   └── source_files/           # 누적된 SQL 파일들\n");
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
     * 누적 패치 이력 조회
     */
    @Transactional(readOnly = true)
    public CumulativePatch getCumulativePatch(Long cumulativePatchId) {
        return cumulativePatchRepository.findById(cumulativePatchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "누적 패치 이력을 찾을 수 없습니다: " + cumulativePatchId));
    }

    /**
     * 누적 패치 이력 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CumulativePatch> listCumulativePatches(String releaseType) {
        if (releaseType != null) {
            return cumulativePatchRepository.findAllByReleaseTypeOrderByGeneratedAtDesc(
                    releaseType);
        }
        return cumulativePatchRepository.findAll();
    }
}
