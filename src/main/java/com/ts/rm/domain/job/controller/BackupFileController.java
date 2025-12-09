package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.BackupFileDto;
import com.ts.rm.domain.job.service.BackupFileService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 백업 파일 컨트롤러
 *
 * <p>백업 파일 조회/다운로드/삭제 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs/backup-files")
@RequiredArgsConstructor
public class BackupFileController implements BackupFileControllerDocs {

    private final BackupFileService backupFileService;

    @Override
    @GetMapping
    public ApiResponse<Page<BackupFileDto.ListResponse>> listBackupFiles(
            @RequestParam(required = false) String fileCategory,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String fileName,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("백업 파일 목록 조회 요청 - fileCategory: {}, fileType: {}, fileName: {}, page: {}, size: {}",
                fileCategory, fileType, fileName, pageable.getPageNumber(), pageable.getPageSize());

        BackupFileDto.SearchRequest searchRequest = new BackupFileDto.SearchRequest(
                fileCategory, fileType, fileName);
        Page<BackupFileDto.ListResponse> response = backupFileService.searchBackupFiles(searchRequest, pageable);

        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{id}/download")
    public void downloadBackupFile(@PathVariable Long id, HttpServletResponse response) throws IOException {

        log.info("백업 파일 다운로드 요청 - ID: {}", id);

        String fileName = backupFileService.getFileName(id);
        long fileSize = backupFileService.getFileSize(id);

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(fileName));
        response.setContentLengthLong(fileSize);

        backupFileService.downloadFile(id, response.getOutputStream());

        log.info("백업 파일 다운로드 완료 - ID: {}, fileName: {}", id, fileName);
    }

    @Override
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBackupFile(@PathVariable Long id) {

        log.info("백업 파일 삭제 요청 - ID: {}", id);

        backupFileService.deleteFile(id);

        return ApiResponse.success(null);
    }
}
