package com.ts.rm.domain.remote.controller;

import com.ts.rm.domain.remote.dto.request.MariaDBBackupRequest;
import com.ts.rm.domain.remote.dto.request.MariaDBRestoreRequest;
import com.ts.rm.domain.remote.dto.response.BackupFileInfo;
import com.ts.rm.domain.remote.dto.response.BackupJobResponse;
import com.ts.rm.domain.remote.service.BackupJobStatusManager;
import com.ts.rm.domain.remote.service.RemoteMariaDBBackupService;
import com.ts.rm.domain.remote.service.RemoteMariaDBRestoreService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.response.ApiResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MariaDB 원격 백업/복원 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/remote")
@RequiredArgsConstructor
@Tag(name = "Remote MariaDB", description = "MariaDB 원격 백업/복원 API")
public class RemoteMariaDBController {

    private final RemoteMariaDBBackupService backupService;
    private final RemoteMariaDBRestoreService restoreService;
    private final BackupJobStatusManager jobStatusManager;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss");

    /**
     * MariaDB 원격 백업 실행
     *
     * @param request 백업 요청 정보
     * @return 백업 작업 응답
     */
    @PostMapping("/mariadb-backup")
    @Operation(summary = "MariaDB 원격 백업", description = "원격 MariaDB 서버의 데이터베이스를 백업합니다.")
    public ResponseEntity<ApiResponse<BackupJobResponse>> executeBackup(
            @Valid @RequestBody MariaDBBackupRequest request) {

        log.info("MariaDB 원격 백업 요청 - host: {}, database: {}",
                request.getHost(), request.getDatabase());

        BackupJobResponse response = backupService.executeBackup(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 백업 파일 목록 조회
     *
     * @return 백업 파일 목록
     */
    @GetMapping("/mariadb-backup/list")
    @Operation(summary = "백업 파일 목록 조회", description = "원격 MariaDB 백업 파일 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<BackupFileInfo>>> getBackupFileList() {

        log.info("백업 파일 목록 조회 요청");

        List<BackupFileInfo> fileList = backupService.getBackupFileList();

        return ResponseEntity.ok(ApiResponse.success(fileList));
    }

    /**
     * MariaDB 원격 백업 비동기 실행
     *
     * @param request 백업 요청 정보
     * @return 백업 작업 시작 응답
     */
    @PostMapping("/mariadb-backup/async")
    @Operation(summary = "MariaDB 원격 백업 (비동기)", description = "원격 MariaDB 서버의 데이터베이스를 비동기로 백업합니다.")
    public ResponseEntity<ApiResponse<BackupJobResponse>> executeBackupAsync(
            @Valid @RequestBody MariaDBBackupRequest request) {

        log.info("MariaDB 원격 백업 비동기 요청 - host: {}, database: {}",
                request.getHost(), request.getDatabase());

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String jobId = "backup_" + timestamp;
        String backupFileName = String.format("backup_remote_%s.sql", timestamp);
        String logFileName = String.format("backup_remote_mariadb_%s.log", timestamp);

        // 작업 시작 상태 저장
        BackupJobResponse runningResponse = BackupJobResponse.createRunning(jobId, backupFileName,
                "logs/" + logFileName);
        jobStatusManager.saveJobStatus(jobId, runningResponse);

        // 비동기 백업 실행
        backupService.executeBackupAsync(request, jobId, backupFileName, logFileName);

        return ResponseEntity.ok(ApiResponse.success(runningResponse));
    }

    /**
     * MariaDB 원격 복원 실행
     *
     * @param request 복원 요청 정보
     * @return 복원 작업 응답
     */
    @PostMapping("/mariadb-restore")
    @Operation(summary = "MariaDB 원격 복원", description = "백업 파일을 사용하여 원격 MariaDB 서버의 데이터베이스를 복원합니다.")
    public ResponseEntity<ApiResponse<BackupJobResponse>> executeRestore(
            @Valid @RequestBody MariaDBRestoreRequest request) {

        log.info("MariaDB 원격 복원 요청 - host: {}, backupFile: {}",
                request.getHost(), request.getBackupFileName());

        BackupJobResponse response = restoreService.executeRestore(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * MariaDB 원격 복원 비동기 실행
     *
     * @param request 복원 요청 정보
     * @return 복원 작업 시작 응답
     */
    @PostMapping("/mariadb-restore/async")
    @Operation(summary = "MariaDB 원격 복원 (비동기)", description = "백업 파일을 사용하여 원격 MariaDB 서버의 데이터베이스를 비동기로 복원합니다.")
    public ResponseEntity<ApiResponse<BackupJobResponse>> executeRestoreAsync(
            @Valid @RequestBody MariaDBRestoreRequest request) {

        log.info("MariaDB 원격 복원 비동기 요청 - host: {}, backupFile: {}",
                request.getHost(), request.getBackupFileName());

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String jobId = "restore_" + timestamp;
        String logFileName = String.format("restore_remote_mariadb_%s.log", timestamp);

        // 작업 시작 상태 저장
        BackupJobResponse runningResponse = BackupJobResponse.createRunning(jobId,
                request.getBackupFileName(), "logs/" + logFileName);
        jobStatusManager.saveJobStatus(jobId, runningResponse);

        // 비동기 복원 실행
        restoreService.executeRestoreAsync(request, jobId, logFileName);

        return ResponseEntity.ok(ApiResponse.success(runningResponse));
    }

    /**
     * 백업/복원 작업 상태 조회
     *
     * @param jobId 작업 ID
     * @return 작업 상태 응답
     */
    @GetMapping("/job-status/{jobId}")
    @Operation(summary = "작업 상태 조회", description = "백업 또는 복원 작업의 현재 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<BackupJobResponse>> getJobStatus(
            @PathVariable String jobId) {

        log.info("작업 상태 조회 요청 - jobId: {}", jobId);

        BackupJobResponse jobStatus = jobStatusManager.getJobStatus(jobId);

        if (jobStatus == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "작업 정보를 찾을 수 없습니다: " + jobId);
        }

        return ResponseEntity.ok(ApiResponse.success(jobStatus));
    }

    /**
     * 백업 파일 다운로드
     *
     * @param fileName 파일명
     * @return 백업 파일 리소스
     */
    @GetMapping("/mariadb-backup/download/{fileName}")
    @Operation(summary = "백업 파일 다운로드", description = "백업 파일을 다운로드합니다.")
    public ResponseEntity<Resource> downloadBackupFile(@PathVariable String fileName) {

        log.info("백업 파일 다운로드 요청 - fileName: {}", fileName);

        try {
            Path filePath = backupService.getBackupFilePath(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "백업 파일을 읽을 수 없습니다: " + fileName);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("백업 파일 다운로드 실패: {}", fileName, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "백업 파일 다운로드에 실패했습니다.");
        }
    }

    /**
     * 백업 파일 삭제
     *
     * @param fileName 파일명
     * @return 성공 응답
     */
    @DeleteMapping("/mariadb-backup/{fileName}")
    @Operation(summary = "백업 파일 삭제", description = "백업 파일을 삭제합니다.")
    public ResponseEntity<ApiResponse<String>> deleteBackupFile(@PathVariable String fileName) {

        log.info("백업 파일 삭제 요청 - fileName: {}", fileName);

        backupService.deleteBackupFile(fileName);

        return ResponseEntity.ok(ApiResponse.success("백업 파일이 삭제되었습니다: " + fileName));
    }
}
