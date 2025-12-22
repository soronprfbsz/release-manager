package com.ts.rm.domain.job.filesync;

import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.repository.BackupFileRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.domain.filesync.adapter.FileSyncAdapter;
import com.ts.rm.domain.filesync.dto.FileSyncMetadata;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 백업 파일 동기화 어댑터
 *
 * <p>BackupFile 도메인의 파일 동기화를 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupFileSyncAdapter implements FileSyncAdapter {

    private final BackupFileRepository backupFileRepository;

    @Override
    public FileSyncTarget getTarget() {
        return FileSyncTarget.BACKUP_FILE;
    }

    @Override
    public String getBaseScanPath() {
        return "job";
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileSyncMetadata> getRegisteredFiles(@Nullable String subPath) {
        List<BackupFile> files;

        if (subPath != null && !subPath.isEmpty()) {
            // filePath가 subPath로 시작하는 파일들 조회
            files = backupFileRepository.findAll().stream()
                    .filter(f -> f.getFilePath() != null && f.getFilePath().startsWith(subPath))
                    .toList();
        } else {
            files = backupFileRepository.findAllByOrderByCreatedAtDesc();
        }

        return files.stream()
                .map(this::toMetadata)
                .toList();
    }

    @Override
    @Transactional
    public Long registerFile(FileSyncMetadata metadata, @Nullable Map<String, Object> additionalData) {
        // 파일 확장자에서 fileType 추출
        String fileType = extractFileType(metadata.getFileName());

        // 경로에서 카테고리 추출 (job/{category}/backup_files/...)
        String[] pathParts = metadata.getFilePath().split("/");
        String fileCategory = pathParts.length > 1 ? pathParts[1].toUpperCase() : "ETC";

        // additionalData에서 오버라이드 가능
        fileCategory = extractStringOrDefault(additionalData, "fileCategory", fileCategory);
        String description = extractString(additionalData, "description");
        String createdBy = extractStringOrDefault(additionalData, "createdBy", "SYSTEM_SYNC");

        BackupFile backupFile = BackupFile.builder()
                .fileCategory(fileCategory)
                .fileType(fileType)
                .fileName(metadata.getFileName())
                .filePath(metadata.getFilePath())
                .fileSize(metadata.getFileSize())
                .checksum(metadata.getChecksum())
                .description(description)
                .createdBy(createdBy)
                .build();

        BackupFile saved = backupFileRepository.save(backupFile);
        log.info("백업 파일 동기화 등록: {} (ID: {})", metadata.getFilePath(), saved.getBackupFileId());

        return saved.getBackupFileId();
    }

    @Override
    @Transactional
    public void updateMetadata(Long id, FileSyncMetadata newMetadata) {
        BackupFile backupFile = backupFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND,
                        "백업 파일을 찾을 수 없습니다: " + id));

        backupFile.setFileSize(newMetadata.getFileSize());
        backupFile.setChecksum(newMetadata.getChecksum());

        backupFileRepository.save(backupFile);
        log.info("백업 파일 메타데이터 갱신: {} (ID: {})", newMetadata.getFilePath(), id);
    }

    @Override
    @Transactional
    public void deleteMetadata(Long id) {
        if (!backupFileRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "백업 파일을 찾을 수 없습니다: " + id);
        }

        backupFileRepository.deleteById(id);
        log.info("백업 파일 메타데이터 삭제: ID {}", id);
    }

    @Override
    public List<String> getAllowedExtensions() {
        return List.of(".sql", ".gz", ".tar", ".tar.gz", ".zip");
    }

    @Override
    public List<String> getExcludedDirectories() {
        return List.of("logs");
    }

    /**
     * BackupFile 엔티티를 FileSyncMetadata로 변환
     */
    private FileSyncMetadata toMetadata(BackupFile file) {
        return FileSyncMetadata.builder()
                .id(file.getBackupFileId())
                .filePath(file.getFilePath())
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .checksum(file.getChecksum())
                .registeredAt(file.getCreatedAt())
                .target(FileSyncTarget.BACKUP_FILE)
                .build();
    }

    /**
     * 파일명에서 확장자 추출 (대문자)
     */
    private String extractFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "UNKNOWN";
        }
        int lastDot = fileName.lastIndexOf(".");
        return fileName.substring(lastDot + 1).toUpperCase();
    }

    private String extractString(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    private String extractStringOrDefault(Map<String, Object> data, String key, String defaultValue) {
        String value = extractString(data, key);
        return value != null ? value : defaultValue;
    }
}
