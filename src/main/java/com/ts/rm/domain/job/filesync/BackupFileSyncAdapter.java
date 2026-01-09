package com.ts.rm.domain.job.filesync;

import static com.ts.rm.global.util.FileTypeExtractor.extractFileType;
import static com.ts.rm.global.util.MapExtractUtil.extractString;
import static com.ts.rm.global.util.MapExtractUtil.extractStringOrDefault;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.repository.BackupFileRepository;
import com.ts.rm.global.account.AccountLookupService;
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
    private final AccountLookupService accountLookupService;

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
        String createdByEmail = extractStringOrDefault(additionalData, "createdByEmail", "SYSTEM_SYNC");

        // 생성자 Account 조회
        Account creator = accountLookupService.findByEmail(createdByEmail);

        BackupFile backupFile = BackupFile.builder()
                .fileCategory(fileCategory)
                .fileType(fileType)
                .fileName(metadata.getFileName())
                .filePath(metadata.getFilePath())
                .fileSize(metadata.getFileSize())
                .checksum(metadata.getChecksum())
                .description(description)
                .creator(creator)
                .createdByEmail(createdByEmail)
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
     * 유효한 동기화 경로인지 확인
     *
     * <p>백업 파일은 job/{category}/backup_files/{fileName} 형식이어야 합니다.
     * 올바른 경로가 아니면 DB 메타데이터를 생성할 수 없으므로 동기화 대상에서 제외합니다.
     *
     * @param filePath 파일 경로
     * @return true면 동기화 대상, false면 무시
     */
    @Override
    public boolean isValidSyncPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        // 경로 형식: job/{category}/backup_files/{fileName}
        // 최소 4개의 경로 부분이 필요: [job, category, backup_files, fileName]
        String[] pathParts = filePath.split("/");
        if (pathParts.length < 4) {
            log.debug("백업 파일 경로 형식 불일치 (최소 4단계 필요): {}", filePath);
            return false;
        }

        // 첫 번째 부분이 "job"인지 확인
        if (!"job".equalsIgnoreCase(pathParts[0])) {
            log.debug("백업 파일 경로는 'job/'으로 시작해야 합니다: {}", filePath);
            return false;
        }

        // category가 비어있지 않은지 확인
        String category = pathParts[1];
        if (category.isBlank()) {
            log.debug("백업 파일 경로의 category가 비어있습니다: {}", filePath);
            return false;
        }

        // 세 번째 부분이 "backup_files"인지 확인
        if (!"backup_files".equalsIgnoreCase(pathParts[2])) {
            log.debug("백업 파일 경로는 'job/{category}/backup_files/' 형식이어야 합니다: {}", filePath);
            return false;
        }

        return true;
    }
}
