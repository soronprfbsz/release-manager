package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.BackupLogDto;
import com.ts.rm.domain.job.service.BackupLogService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
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
public class BackupLogController implements BackupLogControllerDocs {

    private final BackupLogService backupLogService;

    @Override
    @GetMapping("/{id}/logs")
    public ApiResponse<BackupLogDto.LogListResponse> getLogFiles(@PathVariable Long id) {

        log.info("백업 로그 파일 목록 조회 요청 - backupFileId: {}", id);

        BackupLogDto.LogListResponse response = backupLogService.getLogFiles(id);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{id}/logs/download")
    public void downloadLogFile(@PathVariable Long id,
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
