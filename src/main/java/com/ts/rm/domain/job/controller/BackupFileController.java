package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.BackupFileDto;
import com.ts.rm.domain.job.service.BackupFileService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "백업 파일", description = "백업 파일 관리 API")
public class BackupFileController {

    private final BackupFileService backupFileService;

    /**
     * 백업 파일 목록 조회 (검색 + 페이징)
     */
    @GetMapping
    @Operation(
            summary = "백업 파일 목록 조회",
            description = "백업 파일 목록을 검색 조건과 페이징으로 조회합니다.\n\n"
                    + "**검색 조건**:\n"
                    + "- `fileCategory`: 파일 카테고리 (MARIADB, CRATEDB)\n"
                    + "- `fileType`: 파일 타입 (확장자 대문자, 예: SQL, GZ, ZIP)\n"
                    + "- `fileName`: 파일명 (부분 일치)"
    )
    public ApiResponse<Page<BackupFileDto.ListResponse>> listBackupFiles(
            @Parameter(description = "파일 카테고리 (MARIADB, CRATEDB)")
            @RequestParam(required = false) String fileCategory,

            @Parameter(description = "파일 타입 (예: SQL, GZ, ZIP)")
            @RequestParam(required = false) String fileType,

            @Parameter(description = "파일명 (부분 일치)")
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

    /**
     * 백업 파일 다운로드
     */
    @GetMapping("/{id}/download")
    @Operation(
            summary = "백업 파일 다운로드",
            description = "백업 파일을 다운로드합니다."
    )
    public void downloadBackupFile(
            @PathVariable Long id,
            HttpServletResponse response) throws IOException {

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

    /**
     * 백업 파일 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "백업 파일 삭제",
            description = "백업 파일을 삭제합니다.\n\n"
                    + "**삭제 범위**:\n"
                    + "- DB 레코드 (backup_file 테이블)\n"
                    + "- 실제 파일\n\n"
                    + "**주의사항**:\n"
                    + "- 삭제된 데이터는 복구할 수 없습니다."
    )
    public ApiResponse<Void> deleteBackupFile(@PathVariable Long id) {

        log.info("백업 파일 삭제 요청 - ID: {}", id);

        backupFileService.deleteFile(id);

        return ApiResponse.success(null);
    }
}
