package com.ts.rm.domain.releaseversion.service;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.util.ReleaseMetadataManager;
import com.ts.rm.domain.releaseversion.util.VersionParser.VersionInfo;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ReleaseVersion FileSystem Service
 *
 * <p>릴리즈 버전의 파일 시스템 관리 (디렉토리 생성/삭제)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReleaseVersionFileSystemService {

    private final ReleaseMetadataManager metadataManager;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String baseReleasePath;

    /**
     * 릴리즈 디렉토리 구조 생성
     *
     * <pre>
     * versions/{projectId}/{type}/{majorMinor}.x/{version}/mariadb/
     * versions/{projectId}/{type}/{majorMinor}.x/{version}/cratedb/
     * </pre>
     */
    public void createDirectoryStructure(ReleaseVersion version, Customer customer) {
        try {
            String projectId = version.getProject() != null ? version.getProject().getProjectId() : "infraeye2";
            String basePath;

            if ("STANDARD".equals(version.getReleaseType())) {
                basePath = String.format("versions/%s/standard/%s/%s",
                        projectId,
                        version.getMajorMinor(),
                        version.getVersion());
            } else {
                // CUSTOM인 경우 고객사 코드 사용
                String customerCode = customer != null ? customer.getCustomerCode() : "unknown";
                basePath = String.format("versions/%s/custom/%s/%s/%s",
                        projectId,
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
     * 버전 디렉토리 생성
     *
     * @param versionInfo 버전 정보
     * @param projectId   프로젝트 ID
     * @return 생성된 버전 경로
     */
    public Path createVersionDirectory(VersionInfo versionInfo, String projectId) throws IOException {
        // 경로: resources/release/versions/{projectId}/standard/{major}.{minor}.x/{version}/
        String majorMinor = versionInfo.getMajorMinor();
        String version = versionInfo.getMajorVersion() + "." + versionInfo.getMinorVersion() + "." + versionInfo.getPatchVersion();

        Path versionPath = Paths.get(baseReleasePath, "versions", projectId, "standard",
                majorMinor, version);

        Files.createDirectories(versionPath);
        log.info("버전 디렉토리 생성: {}", versionPath);

        return versionPath;
    }

    /**
     * 버전 디렉토리 삭제
     *
     * @param version 릴리즈 버전 엔티티
     */
    public void deleteVersionDirectory(ReleaseVersion version) {
        String projectId = version.getProject() != null ? version.getProject().getProjectId() : "infraeye2";
        Path versionPath;

        if ("STANDARD".equals(version.getReleaseType())) {
            versionPath = Paths.get(baseReleasePath, "versions", projectId, "standard",
                    version.getMajorMinor(), version.getVersion());
        } else {
            String customerCode = version.getCustomer() != null
                    ? version.getCustomer().getCustomerCode()
                    : "unknown";
            versionPath = Paths.get(baseReleasePath, "versions", projectId, "custom",
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
     * 디렉토리 재귀 삭제
     */
    public void deleteDirectory(Path directory) {
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
    public boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.findAny().isEmpty();
        }
    }

    /**
     * 파일시스템 롤백 (버전 디렉토리 및 release_metadata.json 복원)
     *
     * @param versionDir 생성된 버전 디렉토리 경로
     * @param version    버전 번호
     */
    public void rollbackFileSystem(String versionDir, String version) {
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
}
