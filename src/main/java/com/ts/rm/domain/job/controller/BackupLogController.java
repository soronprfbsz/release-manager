package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.BackupLogDto;
import com.ts.rm.domain.job.service.BackupLogService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 백업 로그 컨트롤러
 *
 * <p>백업 파일에 대한 로그 파일 조회 및 다운로드 API
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs/backup-files")
@RequiredArgsConstructor
@Tag(name = "백업 로그", description = "백업 파일 관련 로그 조회/다운로드 API")
public class BackupLogController {

    private final BackupLogService backupLogService;

    /**
     * 백업 파일에 대한 로그 파일 목록 조회
     *
     * <p>해당 백업 파일 생성 시의 로그와 이 백업 파일로 복원을 시도한 로그 목록을 반환합니다.
     */
    @GetMapping("/{id}/logs")
    @Operation(
            summary = "백업 로그 파일 목록 조회",
            description = """
                    백업 파일과 관련된 로그 파일 목록을 조회합니다.

                    **반환되는 로그 유형**:
                    - `BACKUP`: 백업 파일 생성 시 생성된 로그
                    - `RESTORE`: 해당 백업 파일로 복원을 시도한 로그들

                    **사용 예시**:
                    1. 이 API로 로그 파일 목록을 조회
                    2. 응답의 `logFileName`을 사용하여 `/download` API로 로그 다운로드
                    """
    )
    public ApiResponse<BackupLogDto.LogListResponse> getLogFiles(
            @Parameter(description = "백업 파일 ID", example = "1")
            @PathVariable Long id) {

        log.info("백업 로그 파일 목록 조회 요청 - backupFileId: {}", id);

        BackupLogDto.LogListResponse response = backupLogService.getLogFiles(id);

        return ApiResponse.success(response);
    }

    /**
     * 로그 파일 다운로드
     *
     * <p>지정된 백업 파일의 관련 로그 파일을 다운로드합니다.
     */
    @GetMapping("/{id}/logs/download")
    @Operation(
            summary = "로그 파일 다운로드",
            description = """
                    백업 파일 관련 로그 파일을 다운로드합니다.

                    **사용 방법**:
                    1. 먼저 `GET /api/jobs/backup-files/{id}/logs` API로 로그 파일 목록 조회
                    2. 응답에서 원하는 로그의 `logFileName` 값 확인
                    3. 해당 값을 `logFileName` 파라미터로 전달하여 다운로드

                    **예시**:
                    ```
                    GET /api/jobs/backup-files/1/logs/download?logFileName=backup_mariadb_20251205_120000.log
                    ```
                    """
    )
    public void downloadLogFile(
            @Parameter(description = "백업 파일 ID", example = "1")
            @PathVariable Long id,

            @Parameter(description = "다운로드할 로그 파일명 (로그 목록 조회 API에서 확인)",
                    example = "backup_mariadb_20251205_120000.log")
            @RequestParam String logFileName,

            HttpServletResponse response) throws IOException {

        log.info("로그 파일 다운로드 요청 - backupFileId: {}, logFileName: {}", id, logFileName);

        long fileSize = backupLogService.getLogFileSize(id, logFileName);

        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(logFileName));
        response.setContentLengthLong(fileSize);

        backupLogService.downloadLogFile(id, logFileName, response.getOutputStream());

        log.info("로그 파일 다운로드 완료 - backupFileId: {}, logFileName: {}", id, logFileName);
    }
}
