package com.ts.rm.domain.release.util;

import com.ts.rm.domain.release.entity.ReleaseVersion;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
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
            relativePath = "releases/standard/patch_note.md";
        } else {
            // CUSTOM인 경우 고객사 폴더
            String customerCode = version.getCustomer() != null
                    ? version.getCustomer().getCustomerCode()
                    : "unknown";
            relativePath = String.format("releases/custom/%s/patch_note.md", customerCode);
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
                patchNotePath = Paths.get(baseReleasePath, "releases/standard/patch_note.md");
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
}
