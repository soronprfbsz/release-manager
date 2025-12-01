package com.ts.rm.domain.releaseversion.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ts.rm.domain.releaseversion.dto.ReleaseMetadataDto;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * release_metadata.json 파일 관리
 *
 * <p>버전 정보를 JSON 형식으로 관리
 */
@Slf4j
@Component
public class ReleaseMetadataManager {

    private final String baseReleasePath;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd");

    public ReleaseMetadataManager(
            @Value("${app.release.base-path:src/main/resources/release}") String basePath) {
        this.baseReleasePath = basePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
    }

    /**
     * release_metadata.json에 새 버전 정보 추가
     *
     * @param version 릴리즈 버전
     */
    public void addVersionEntry(ReleaseVersion version) {
        try {
            // release_metadata.json 경로 결정
            Path metadataPath = getMetadataPath(version);

            // 기존 문서 읽기 또는 새로 생성
            ReleaseMetadataDto.MetadataDocument document = readMetadataDocument(metadataPath);

            // 새 버전 항목 생성
            ReleaseMetadataDto.MetadataEntry newEntry = new ReleaseMetadataDto.MetadataEntry(
                    version.getVersion(),
                    version.getCreatedAt().format(DATE_FORMATTER),
                    version.getCreatedBy(),
                    version.getComment() != null ? version.getComment() : ""
            );

            // 맨 앞에 추가 (최신 버전이 위로)
            List<ReleaseMetadataDto.MetadataEntry> versions = new ArrayList<>(document.versions());
            versions.add(0, newEntry);

            // 문서 업데이트
            ReleaseMetadataDto.MetadataDocument updatedDocument = new ReleaseMetadataDto.MetadataDocument(
                    versions);

            // JSON 파일 쓰기
            writeMetadataDocument(metadataPath, updatedDocument);

            log.info("release_metadata.json 업데이트 완료: {}", version.getVersion());

        } catch (IOException e) {
            log.error("release_metadata.json 업데이트 실패: {}", version.getVersion(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "release_metadata.json 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * release_metadata.json에서 특정 버전 엔트리 제거
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param version     버전 번호
     */
    public void removeVersionEntry(String releaseType, String version) {
        try {
            // release_metadata.json 경로 결정
            Path metadataPath;
            if ("STANDARD".equals(releaseType)) {
                metadataPath = Paths.get(baseReleasePath, "versions/standard/release_metadata.json");
            } else {
                // CUSTOM의 경우 customerCode가 필요하지만, 삭제 시점에는 알 수 없으므로 스킵
                log.warn("Cannot remove custom version entry without customerCode");
                return;
            }

            if (!Files.exists(metadataPath)) {
                log.warn("release_metadata.json does not exist: {}", metadataPath);
                return;
            }

            // 기존 문서 읽기
            ReleaseMetadataDto.MetadataDocument document = readMetadataDocument(metadataPath);

            // 해당 버전 제거
            List<ReleaseMetadataDto.MetadataEntry> versions = new ArrayList<>(document.versions());
            versions.removeIf(entry -> entry.version().equals(version));

            // 문서 업데이트
            ReleaseMetadataDto.MetadataDocument updatedDocument = new ReleaseMetadataDto.MetadataDocument(
                    versions);

            // JSON 파일 쓰기
            writeMetadataDocument(metadataPath, updatedDocument);

            log.info("release_metadata.json에서 버전 제거 완료: {}", version);

        } catch (IOException e) {
            log.error("release_metadata.json 버전 제거 실패: {}", version, e);
            // 삭제 작업이므로 예외를 던지지 않음
        }
    }

    /**
     * release_metadata.json에서 특정 버전의 메타데이터 조회
     *
     * @param releaseType  릴리즈 타입 (STANDARD/CUSTOM)
     * @param customerCode 고객사 코드 (CUSTOM인 경우 필수)
     * @param version      버전 번호
     * @return 버전 메타데이터 (없으면 null)
     */
    public VersionMetadata getVersionMetadata(String releaseType, String customerCode,
            String version) {
        try {
            // release_metadata.json 경로 결정
            Path metadataPath;
            if ("STANDARD".equals(releaseType)) {
                metadataPath = Paths.get(baseReleasePath, "versions/standard/release_metadata.json");
            } else {
                if (customerCode == null) {
                    log.warn("customerCode is required for CUSTOM release type");
                    return null;
                }
                metadataPath = Paths.get(baseReleasePath, "versions/custom", customerCode,
                        "release_metadata.json");
            }

            if (!Files.exists(metadataPath)) {
                log.warn("release_metadata.json does not exist: {}", metadataPath);
                return null;
            }

            // 문서 읽기
            ReleaseMetadataDto.MetadataDocument document = readMetadataDocument(metadataPath);

            // 해당 버전 찾기
            return document.versions().stream()
                    .filter(entry -> entry.version().equals(version))
                    .findFirst()
                    .map(entry -> new VersionMetadata(
                            entry.version(),
                            entry.createdAt(),
                            entry.createdBy(),
                            entry.comment(),
                            null // customVersion은 향후 추가 가능
                    ))
                    .orElse(null);

        } catch (IOException e) {
            log.error("release_metadata.json 메타데이터 조회 실패: {}", version, e);
            return null;
        }
    }

    /**
     * release_metadata.json 경로 결정
     */
    private Path getMetadataPath(ReleaseVersion version) {
        String relativePath;

        if ("STANDARD".equals(version.getReleaseType())) {
            relativePath = "versions/standard/release_metadata.json";
        } else {
            // CUSTOM인 경우 고객사 폴더
            String customerCode = version.getCustomer() != null
                    ? version.getCustomer().getCustomerCode()
                    : "unknown";
            relativePath = String.format("versions/custom/%s/release_metadata.json", customerCode);
        }

        return Paths.get(baseReleasePath, relativePath);
    }

    /**
     * release_metadata.json 문서 읽기
     */
    private ReleaseMetadataDto.MetadataDocument readMetadataDocument(Path metadataPath)
            throws IOException {
        if (!Files.exists(metadataPath)) {
            // 파일이 없으면 빈 문서 생성
            Files.createDirectories(metadataPath.getParent());
            return new ReleaseMetadataDto.MetadataDocument(new ArrayList<>());
        }

        String content = Files.readString(metadataPath);
        if (content.trim().isEmpty()) {
            return new ReleaseMetadataDto.MetadataDocument(new ArrayList<>());
        }

        return objectMapper.readValue(content, ReleaseMetadataDto.MetadataDocument.class);
    }

    /**
     * release_metadata.json 문서 쓰기
     */
    private void writeMetadataDocument(Path metadataPath,
            ReleaseMetadataDto.MetadataDocument document) throws IOException {
        Files.createDirectories(metadataPath.getParent());
        String json = objectMapper.writeValueAsString(document);
        Files.writeString(metadataPath, json);
    }

    /**
     * 버전 메타데이터 (release_metadata.json에서 읽어온 정보)
     */
    public record VersionMetadata(
            String version,
            String createdAt,
            String createdBy,
            String comment,
            String customVersion
    ) {

    }
}
