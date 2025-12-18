package com.ts.rm.domain.patch.service;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.entity.CustomerProject;
import com.ts.rm.domain.customer.repository.CustomerProjectRepository;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.domain.engineer.entity.Engineer;
import com.ts.rm.domain.engineer.repository.EngineerRepository;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.repository.PatchRepository;
import com.ts.rm.domain.patch.util.ScriptGenerator;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.project.repository.ProjectRepository;
import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
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
import org.springframework.util.StringUtils;

/**
 * 패치 생성 서비스
 *
 * <p>패치 파일 생성, SQL 파일 복사, 스크립트 생성 등의 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatchGenerationService {

    private final PatchRepository patchRepository;
    private final ReleaseVersionRepository releaseVersionRepository;
    private final ReleaseFileRepository releaseFileRepository;
    private final CustomerRepository customerRepository;
    private final CustomerProjectRepository customerProjectRepository;
    private final EngineerRepository engineerRepository;
    private final ProjectRepository projectRepository;
    private final ScriptGenerator mariaDBScriptGenerator;
    private final ScriptGenerator crateDBScriptGenerator;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String releaseBasePath;

    /**
     * 패치 생성 (버전 문자열 기반)
     *
     * @param projectId    프로젝트 ID
     * @param releaseType  릴리즈 타입 (STANDARD/CUSTOM)
     * @param customerId   고객사 ID (CUSTOM인 경우)
     * @param fromVersion  From 버전 (예: 1.0.0)
     * @param toVersion    To 버전 (예: 1.1.1)
     * @param createdBy    생성자
     * @param description  설명 (선택)
     * @param engineerId   패치 담당자 엔지니어 ID (선택)
     * @param patchName    패치 이름 (선택, 미입력 시 자동 생성)
     * @return 생성된 패치
     */
    @Transactional
    public Patch generatePatchByVersion(String projectId, String releaseType, Long customerId,
            String fromVersion, String toVersion, String createdBy, String description,
            Long engineerId, String patchName) {

        // 버전 조회 (프로젝트 내에서)
        ReleaseVersion from = releaseVersionRepository.findByProject_ProjectIdAndReleaseTypeAndVersion(
                        projectId, releaseType.toUpperCase(), fromVersion)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                        "From 버전을 찾을 수 없습니다: " + fromVersion));

        ReleaseVersion to = releaseVersionRepository.findByProject_ProjectIdAndReleaseTypeAndVersion(
                        projectId, releaseType.toUpperCase(), toVersion)
                .orElseThrow(() -> new BusinessException(ErrorCode.RELEASE_VERSION_NOT_FOUND,
                        "To 버전을 찾을 수 없습니다: " + toVersion));

        return generatePatch(projectId, from.getReleaseVersionId(), to.getReleaseVersionId(),
                customerId, createdBy, description, engineerId, patchName);
    }

    /**
     * 패치 생성 (버전 ID 기반)
     *
     * @param projectId     프로젝트 ID
     * @param fromVersionId From 버전 ID
     * @param toVersionId   To 버전 ID
     * @param customerId    고객사 ID (선택)
     * @param createdBy     생성자
     * @param description   설명 (선택)
     * @param engineerId    패치 담당자 엔지니어 ID (선택)
     * @param patchName     패치 이름 (선택, 미입력 시 자동 생성)
     * @return 생성된 패치
     */
    @Transactional
    public Patch generatePatch(String projectId, Long fromVersionId, Long toVersionId, Long customerId,
            String createdBy, String description, Long engineerId, String patchName) {
        try {
            // 프로젝트 조회
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND,
                            "프로젝트를 찾을 수 없습니다: " + projectId));

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

            log.info("패치 생성 시작 - Project: {}, From: {}, To: {}, 포함 버전: {}",
                    projectId, fromVersion.getVersion(), toVersion.getVersion(),
                    betweenVersions.stream().map(ReleaseVersion::getVersion).toList());

            // 3. 고객사 조회 (customerId가 있는 경우)
            Customer customer = null;
            if (customerId != null) {
                customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND,
                                "고객사를 찾을 수 없습니다: " + customerId));
            }

            // 3-1. 엔지니어 조회 (engineerId가 있는 경우)
            Engineer engineer = null;
            if (engineerId != null) {
                engineer = engineerRepository.findById(engineerId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                                "엔지니어를 찾을 수 없습니다: " + engineerId));
            }

            // 4. 패치 이름 결정 (입력값이 없으면 자동 생성: YYYYMMDDHHMMSS_fromversion_toversion)
            String resolvedPatchName = resolvePatchName(patchName, fromVersion.getVersion(), toVersion.getVersion());

            // 5. 출력 디렉토리 생성 (패치 이름으로)
            String outputPath = createOutputDirectory(resolvedPatchName, projectId);

            // 6. SQL 파일 복사
            copySqlFiles(betweenVersions, outputPath);

            // 7. 패치 스크립트 생성
            String engineerName = engineer != null ? engineer.getEngineerName() : null;
            generatePatchScripts(fromVersion, toVersion, betweenVersions, outputPath, engineerName);

            // 8. README 생성
            generateReadme(fromVersion, toVersion, betweenVersions, outputPath);

            // 9. 패치 저장
            Patch patch = Patch.builder()
                    .project(project)
                    .releaseType(fromVersion.getReleaseType())
                    .customer(customer)
                    .fromVersion(fromVersion.getVersion())
                    .toVersion(toVersion.getVersion())
                    .patchName(resolvedPatchName)
                    .outputPath(outputPath)
                    .createdBy(createdBy)
                    .description(description)
                    .engineer(engineer)
                    .build();

            Patch saved = patchRepository.save(patch);

            // 10. CustomerProject 마지막 패치 정보 업데이트 (고객사가 지정된 경우)
            if (customer != null) {
                updateCustomerProjectPatchInfo(customer, project, toVersion.getVersion());
            }

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

        // 임시버전(미승인 버전) 검증
        List<ReleaseVersion> unapprovedVersions = releaseVersionRepository.findUnapprovedVersionsBetween(
                fromVersion.getReleaseType(),
                fromVersion.getVersion(),
                toVersion.getVersion()
        );

        if (!unapprovedVersions.isEmpty()) {
            String unapprovedVersionList = unapprovedVersions.stream()
                    .map(ReleaseVersion::getVersion)
                    .reduce((v1, v2) -> v1 + ", " + v2)
                    .orElse("");

            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("버전 범위 내에 미승인 버전이 존재합니다. 패치를 생성할 수 없습니다. (미승인 버전: %s)",
                            unapprovedVersionList));
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
     * @return 상대 경로 (예: patches/{projectId}/20251127143025_1.0.0_1.1.1)
     */
    private String createOutputDirectory(String patchName, String projectId) {
        try {
            // 출력 경로: patches/{projectId}/{patchName}
            String relativePath = String.format("patches/%s/%s", projectId, patchName);

            Path outputDir = Paths.get(releaseBasePath, relativePath);

            // 루트 디렉토리만 생성 (하위 디렉토리는 파일 복사 시 동적 생성)
            Files.createDirectories(outputDir);

            log.info("출력 디렉토리 생성 완료: {}", outputDir.toAbsolutePath());

            return relativePath;

        } catch (IOException e) {
            log.error("출력 디렉토리 생성 실패: releaseBasePath={}, projectId={}, patchName={}",
                    releaseBasePath, projectId, patchName, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "출력 디렉토리 생성 실패: " + releaseBasePath + "/patches/" + projectId + "/" + patchName);
        }
    }

    /**
     * 모든 파일 복사 (버전별 디렉토리 구조 유지)
     * <p>⚠️ INSTALL 카테고리 버전은 패치 생성에서 제외됩니다.
     */
    private void copySqlFiles(List<ReleaseVersion> versions, String outputPath) {
        try {
            Path outputDir = Paths.get(releaseBasePath, outputPath);

            for (ReleaseVersion version : versions) {
                // INSTALL 카테고리 버전은 패치에서 제외
                if (version.getReleaseCategory() != null
                        && version.getReleaseCategory().isExcludedFromPatch()) {
                    log.info("버전 {}는 INSTALL 카테고리이므로 패치에서 제외됩니다.", version.getVersion());
                    continue;
                }

                // 모든 파일 조회
                List<ReleaseFile> files = releaseFileRepository
                        .findAllByReleaseVersion_ReleaseVersionIdOrderByExecutionOrderAsc(
                                version.getReleaseVersionId());

                if (files.isEmpty()) {
                    log.warn("버전 {}의 패치 대상 파일이 없습니다.", version.getVersion());
                    continue;
                }

                log.info("버전 {} 패치 대상 파일: {}개", version.getVersion(), files.size());

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

            default:
                return outputDir.resolve(
                        String.format("etc/%s", file.getFileName())
                );
        }
    }

    /**
     * 패치 스크립트 생성 (MariaDB, CrateDB)
     */
    private void generatePatchScripts(ReleaseVersion fromVersion, ReleaseVersion toVersion,
            List<ReleaseVersion> versions, String outputPath, String patchedBy) {
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

            // MariaDB 스크립트는 항상 생성 (VERSION_HISTORY INSERT를 위해 필수)
            // SQL 파일이 없더라도 VERSION_HISTORY에 버전 이력을 기록해야 함
            mariaDBScriptGenerator.generatePatchScript(fromVersion.getVersion(),
                    toVersion.getVersion(), versions, mariadbFiles, outputPath, patchedBy);
            if (mariadbFiles.isEmpty()) {
                log.info("MariaDB SQL 파일은 없지만 VERSION_HISTORY 기록을 위해 스크립트 생성: {}/{}", outputPath, mariaDBScriptGenerator.getScriptFileName());
            } else {
                log.info("MariaDB 패치 스크립트 생성 완료: {}/{} (SQL 파일 {}개)", outputPath, mariaDBScriptGenerator.getScriptFileName(), mariadbFiles.size());
            }

            // CrateDB 스크립트는 파일이 있을 때만 생성
            if (!cratedbFiles.isEmpty()) {
                crateDBScriptGenerator.generatePatchScript(fromVersion.getVersion(),
                        toVersion.getVersion(), versions, cratedbFiles, outputPath, null);
                log.info("CrateDB 패치 스크립트 생성 완료: {}/{}", outputPath, crateDBScriptGenerator.getScriptFileName());
            } else {
                log.info("CrateDB 파일이 없어 스크립트를 생성하지 않습니다.");
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
            content.append("CREATED BY - Release Manager\n");

            Files.writeString(readmePath, content.toString());

            log.info("README.md 생성 완료: {}", readmePath);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "README 생성 실패: " + e.getMessage());
        }
    }

    /**
     * CustomerProject 마지막 패치 정보 업데이트
     *
     * <p>고객사-프로젝트 매핑이 없으면 새로 생성하고, 있으면 업데이트합니다.
     *
     * @param customer  고객사
     * @param project   프로젝트
     * @param toVersion 패치된 버전 (to_version)
     */
    private void updateCustomerProjectPatchInfo(Customer customer, Project project, String toVersion) {
        CustomerProject customerProject = customerProjectRepository
                .findByCustomer_CustomerIdAndProject_ProjectId(customer.getCustomerId(), project.getProjectId())
                .orElseGet(() -> {
                    // 매핑이 없으면 새로 생성
                    log.info("고객사-프로젝트 매핑 생성 - customerId: {}, projectId: {}",
                            customer.getCustomerId(), project.getProjectId());
                    return CustomerProject.create(customer, project);
                });

        // 마지막 패치 정보 업데이트
        customerProject.updateLastPatchInfo(toVersion, LocalDateTime.now());
        customerProjectRepository.save(customerProject);

        log.info("CustomerProject 업데이트 완료 - customerId: {}, projectId: {}, lastPatchedVersion: {}",
                customer.getCustomerId(), project.getProjectId(), toVersion);
    }
}
