package com.ts.rm.domain.job.service;

import com.ts.rm.domain.job.dto.BackupLogDto;
import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.entity.BackupFileLog;
import com.ts.rm.domain.job.repository.BackupFileLogRepository;
import com.ts.rm.domain.job.repository.BackupFileRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
    private final BackupFileLogRepository backupFileLogRepository;

    @Value("${app.release.base-path:/app/release_files}")
    private String releaseBasePath;

    /**
     * 백업 파일에 대한 관련 로그 파일 목록 조회
     *
     * <p>DB에서 backupFileId를 기준으로 해당 백업 파일의 백업 로그와 복원 로그를 모두 조회합니다.
     *
     * @param backupFileId 백업 파일 ID
     * @return 로그 파일 목록 응답
     */
    public BackupLogDto.LogListResponse getLogFiles(Long backupFileId) {
        BackupFile backupFile = getBackupFile(backupFileId);

        log.info("백업 로그 조회 - backupFileId: {}", backupFileId);

        // DB에서 로그 파일 정보 조회
        List<BackupFileLog> logEntities = backupFileLogRepository
                .findByBackupFile_BackupFileIdOrderByCreatedAtDesc(backupFileId);

        // Entity를 DTO로 변환
        List<BackupLogDto.LogFileInfo> logFiles = logEntities.stream()
                .map(this::toLogFileInfo)
                .toList();

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

        log.info("로그 파일 다운로드 - backupFileId: {}, logFileName: {}", backupFileId, logFileName);

        // 보안: 경로 순회 공격 방지
        validateLogFileName(logFileName);

        // DB에서 로그 파일 정보 조회하여 소유권 검증
        BackupFileLog logEntity = backupFileLogRepository
                .findByBackupFile_BackupFileIdOrderByCreatedAtDesc(backupFileId).stream()
                .filter(log -> log.getLogFileName().equals(logFileName))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "해당 백업 파일의 로그가 아니거나 존재하지 않습니다"));

        // 실제 파일 경로 구성 (job/{fileCategory}/logs/{backupFileName}/{logFileName})
        Path logFilePath = Paths.get(releaseBasePath, logEntity.getLogFilePath());

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
        validateLogFileName(logFileName);

        // DB에서 로그 파일 정보 조회
        BackupFileLog logEntity = backupFileLogRepository
                .findByBackupFile_BackupFileIdOrderByCreatedAtDesc(backupFileId).stream()
                .filter(log -> log.getLogFileName().equals(logFileName))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "해당 백업 파일의 로그가 아니거나 존재하지 않습니다"));

        Path logFilePath = Paths.get(releaseBasePath, logEntity.getLogFilePath());

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
     * BackupFileLog Entity를 LogFileInfo DTO로 변환
     */
    private BackupLogDto.LogFileInfo toLogFileInfo(BackupFileLog logEntity) {
        return new BackupLogDto.LogFileInfo(
                logEntity.getLogFileName(),
                logEntity.getLogType(),
                logEntity.getFileSize(),
                BackupLogDto.formatFileSize(logEntity.getFileSize()),
                logEntity.getCreatedAt()
        );
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
     * 백업 파일명에서 확장자 제거
     *
     * @param fileName 파일명
     * @return 확장자가 제거된 파일명
     */
    private static String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0) ? fileName.substring(0, lastDotIndex) : fileName;
    }

    /**
     * 로그 파일 경로 생성 헬퍼 메서드
     *
     * <p>새로운 경로 구조: job/{fileCategory}/logs/{backupFileName_확장자제거}/{logFileName}
     *
     * @param backupFile  백업 파일 정보
     * @param logFileName 로그 파일명
     * @return 로그 파일 상대 경로
     */
    public static String buildLogFilePath(BackupFile backupFile, String logFileName) {
        String baseFileName = removeFileExtension(backupFile.getFileName());
        // job/MARIADB/logs/103_CM_DB/1_backup_20250101_120500.log
        return String.format("job/%s/logs/%s/%s",
                backupFile.getFileCategory(),
                baseFileName,
                logFileName);
    }

    /**
     * 복원 순번 계산 (다음 복원 순번 반환)
     *
     * @param backupFileId 백업 파일 ID
     * @return 다음 복원 순번
     */
    public int getNextRestoreNumber(Long backupFileId) {
        int restoreCount = (int) backupFileLogRepository
                .findByBackupFile_BackupFileIdOrderByCreatedAtDesc(backupFileId)
                .stream()
                .filter(log -> LOG_TYPE_RESTORE.equals(log.getLogType()))
                .count();
        return restoreCount + 1;
    }

    /**
     * 로그 파일 생성 (DB에 메타데이터 저장)
     *
     * <p>백업/복원 작업 시 로그 파일을 생성하고 DB에 메타데이터를 저장합니다.
     *
     * @param backupFile  백업 파일 정보
     * @param logFileName 로그 파일명
     * @param logType     로그 타입 (BACKUP, RESTORE)
     * @param createdBy   생성자
     * @param fileSize    로그 파일 크기 (bytes)
     * @param checksum    로그 파일 체크섬 (SHA-256)
     * @return 생성된 로그 파일 엔티티
     */
    @Transactional
    public BackupFileLog createLogFile(BackupFile backupFile, String logFileName,
            String logType, String createdBy, Long fileSize, String checksum) {
        String logFilePath = buildLogFilePath(backupFile, logFileName);

        // 실제 파일 디렉토리 생성 (확장자 제거한 디렉토리명 사용)
        String baseFileName = removeFileExtension(backupFile.getFileName());
        Path logDir = Paths.get(releaseBasePath, "job", backupFile.getFileCategory(),
                "logs", baseFileName);
        try {
            Files.createDirectories(logDir);
        } catch (IOException e) {
            log.error("로그 디렉토리 생성 실패: {}", logDir, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "로그 디렉토리 생성에 실패했습니다");
        }

        // DB에 메타데이터 저장
        BackupFileLog logEntity = BackupFileLog.builder()
                .backupFile(backupFile)
                .logType(logType)
                .logFileName(logFileName)
                .logFilePath(logFilePath)
                .fileSize(fileSize)
                .checksum(checksum)
                .createdBy(createdBy)
                .build();

        BackupFileLog savedLog = backupFileLogRepository.save(logEntity);
        log.info("로그 파일 DB 저장 완료 - backupFileId: {}, logFileName: {}, fileSize: {}",
                backupFile.getBackupFileId(), logFileName, fileSize);

        return savedLog;
    }

    /**
     * 로그 파일 삭제 (DB 메타데이터 및 실제 파일 삭제)
     *
     * @param backupFileId 백업 파일 ID
     * @param logFileName  삭제할 로그 파일명
     */
    @Transactional
    public void deleteLogFile(Long backupFileId, String logFileName) {
        validateLogFileName(logFileName);

        // DB에서 로그 파일 정보 조회
        BackupFileLog logEntity = backupFileLogRepository
                .findByBackupFile_BackupFileIdOrderByCreatedAtDesc(backupFileId).stream()
                .filter(log -> log.getLogFileName().equals(logFileName))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "해당 백업 파일의 로그가 아니거나 존재하지 않습니다"));

        // 실제 파일 삭제
        Path logFilePath = Paths.get(releaseBasePath, logEntity.getLogFilePath());
        try {
            if (Files.exists(logFilePath)) {
                Files.delete(logFilePath);
                log.info("로그 파일 삭제 완료: {}", logFilePath);
            }
        } catch (IOException e) {
            log.warn("로그 파일 삭제 실패 (DB에서는 삭제 진행): {}", logFilePath, e);
        }

        // DB에서 메타데이터 삭제
        backupFileLogRepository.delete(logEntity);
        log.info("로그 파일 메타데이터 삭제 완료 - backupFileId: {}, logFileName: {}",
                backupFileId, logFileName);
    }
}
