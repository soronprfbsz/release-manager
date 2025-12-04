package com.ts.rm.domain.job.service;

import com.ts.rm.domain.job.dto.BackupLogDto;
import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.repository.BackupFileRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 백업 로그 서비스
 *
 * <p>백업 파일과 관련된 로그 파일 조회 및 다운로드 서비스
 *
 * <p>로그 파일 명명 규칙:
 * <ul>
 *   <li>백업 로그: backup_{backupFileId}_{timestamp}.log</li>
 *   <li>복원 로그: restore_{backupFileId}_{timestamp}.log</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackupLogService {

    private static final String LOG_TYPE_BACKUP = "BACKUP";
    private static final String LOG_TYPE_RESTORE = "RESTORE";

    private final BackupFileRepository backupFileRepository;

    @Value("${app.release.base-path:/app/release_files}")
    private String releaseBasePath;

    /**
     * 백업 파일에 대한 관련 로그 파일 목록 조회
     *
     * <p>backupFileId를 기준으로 해당 백업 파일의 백업 로그와 복원 로그를 모두 조회합니다.
     *
     * @param backupFileId 백업 파일 ID
     * @return 로그 파일 목록 응답
     */
    public BackupLogDto.LogListResponse getLogFiles(Long backupFileId) {
        BackupFile backupFile = getBackupFile(backupFileId);

        log.info("백업 로그 조회 - backupFileId: {}", backupFileId);

        List<BackupLogDto.LogFileInfo> logFiles = new ArrayList<>();
        Path logDir = Paths.get(releaseBasePath, "job/logs", backupFile.getFileCategory());

        if (!Files.exists(logDir)) {
            log.warn("로그 디렉토리 존재하지 않음: {}", logDir);
            return new BackupLogDto.LogListResponse(
                    backupFileId,
                    backupFile.getFileName(),
                    List.of()
            );
        }

        // 백업 로그 파일 조회 (backup_{backupFileId}_{timestamp}.log)
        String backupLogPrefix = String.format("backup_%d_", backupFileId);
        logFiles.addAll(findLogsByPrefix(logDir, backupLogPrefix, LOG_TYPE_BACKUP));

        // 복원 로그 파일 조회 (restore_{backupFileId}_{timestamp}.log)
        String restoreLogPrefix = String.format("restore_%d_", backupFileId);
        logFiles.addAll(findLogsByPrefix(logDir, restoreLogPrefix, LOG_TYPE_RESTORE));

        // 수정일시 기준 내림차순 정렬
        logFiles.sort(Comparator.comparing(BackupLogDto.LogFileInfo::lastModified,
                Comparator.nullsLast(Comparator.reverseOrder())));

        log.info("백업 로그 조회 완료 - backupFileId: {}, 로그 파일 수: {}",
                backupFileId, logFiles.size());

        return new BackupLogDto.LogListResponse(
                backupFileId,
                backupFile.getFileName(),
                logFiles
        );
    }

    /**
     * 로그 파일 다운로드 (스트리밍)
     *
     * @param backupFileId 백업 파일 ID
     * @param logFileName  다운로드할 로그 파일명
     * @param outputStream 출력 스트림
     */
    public void downloadLogFile(Long backupFileId, String logFileName, OutputStream outputStream) {
        BackupFile backupFile = getBackupFile(backupFileId);
        Path logFilePath = Paths.get(releaseBasePath, "job/logs",
                backupFile.getFileCategory(), logFileName);

        log.info("로그 파일 다운로드 - backupFileId: {}, logFileName: {}", backupFileId, logFileName);

        // 보안: 경로 순회 공격 방지
        validateLogFileName(logFileName);

        // 보안: 요청한 backupFileId와 로그 파일이 일치하는지 검증
        validateLogFileOwnership(backupFileId, logFileName);

        if (!Files.exists(logFilePath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "로그 파일을 찾을 수 없습니다: " + logFileName);
        }

        try (InputStream is = Files.newInputStream(logFilePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            log.error("로그 파일 다운로드 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "로그 파일 다운로드에 실패했습니다");
        }
    }

    /**
     * 로그 파일 크기 조회
     *
     * @param backupFileId 백업 파일 ID
     * @param logFileName  로그 파일명
     * @return 파일 크기 (bytes)
     */
    public long getLogFileSize(Long backupFileId, String logFileName) {
        BackupFile backupFile = getBackupFile(backupFileId);
        Path logFilePath = Paths.get(releaseBasePath, "job/logs",
                backupFile.getFileCategory(), logFileName);

        validateLogFileName(logFileName);
        validateLogFileOwnership(backupFileId, logFileName);

        if (!Files.exists(logFilePath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "로그 파일을 찾을 수 없습니다: " + logFileName);
        }

        try {
            return Files.size(logFilePath);
        } catch (IOException e) {
            log.error("로그 파일 크기 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "로그 파일 정보 조회에 실패했습니다");
        }
    }

    /**
     * 백업 파일 조회
     */
    private BackupFile getBackupFile(Long backupFileId) {
        return backupFileRepository.findById(backupFileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "백업 파일을 찾을 수 없습니다: " + backupFileId));
    }

    /**
     * 특정 prefix로 시작하는 로그 파일 목록 조회
     *
     * @param logDir  로그 디렉토리
     * @param prefix  파일명 prefix (예: "backup_1_", "restore_1_")
     * @param logType 로그 타입 (BACKUP, RESTORE)
     * @return 로그 파일 정보 목록
     */
    private List<BackupLogDto.LogFileInfo> findLogsByPrefix(Path logDir, String prefix, String logType) {
        List<BackupLogDto.LogFileInfo> logFiles = new ArrayList<>();

        try (Stream<Path> files = Files.list(logDir)) {
            files.filter(path -> path.getFileName().toString().startsWith(prefix))
                    .filter(path -> path.getFileName().toString().endsWith(".log"))
                    .forEach(path -> logFiles.add(createLogFileInfo(path, logType)));
        } catch (IOException e) {
            log.warn("로그 파일 검색 실패 - prefix: {}, error: {}", prefix, e.getMessage());
        }

        return logFiles;
    }

    /**
     * 로그 파일 정보 생성
     */
    private BackupLogDto.LogFileInfo createLogFileInfo(Path logPath, String logType) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(logPath, BasicFileAttributes.class);
            long fileSize = attrs.size();
            LocalDateTime lastModified = LocalDateTime.ofInstant(
                    attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());

            return new BackupLogDto.LogFileInfo(
                    logPath.getFileName().toString(),
                    logType,
                    fileSize,
                    BackupLogDto.formatFileSize(fileSize),
                    lastModified
            );
        } catch (IOException e) {
            log.warn("로그 파일 정보 조회 실패: {}", logPath, e);
            return new BackupLogDto.LogFileInfo(
                    logPath.getFileName().toString(),
                    logType,
                    null,
                    "-",
                    null
            );
        }
    }

    /**
     * 로그 파일명 유효성 검증 (경로 순회 공격 방지)
     */
    private void validateLogFileName(String logFileName) {
        if (logFileName == null || logFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "로그 파일명이 비어있습니다");
        }

        // 경로 순회 문자 검증
        if (logFileName.contains("..") || logFileName.contains("/") || logFileName.contains("\\")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 로그 파일명입니다");
        }

        // 허용된 파일 확장자 검증
        if (!logFileName.endsWith(".log")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "로그 파일만 다운로드 가능합니다");
        }
    }

    /**
     * 로그 파일 소유권 검증
     *
     * <p>요청한 backupFileId와 로그 파일명의 ID가 일치하는지 검증합니다.
     * 다른 백업 파일의 로그에 접근하는 것을 방지합니다.
     *
     * @param backupFileId 요청한 백업 파일 ID
     * @param logFileName  로그 파일명
     */
    private void validateLogFileOwnership(Long backupFileId, String logFileName) {
        // 로그 파일명 패턴: (backup|restore)_{backupFileId}_{timestamp}.log
        String expectedBackupPrefix = String.format("backup_%d_", backupFileId);
        String expectedRestorePrefix = String.format("restore_%d_", backupFileId);

        if (!logFileName.startsWith(expectedBackupPrefix)
                && !logFileName.startsWith(expectedRestorePrefix)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "해당 백업 파일의 로그가 아닙니다");
        }
    }
}
