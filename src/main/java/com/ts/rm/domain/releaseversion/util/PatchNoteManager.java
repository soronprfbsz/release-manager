package com.ts.rm.domain.releaseversion.util;

import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * patch_note.md 파일 관리
 *
 * <p>기존 파일 기반 시스템과의 호환성을 위해 patch_note.md 파일을 자동 업데이트
 */
@Slf4j
@Component
public class PatchNoteManager {

    private final String baseReleasePath;
    private static final String SEPARATOR_LINE = "=========================================================";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd");

    public PatchNoteManager(
            @Value("${app.release.base-path:src/main/resources/release}") String basePath) {
        this.baseReleasePath = basePath;
    }

    /**
     * patch_note.md에 새 버전 정보 추가
     *
     * @param version 릴리즈 버전
     */
    public void addVersionEntry(ReleaseVersion version) {
        try {
            // patch_note.md 경로 결정
            Path patchNotePath = getPatchNotePath(version);

            // patch_note.md 파일이 없으면 생성
            if (!Files.exists(patchNotePath)) {
                Files.createDirectories(patchNotePath.getParent());
                Files.createFile(patchNotePath);
                log.info("patch_note.md 파일 생성: {}", patchNotePath);
            }

            // 버전 정보 포맷팅
            String versionEntry = formatVersionEntry(version);

            // 파일 맨 앞에 추가 (최신 버전이 위로)
            String existingContent = Files.exists(patchNotePath)
                    ? Files.readString(patchNotePath)
                    : "";

            String newContent = versionEntry + "\n" + existingContent;

            // 파일 쓰기
            Files.writeString(patchNotePath, newContent);

            log.info("patch_note.md 업데이트 완료: {}", version.getVersion());

        } catch (IOException e) {
            log.error("patch_note.md 업데이트 실패: {}", version.getVersion(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "patch_note.md 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * patch_note.md 경로 결정
     */
    private Path getPatchNotePath(ReleaseVersion version) {
        String relativePath;

        if ("STANDARD".equals(version.getReleaseType())) {
            relativePath = "versions/standard/patch_note.md";
        } else {
            // CUSTOM인 경우 고객사 폴더
            String customerCode = version.getCustomer() != null
                    ? version.getCustomer().getCustomerCode()
                    : "unknown";
            relativePath = String.format("versions/custom/%s/patch_note.md", customerCode);
        }

        return Paths.get(baseReleasePath, relativePath);
    }

    /**
     * 버전 정보 포맷팅
     *
     * <pre>
     * =========================================================
     * VERSION: 1.1.1
     * CREATED_AT: 2025-11-05
     * CREATED_BY: jhlee@tscientific
     * CUSTOM_VERSION: (커스텀 버전인 경우만)
     * COMMENT: 기능 설명
     * =========================================================
     * </pre>
     */
    private String formatVersionEntry(ReleaseVersion version) {
        StringBuilder entry = new StringBuilder();

        entry.append(SEPARATOR_LINE).append("\n");
        entry.append("VERSION: ").append(version.getVersion()).append("\n");
        entry.append("CREATED_AT: ").append(
                version.getCreatedAt().format(DATE_FORMATTER)).append("\n");
        entry.append("CREATED_BY: ").append(version.getCreatedBy()).append("\n");

        // CUSTOM_VERSION (있는 경우만)
        if (version.getCustomVersion() != null && !version.getCustomVersion().isEmpty()) {
            entry.append("CUSTOM_VERSION: ").append(version.getCustomVersion()).append("\n");
        }

        // COMMENT
        String comment = version.getComment() != null ? version.getComment() : "";
        entry.append("COMMENT: ").append(comment).append("\n");

//        entry.append(SEPARATOR_LINE);

        return entry.toString();
    }

    /**
     * patch_note.md에서 특정 버전 엔트리 제거 (롤백용)
     *
     * @param releaseType 릴리즈 타입 (STANDARD/CUSTOM)
     * @param version     버전 번호
     */
    public void removeVersionEntry(String releaseType, String version) {
        try {
            // patch_note.md 경로 결정
            Path patchNotePath;
            if ("STANDARD".equals(releaseType)) {
                patchNotePath = Paths.get(baseReleasePath, "versions/standard/patch_note.md");
            } else {
                // CUSTOM의 경우 customerCode가 필요하지만, 롤백 시점에는 알 수 없으므로 스킵
                log.warn("Cannot remove custom version entry without customerCode");
                return;
            }

            if (!Files.exists(patchNotePath)) {
                log.warn("patch_note.md does not exist: {}", patchNotePath);
                return;
            }

            // 파일 읽기
            String content = Files.readString(patchNotePath);

            // 해당 버전의 엔트리 찾아서 제거
            String[] lines = content.split("\n");
            StringBuilder newContent = new StringBuilder();
            boolean skipEntry = false;
            int separatorCount = 0;

            for (String line : lines) {
                if (line.startsWith(SEPARATOR_LINE)) {
                    separatorCount++;
                    skipEntry = false;
                    continue;
                }

                if (line.startsWith("VERSION: " + version)) {
                    skipEntry = true;
                    continue;
                }

                if (!skipEntry) {
                    newContent.append(line).append("\n");
                }
            }

            // 파일 쓰기
            Files.writeString(patchNotePath, newContent.toString());
            log.info("Removed version {} from patch_note.md", version);

        } catch (IOException e) {
            log.error("Failed to remove version entry from patch_note.md: {}", version, e);
            // 롤백 작업이므로 예외를 던지지 않음
        }
    }

    /**
     * patch_note.md에서 특정 버전의 메타데이터 조회
     *
     * @param releaseType  릴리즈 타입 (STANDARD/CUSTOM)
     * @param customerCode 고객사 코드 (CUSTOM인 경우 필수)
     * @param version      버전 번호
     * @return 버전 메타데이터 (없으면 null)
     */
    public VersionMetadata getVersionMetadata(String releaseType, String customerCode, String version) {
        try {
            // patch_note.md 경로 결정
            Path patchNotePath;
            if ("STANDARD".equals(releaseType)) {
                patchNotePath = Paths.get(baseReleasePath, "versions/standard/patch_note.md");
            } else {
                if (customerCode == null) {
                    log.warn("customerCode is required for CUSTOM release type");
                    return null;
                }
                patchNotePath = Paths.get(baseReleasePath, "versions/custom", customerCode, "patch_note.md");
            }

            if (!Files.exists(patchNotePath)) {
                log.warn("patch_note.md does not exist: {}", patchNotePath);
                return null;
            }

            // 파일 읽기
            String content = Files.readString(patchNotePath);

            // 버전 엔트리 파싱
            return parseVersionEntry(content, version);

        } catch (IOException e) {
            log.error("Failed to get version metadata from patch_note.md: {}", version, e);
            return null;
        }
    }

    /**
     * patch_note.md에서 특정 버전 엔트리 파싱
     */
    private VersionMetadata parseVersionEntry(String content, String targetVersion) {
        String[] lines = content.split("\n");
        Map<String, String> metadata = new HashMap<>();
        boolean inTargetEntry = false;

        for (String line : lines) {
            // 구분선 만나면 엔트리 종료
            if (line.startsWith(SEPARATOR_LINE)) {
                if (inTargetEntry) {
                    // 타겟 버전 엔트리 완료
                    break;
                }
                continue;
            }

            // VERSION 확인
            if (line.startsWith("VERSION: ")) {
                String version = line.substring("VERSION: ".length()).trim();
                if (version.equals(targetVersion)) {
                    inTargetEntry = true;
                    metadata.put("version", version);
                } else {
                    inTargetEntry = false;
                    metadata.clear();
                }
                continue;
            }

            // 타겟 버전 엔트리 내의 메타데이터 수집
            if (inTargetEntry) {
                if (line.startsWith("CREATED_AT: ")) {
                    metadata.put("createdAt", line.substring("CREATED_AT: ".length()).trim());
                } else if (line.startsWith("CREATED_BY: ")) {
                    metadata.put("createdBy", line.substring("CREATED_BY: ".length()).trim());
                } else if (line.startsWith("COMMENT: ")) {
                    metadata.put("comment", line.substring("COMMENT: ".length()).trim());
                } else if (line.startsWith("CUSTOM_VERSION: ")) {
                    metadata.put("customVersion", line.substring("CUSTOM_VERSION: ".length()).trim());
                }
            }
        }

        // 메타데이터가 수집되었으면 VersionMetadata 생성
        if (!metadata.isEmpty() && metadata.containsKey("version")) {
            return new VersionMetadata(
                    metadata.get("version"),
                    metadata.get("createdAt"),
                    metadata.get("createdBy"),
                    metadata.get("comment"),
                    metadata.get("customVersion"),
                    false // isInstall 정보는 patch_note.md에 없으므로 false
            );
        }

        return null;
    }

    /**
     * 버전 메타데이터 (patch_note.md에서 읽어온 정보)
     */
    public record VersionMetadata(
            String version,
            String createdAt,
            String createdBy,
            String comment,
            String customVersion,
            Boolean isInstall
    ) {

    }
}
