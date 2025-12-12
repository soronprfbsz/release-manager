package com.ts.rm.domain.job.service;

import com.ts.rm.domain.job.dto.MariaDBRestoreRequest;
import com.ts.rm.domain.job.dto.JobResponse;
import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.repository.BackupFileRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
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

/**
 * MariaDB 복원 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MariaDBRestoreService {

    private static final String FILE_CATEGORY = "MARIADB";
    private static final String LOG_TYPE_RESTORE = "RESTORE";

    @Value("${app.release.base-path:/app/release_files}")
    private String releaseBasePath;

    private final JobStatusManager jobStatusManager;
    private final BackupFileRepository backupFileRepository;
    private final BackupLogService backupLogService;

    /**
     * MariaDB 복원 비동기 실행
     *
     * @param request     복원 요청 정보
     * @param jobId       작업 ID
     * @param logFileName 로그 파일명
     */
    @Async("backupTaskExecutor")
    public void executeRestoreAsync(MariaDBRestoreRequest request, String jobId,
            String logFileName) {

        // 백업 파일 정보 조회
        BackupFile backupFile = backupFileRepository.findById(request.getBackupFileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "백업 파일을 찾을 수 없습니다: " + request.getBackupFileId()));

        // 복원 순번 계산
        String timestamp = jobId.replace("restore_", "");
        int restoreNumber = backupLogService.getNextRestoreNumber(request.getBackupFileId());
        String newLogFileName = String.format("%d_restore_%s.log", restoreNumber, timestamp);

        Path backupFilePath = Paths.get(releaseBasePath, backupFile.getFilePath());

        // 백업 파일명에서 확장자 제거하여 로그 디렉토리명 생성
        String baseFileName = removeFileExtension(backupFile.getFileName());
        Path logDir = Paths.get(releaseBasePath, "job", FILE_CATEGORY, "logs", baseFileName);
        Path logFilePath = logDir.resolve(newLogFileName);

        log.info("복원 시작 - jobId: {}, backupFileId: {}, restoreNumber: {}",
                jobId, request.getBackupFileId(), restoreNumber);

        try {
            // 디렉토리 생성
            createDirectories(backupFile.getFileName());

            // 백업 파일 존재 확인
            if (!Files.exists(backupFilePath)) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "백업 파일을 찾을 수 없습니다: " + backupFile.getFileName());
            }

            // 로그 파일 초기화
            initializeLogFile(logFilePath, request, backupFile, backupFilePath);

            // 연결 테스트
            testConnection(request, logFilePath);

            // 복원 실행
            executeMariaDBRestore(request, backupFilePath, logFilePath);

            log.info("복원 완료 - jobId: {}, backupFileId: {}", jobId, request.getBackupFileId());

            // 성공 로그 기록
            appendToLogFile(logFilePath, "========================================");
            appendToLogFile(logFilePath, "복원 완료: " + backupFile.getFileName());
            appendToLogFile(logFilePath, "========================================");

            // 로그 파일 크기 및 체크섬 계산
            long logFileSize = Files.size(logFilePath);
            String logChecksum = com.ts.rm.global.file.FileChecksumUtil.calculateChecksum(logFilePath);

            // BackupFileLog 테이블에 로그 파일 정보 저장
            backupLogService.createLogFile(backupFile, newLogFileName, LOG_TYPE_RESTORE,
                    request.getUsername(), logFileSize, logChecksum);
            log.info("복원 로그 DB 저장 완료 - logFileName: {}", newLogFileName);

            // 작업 상태 업데이트 (성공)
            jobStatusManager.saveJobStatus(jobId,
                    JobResponse.createSuccess(jobId, backupFile.getFileName(),
                            Files.size(backupFilePath), "logs/" + baseFileName + "/" + newLogFileName));

        } catch (Exception e) {
            log.error("복원 실패 - jobId: {}, backupFileId: {}, error: {}",
                    jobId, request.getBackupFileId(), e.getMessage(), e);

            // 실패 로그 기록
            try {
                appendToLogFile(logFilePath, "========================================");
                appendToLogFile(logFilePath, "복원 실패: " + e.getMessage());
                appendToLogFile(logFilePath, "========================================");
            } catch (IOException logError) {
                log.error("로그 파일 기록 실패", logError);
            }

            // 작업 상태 업데이트 (실패)
            jobStatusManager.saveJobStatus(jobId,
                    JobResponse.createFailed(jobId, backupFile.getFileName(),
                            "logs/" + baseFileName + "/" + newLogFileName, e.getMessage()));
        }
    }

    /**
     * 디렉토리 생성
     *
     * @param backupFileName 백업 파일명
     */
    private void createDirectories(String backupFileName) {
        String baseFileName = removeFileExtension(backupFileName);
        try {
            Files.createDirectories(Paths.get(releaseBasePath, "job", FILE_CATEGORY, "logs", baseFileName));
        } catch (IOException e) {
            log.error("디렉토리 생성 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "디렉토리 생성에 실패했습니다.");
        }
    }

    /**
     * 백업 파일명에서 확장자 제거
     *
     * @param fileName 파일명
     * @return 확장자가 제거된 파일명
     */
    private String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0) ? fileName.substring(0, lastDotIndex) : fileName;
    }

    /**
     * 로그 파일 초기화
     */
    private void initializeLogFile(Path logFilePath, MariaDBRestoreRequest request,
            BackupFile backupFile, Path backupFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath.toFile()))) {
            writer.write("=========================================\n");
            writer.write("MariaDB 복원\n");
            writer.write("=========================================\n");
            writer.write("호스트: " + request.getHost() + ":" + request.getPort() + "\n");
            writer.write("백업 파일 ID: " + backupFile.getBackupFileId() + "\n");
            writer.write("백업 파일: " + backupFile.getFileName() + "\n");
            writer.write("파일 크기: " + Files.size(backupFilePath) + " bytes\n");
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
    private void testConnection(MariaDBRestoreRequest request, Path logFilePath)
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
     * mariadb 명령 실행 (복원)
     */
    private void executeMariaDBRestore(MariaDBRestoreRequest request, Path backupFilePath,
            Path logFilePath) throws IOException {
        appendToLogFile(logFilePath, "복원 실행 중...");
        appendToLogFile(logFilePath, "경고: 기존 데이터베이스의 데이터가 삭제되고 백업 데이터로 대체됩니다.");

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

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectInput(backupFilePath.toFile());
        pb.redirectError(ProcessBuilder.Redirect.PIPE);
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);

        try {
            Process process = pb.start();

            // 출력 읽기
            String output = readProcessOutput(process);
            if (!output.isEmpty()) {
                appendToLogFile(logFilePath, "출력: " + output);
            }

            // 에러 출력 읽기
            String errorOutput = readProcessErrorOutput(process);
            if (!errorOutput.isEmpty()) {
                appendToLogFile(logFilePath, "경고/오류: " + errorOutput);
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                appendToLogFile(logFilePath, "복원 실패 (종료 코드: " + exitCode + ")");
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "복원 실행에 실패했습니다 (종료 코드: " + exitCode + ")");
            }

            appendToLogFile(logFilePath, "복원 완료");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "복원 작업이 중단되었습니다.");
        }
    }

    /**
     * 프로세스 출력 읽기 (표준 출력)
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
     * 프로세스 출력 읽기 (표준 에러)
     */
    private String readProcessErrorOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }
}
