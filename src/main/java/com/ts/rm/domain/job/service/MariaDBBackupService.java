package com.ts.rm.domain.job.service;

import com.ts.rm.domain.job.dto.MariaDBBackupRequest;
import com.ts.rm.domain.job.dto.JobResponse;
import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.repository.BackupFileRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.file.FileChecksumUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MariaDB 백업 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MariaDBBackupService {

    private static final String FILE_CATEGORY = "MARIADB";
    private static final String FILE_TYPE = "SQL";

    @Value("${app.release.base-path:/app/release_files}")
    private String releaseBasePath;

    private final JobStatusManager jobStatusManager;
    private final BackupFileRepository backupFileRepository;

    /**
     * MariaDB 백업 비동기 실행
     *
     * @param request     백업 요청 정보
     * @param createdBy   생성자
     * @param jobId       작업 ID
     * @param logFileName 로그 파일명
     */
    @Async("backupTaskExecutor")
    @Transactional
    public void executeBackupAsync(MariaDBBackupRequest request, String createdBy,
            String jobId, String logFileName) {

        String timestamp = jobId.replace("backup_", "");
        String backupFileName = String.format("backup_%s_%s.sql", request.getDatabase(), timestamp);

        String relativePath = "job/backup_files/" + FILE_CATEGORY + "/" + backupFileName;
        Path backupFilePath = Paths.get(releaseBasePath, relativePath);
        Path logFilePath = Paths.get(releaseBasePath + "/job/logs/" + FILE_CATEGORY, logFileName);

        log.info("백업 시작 - jobId: {}", jobId);

        try {
            // 디렉토리 생성
            createDirectories();

            // 로그 파일 초기화
            initializeLogFile(logFilePath, request);

            // 연결 테스트
            testConnection(request, logFilePath);

            // 백업 실행
            executeMariaDBBackup(request, backupFilePath, logFilePath);

            // 파일 크기 및 체크섬 계산
            long fileSize = Files.size(backupFilePath);
            String checksum = FileChecksumUtil.calculateChecksum(backupFilePath);

            // DB에 백업 파일 정보 저장
            BackupFile backupFile = BackupFile.builder()
                    .fileCategory(FILE_CATEGORY)
                    .fileType(FILE_TYPE)
                    .fileName(backupFileName)
                    .filePath(relativePath)
                    .fileSize(fileSize)
                    .checksum(checksum)
                    .description(request.getDescription())
                    .createdBy(createdBy)
                    .build();

            backupFileRepository.save(backupFile);

            log.info("백업 완료 - jobId: {}, backupFileId: {}", jobId, backupFile.getBackupFileId());

            // 성공 로그 기록
            appendToLogFile(logFilePath, "========================================");
            appendToLogFile(logFilePath, "백업 파일 ID: " + backupFile.getBackupFileId());
            appendToLogFile(logFilePath, "백업 완료: " + backupFileName);
            appendToLogFile(logFilePath, "파일 크기: " + fileSize + " bytes");
            appendToLogFile(logFilePath, "========================================");

            // 로그 파일명에 backupFileId 포함하여 rename
            String newLogFileName = String.format("backup_%d_%s.log",
                    backupFile.getBackupFileId(), timestamp);
            Path newLogFilePath = logFilePath.getParent().resolve(newLogFileName);
            Files.move(logFilePath, newLogFilePath);
            log.info("로그 파일 rename: {} -> {}", logFileName, newLogFileName);

            // 작업 상태 업데이트 (성공) - 새 로그 파일명 반영
            jobStatusManager.saveJobStatus(jobId,
                    JobResponse.createSuccess(jobId, backupFileName, fileSize,
                            "logs/" + newLogFileName));

        } catch (Exception e) {
            log.error("백업 실패 - jobId: {}, error: {}", jobId, e.getMessage(), e);

            // 실패 시 생성된 파일 삭제
            deleteFileIfExists(backupFilePath);

            // 실패 로그 기록
            try {
                appendToLogFile(logFilePath, "========================================");
                appendToLogFile(logFilePath, "백업 실패: " + e.getMessage());
                appendToLogFile(logFilePath, "========================================");
            } catch (IOException logError) {
                log.error("로그 파일 기록 실패", logError);
            }

            // 작업 상태 업데이트 (실패)
            jobStatusManager.saveJobStatus(jobId,
                    JobResponse.createFailed(jobId, backupFileName,
                            "logs/" + logFileName, e.getMessage()));
        }
    }

    /**
     * 디렉토리 생성
     */
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(releaseBasePath + "/job/logs/" + FILE_CATEGORY));
            Files.createDirectories(Paths.get(releaseBasePath + "/job/backup_files/" + FILE_CATEGORY));
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
            writer.write("MariaDB 백업\n");
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
    private void testConnection(MariaDBBackupRequest request, Path logFilePath)
            throws IOException {
        appendToLogFile(logFilePath, "연결 테스트 중...");

        List<String> command = new ArrayList<>();
        command.add("mariadb");
        command.add("-h");
        command.add(request.getHost());
        command.add("-P");
        command.add(String.valueOf(request.getPort()));
        command.add("-u");
        command.add(request.getUsername());
        command.add("-p" + request.getPassword());
        command.add("--ssl=false");
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
     * mariadb-dump 명령 실행 (백업)
     * <p>
     * 백업 완료 후 하위 버전 호환성을 위해 NOTE_VERBOSITY 등 상위 버전 전용 변수를 제거
     */
    private void executeMariaDBBackup(MariaDBBackupRequest request, Path backupFilePath,
            Path logFilePath) throws IOException {
        appendToLogFile(logFilePath, "백업 실행 중...");
        appendToLogFile(logFilePath, "대상 데이터베이스: " + request.getDatabase());

        List<String> command = new ArrayList<>();
        command.add("mariadb-dump");
        command.add("-h");
        command.add(request.getHost());
        command.add("-P");
        command.add(String.valueOf(request.getPort()));
        command.add("-u");
        command.add(request.getUsername());
        command.add("-p" + request.getPassword());
        command.add("--ssl=false");
        command.add("--single-transaction");
        command.add("--routines");
        command.add("--triggers");
        command.add("--events");
        command.add("--skip-add-locks");
        // --databases 옵션: CREATE DATABASE IF NOT EXISTS, USE 구문 포함
        // 복원 시 데이터베이스 자동 생성 및 선택
        command.add("--databases");
        command.add(request.getDatabase());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(backupFilePath.toFile());
        pb.redirectError(ProcessBuilder.Redirect.PIPE);

        try {
            Process process = pb.start();

            // 에러 출력 읽기
            String errorOutput = readProcessOutput(process);
            if (!errorOutput.isEmpty() && !errorOutput.contains("Warning")) {
                appendToLogFile(logFilePath, "경고/오류: " + errorOutput);
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                appendToLogFile(logFilePath, "백업 실패 (종료 코드: " + exitCode + ")");
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "백업 실행에 실패했습니다 (종료 코드: " + exitCode + "): " + errorOutput);
            }

            appendToLogFile(logFilePath, "백업 파일 생성 완료: " + backupFilePath.getFileName());

            // 하위 버전 호환성을 위한 후처리
            removeIncompatibleStatements(backupFilePath, logFilePath);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "백업 작업이 중단되었습니다.");
        }
    }

    /**
     * 백업 파일에서 하위 버전과 호환되지 않는 구문 제거
     * <p>
     * MariaDB 10.6.16+ 에서 추가된 NOTE_VERBOSITY 등의 시스템 변수를
     * 하위 버전에서 복원 시 오류가 발생하므로 제거
     *
     * @param backupFilePath 백업 파일 경로
     * @param logFilePath    로그 파일 경로
     */
    private void removeIncompatibleStatements(Path backupFilePath, Path logFilePath)
            throws IOException {
        appendToLogFile(logFilePath, "하위 버전 호환성 처리 중...");

        List<String> lines = Files.readAllLines(backupFilePath);
        List<String> filteredLines = new ArrayList<>();
        int removedCount = 0;

        for (String line : lines) {
            // NOTE_VERBOSITY 관련 구문 제거 (MariaDB 10.6.16+ 전용)
            if (line.contains("NOTE_VERBOSITY")) {
                removedCount++;
                continue;
            }
            filteredLines.add(line);
        }

        if (removedCount > 0) {
            Files.write(backupFilePath, filteredLines);
            appendToLogFile(logFilePath,
                    "호환성 처리 완료: " + removedCount + "개 라인 제거 (NOTE_VERBOSITY)");
            log.info("백업 파일 호환성 처리 완료 - 제거된 라인: {}", removedCount);
        } else {
            appendToLogFile(logFilePath, "호환성 처리: 제거할 항목 없음");
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
        // 에러 스트림도 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }

    /**
     * 파일 삭제 (존재하는 경우)
     */
    private void deleteFileIfExists(Path filePath) {
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("실패한 백업 파일 삭제: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("백업 파일 삭제 실패: {}", filePath, e);
        }
    }
}
