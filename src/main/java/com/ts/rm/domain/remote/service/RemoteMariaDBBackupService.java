package com.ts.rm.domain.remote.service;

import com.ts.rm.domain.remote.dto.request.MariaDBBackupRequest;
import com.ts.rm.domain.remote.dto.response.BackupFileInfo;
import com.ts.rm.domain.remote.dto.response.BackupJobResponse;
import com.ts.rm.domain.remote.enums.BackupJobStatus;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * MariaDB 원격 백업 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteMariaDBBackupService {

    private static final String BACKUP_FILE_PREFIX = "backup_remote";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss");

    @Value("${app.release.base-path:/app/release_files}")
    private String releaseBasePath;

    private final BackupJobStatusManager jobStatusManager;

    /**
     * MariaDB 원격 백업 실행
     *
     * @param request 백업 요청 정보
     * @return 백업 작업 응답
     */
    public BackupJobResponse executeBackup(MariaDBBackupRequest request) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String jobId = "backup_" + timestamp;
        String backupFileName = String.format("%s_%s.sql", BACKUP_FILE_PREFIX, timestamp);
        String logFileName = String.format("backup_remote_mariadb_%s.log", timestamp);

        // 디렉토리 생성
        createDirectories();

        Path backupFilePath = Paths.get(releaseBasePath + "/remote/backup_files", backupFileName);
        Path logFilePath = Paths.get(releaseBasePath + "/remote/logs", logFileName);

        log.info("원격 MariaDB 백업 시작 - jobId: {}, host: {}, database: {}",
                jobId, request.getHost(), request.getDatabase());

        try {
            // 로그 파일 초기화
            initializeLogFile(logFilePath, request);

            // 연결 테스트
            testConnection(request, logFilePath);

            // 백업 실행
            executeMariaDBDump(request, backupFilePath, logFilePath);

            // 백업 파일 확인
            File backupFile = backupFilePath.toFile();
            if (!backupFile.exists() || backupFile.length() == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "백업 파일 생성에 실패했습니다.");
            }

            long fileSize = backupFile.length();
            log.info("백업 완료 - jobId: {}, fileSize: {} bytes", jobId, fileSize);

            // 성공 로그 기록
            appendToLogFile(logFilePath, "========================================");
            appendToLogFile(logFilePath, "백업 완료: " + backupFileName);
            appendToLogFile(logFilePath, "파일 크기: " + BackupFileInfo.formatFileSize(fileSize));
            appendToLogFile(logFilePath, "========================================");

            return BackupJobResponse.createSuccess(jobId, backupFileName, fileSize,
                    logFilePath.toString());

        } catch (Exception e) {
            log.error("백업 실패 - jobId: {}, error: {}", jobId, e.getMessage(), e);

            // 실패 로그 기록
            try {
                appendToLogFile(logFilePath, "========================================");
                appendToLogFile(logFilePath, "백업 실패: " + e.getMessage());
                appendToLogFile(logFilePath, "========================================");
            } catch (IOException logError) {
                log.error("로그 파일 기록 실패", logError);
            }

            // 실패 시 빈 백업 파일 삭제
            try {
                Files.deleteIfExists(backupFilePath);
            } catch (IOException deleteError) {
                log.error("백업 파일 삭제 실패", deleteError);
            }

            return BackupJobResponse.createFailed(jobId, backupFileName, logFilePath.toString(),
                    e.getMessage());
        }
    }

    /**
     * MariaDB 원격 백업 비동기 실행
     *
     * @param request 백업 요청 정보
     * @param jobId 작업 ID
     * @param backupFileName 백업 파일명
     * @param logFileName 로그 파일명
     */
    @Async("backupTaskExecutor")
    public void executeBackupAsync(MariaDBBackupRequest request, String jobId,
            String backupFileName, String logFileName) {

        Path backupFilePath = Paths.get(releaseBasePath + "/remote/backup_files", backupFileName);
        Path logFilePath = Paths.get(releaseBasePath + "/remote/logs", logFileName);

        log.info("비동기 백업 시작 - jobId: {}", jobId);

        try {
            // 디렉토리 생성
            createDirectories();

            // 로그 파일 초기화
            initializeLogFile(logFilePath, request);

            // 연결 테스트
            testConnection(request, logFilePath);

            // 백업 실행
            executeMariaDBDump(request, backupFilePath, logFilePath);

            // 백업 파일 확인
            File backupFile = backupFilePath.toFile();
            if (!backupFile.exists() || backupFile.length() == 0) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "백업 파일 생성에 실패했습니다.");
            }

            long fileSize = backupFile.length();
            log.info("비동기 백업 완료 - jobId: {}, fileSize: {} bytes", jobId, fileSize);

            // 성공 로그 기록
            appendToLogFile(logFilePath, "========================================");
            appendToLogFile(logFilePath, "백업 완료: " + backupFileName);
            appendToLogFile(logFilePath, "파일 크기: " + BackupFileInfo.formatFileSize(fileSize));
            appendToLogFile(logFilePath, "========================================");

            // 작업 상태 업데이트 (성공)
            jobStatusManager.saveJobStatus(jobId,
                    BackupJobResponse.createSuccess(jobId, backupFileName, fileSize,
                            logFilePath.toString()));

        } catch (Exception e) {
            log.error("비동기 백업 실패 - jobId: {}, error: {}", jobId, e.getMessage(), e);

            // 실패 로그 기록
            try {
                appendToLogFile(logFilePath, "========================================");
                appendToLogFile(logFilePath, "백업 실패: " + e.getMessage());
                appendToLogFile(logFilePath, "========================================");
            } catch (IOException logError) {
                log.error("로그 파일 기록 실패", logError);
            }

            // 실패 시 빈 백업 파일 삭제
            try {
                Files.deleteIfExists(backupFilePath);
            } catch (IOException deleteError) {
                log.error("백업 파일 삭제 실패", deleteError);
            }

            // 작업 상태 업데이트 (실패)
            jobStatusManager.saveJobStatus(jobId,
                    BackupJobResponse.createFailed(jobId, backupFileName, logFilePath.toString(),
                            e.getMessage()));
        }
    }

    /**
     * 백업 파일 목록 조회
     *
     * @return 백업 파일 목록
     */
    public List<BackupFileInfo> getBackupFileList() {
        Path backupDir = Paths.get(releaseBasePath + "/remote/backup_files");

        if (!Files.exists(backupDir)) {
            return new ArrayList<>();
        }

        try {
            return Files.list(backupDir)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .map(this::createBackupFileInfo)
                    .sorted(Comparator.comparing(BackupFileInfo::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("백업 파일 목록 조회 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "백업 파일 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 백업 파일 조회
     *
     * @param fileName 파일명
     * @return 백업 파일 Path
     */
    public Path getBackupFilePath(String fileName) {
        Path filePath = Paths.get(releaseBasePath + "/remote/backup_files", fileName);

        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "백업 파일을 찾을 수 없습니다: " + fileName);
        }

        return filePath;
    }

    /**
     * 백업 파일 삭제
     *
     * @param fileName 파일명
     */
    public void deleteBackupFile(String fileName) {
        Path filePath = Paths.get(releaseBasePath + "/remote/backup_files", fileName);

        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "백업 파일을 찾을 수 없습니다: " + fileName);
        }

        try {
            Files.delete(filePath);
            log.info("백업 파일 삭제 완료: {}", fileName);
        } catch (IOException e) {
            log.error("백업 파일 삭제 실패: {}", fileName, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "백업 파일 삭제에 실패했습니다.");
        }
    }

    /**
     * 디렉토리 생성
     */
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(releaseBasePath + "/remote/backup_files"));
            Files.createDirectories(Paths.get(releaseBasePath + "/remote/logs"));
        } catch (IOException e) {
            log.error("디렉토리 생성 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패했습니다.");
        }
    }

    /**
     * 로그 파일 초기화
     */
    private void initializeLogFile(Path logFilePath, MariaDBBackupRequest request)
            throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath.toFile()))) {
            writer.write("=========================================\n");
            writer.write("MariaDB 원격 백업\n");
            writer.write("=========================================\n");
            writer.write("호스트: " + request.getHost() + ":" + request.getPort() + "\n");
            writer.write("데이터베이스: " + request.getDatabase() + "\n");
            writer.write("시작 시간: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("=========================================\n\n");
        }
    }

    /**
     * 로그 파일에 추가
     */
    private void appendToLogFile(Path logFilePath, String message) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(logFilePath.toFile(), true))) {
            writer.write(message + "\n");
        }
    }

    /**
     * MariaDB 연결 테스트
     */
    private void testConnection(MariaDBBackupRequest request, Path logFilePath) throws IOException {
        appendToLogFile(logFilePath, "연결 테스트 중...");

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("exec");
        command.add(request.getContainerName());
        command.add("mariadb");
        command.add("-h" + request.getHost());
        command.add("-P" + request.getPort());
        command.add("-u" + request.getUsername());
        command.add("-p" + request.getPassword());
        command.add("-e");
        command.add("SELECT 1");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String error = readProcessOutput(process);
                appendToLogFile(logFilePath, "연결 실패: " + error);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "MariaDB 연결에 실패했습니다: " + error);
            }

            appendToLogFile(logFilePath, "연결 성공!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "연결 테스트가 중단되었습니다.");
        }
    }

    /**
     * mariadb-dump 명령 실행
     */
    private void executeMariaDBDump(MariaDBBackupRequest request, Path backupFilePath,
            Path logFilePath) throws IOException {
        appendToLogFile(logFilePath, "백업 실행 중...");

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("exec");
        command.add(request.getContainerName());
        command.add("mariadb-dump");
        command.add("-h" + request.getHost());
        command.add("-P" + request.getPort());
        command.add("-u" + request.getUsername());
        command.add("-p" + request.getPassword());
        command.add("--single-transaction");
        command.add("--routines");
        command.add("--triggers");
        command.add("--events");
        command.add("--hex-blob");
        command.add("--add-drop-database");
        command.add("--databases");
        command.add(request.getDatabase());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(backupFilePath.toFile());
        pb.redirectError(ProcessBuilder.Redirect.PIPE);

        try {
            Process process = pb.start();

            // 에러 출력 읽기
            String errorOutput = readProcessOutput(process);
            if (!errorOutput.isEmpty()) {
                appendToLogFile(logFilePath, "경고/오류: " + errorOutput);
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                appendToLogFile(logFilePath, "백업 실패 (종료 코드: " + exitCode + ")");
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "백업 실행에 실패했습니다 (종료 코드: " + exitCode + ")");
            }

            appendToLogFile(logFilePath, "백업 파일 생성 완료");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "백업 작업이 중단되었습니다.");
        }
    }

    /**
     * 프로세스 출력 읽기
     */
    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }

    /**
     * BackupFileInfo 생성
     */
    private BackupFileInfo createBackupFileInfo(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            long fileSize = attrs.size();
            LocalDateTime createdAt = LocalDateTime.ofInstant(
                    attrs.creationTime().toInstant(), ZoneId.systemDefault());

            return BackupFileInfo.builder()
                    .fileName(path.getFileName().toString())
                    .fileSizeBytes(fileSize)
                    .fileSizeFormatted(BackupFileInfo.formatFileSize(fileSize))
                    .createdAt(createdAt)
                    .build();

        } catch (IOException e) {
            log.error("파일 정보 조회 실패: {}", path, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 정보 조회에 실패했습니다.");
        }
    }
}
