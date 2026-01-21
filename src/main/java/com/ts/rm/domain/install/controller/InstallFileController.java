package com.ts.rm.domain.install.controller;

import com.ts.rm.domain.install.dto.InstallFileDto;
import com.ts.rm.domain.install.service.InstallFileService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Install File Controller
 *
 * <p>프로젝트별 인스톨 파일 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{id}/installs")
@RequiredArgsConstructor
public class InstallFileController implements InstallFileControllerDocs {

    private final InstallFileService installFileService;

    /**
     * 인스톨 파일 트리 조회
     *
     * @param id 프로젝트 ID
     * @return 인스톨 파일 트리 응답
     */
    @Override
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<InstallFileDto.FilesResponse>> getInstallFiles(
            @PathVariable String id) {

        log.info("인스톨 파일 트리 조회 API 호출 - projectId: {}", id);

        InstallFileDto.FilesResponse response = installFileService.getInstallFiles(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 인스톨 디렉토리 생성
     *
     * @param id   프로젝트 ID
     * @param path 생성할 디렉토리 경로
     * @return 생성 결과 응답
     */
    @Override
    @PostMapping("/files/directory")
    public ResponseEntity<ApiResponse<InstallFileDto.DirectoryResponse>> createDirectory(
            @PathVariable String id,
            @RequestParam String path) {

        log.info("인스톨 디렉토리 생성 API 호출 - projectId: {}, path: {}", id, path);

        InstallFileDto.DirectoryResponse response = installFileService.createDirectory(id, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 인스톨 파일 업로드
     *
     * @param id         프로젝트 ID
     * @param file       업로드할 파일 (ZIP 또는 단일 파일)
     * @param targetPath 대상 경로 (선택)
     * @param extractZip ZIP 파일 압축 해제 여부 (기본값: true)
     * @return 업로드 결과 응답
     */
    @Override
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<InstallFileDto.UploadResponse>> uploadFile(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String targetPath,
            @RequestParam(required = false, defaultValue = "true") Boolean extractZip) {

        log.info("인스톨 파일 업로드 API 호출 - projectId: {}, fileName: {}, targetPath: {}, extractZip: {}",
                id, file.getOriginalFilename(), targetPath, extractZip);

        InstallFileDto.UploadResponse response =
                installFileService.uploadFile(id, file, targetPath, extractZip);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 인스톨 파일 삭제
     *
     * @param id       프로젝트 ID
     * @param filePath 삭제할 파일/디렉토리 경로
     * @return 삭제 결과 응답
     */
    @Override
    @DeleteMapping("/files")
    public ResponseEntity<ApiResponse<InstallFileDto.DeleteResponse>> deleteFile(
            @PathVariable String id,
            @RequestParam String filePath) {

        log.info("인스톨 파일 삭제 API 호출 - projectId: {}, filePath: {}", id, filePath);

        InstallFileDto.DeleteResponse response = installFileService.deleteFile(id, filePath);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 인스톨 전체 파일 ZIP 다운로드
     *
     * @param id       프로젝트 ID
     * @param response HTTP 응답
     */
    @Override
    @GetMapping("/files/zip-download")
    public void downloadAllFiles(
            @PathVariable String id,
            HttpServletResponse response) throws IOException {

        log.info("인스톨 전체 파일 다운로드 API 호출 - projectId: {}", id);

        String zipFileName = id + "_install_files.zip";
        long totalSize = installFileService.getTotalSize(id);

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(zipFileName));
        response.setHeader("X-Uncompressed-Size", String.valueOf(totalSize));

        installFileService.downloadAllFiles(id, response.getOutputStream());

        log.info("인스톨 전체 파일 다운로드 완료 - projectId: {}", id);
    }
}
