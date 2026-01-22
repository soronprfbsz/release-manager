package com.ts.rm.domain.resourcefile.controller;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.domain.resourcefile.service.ResourceFileService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ResourceFile Controller
 *
 * <p>카테고리별 리소스 파일 관리 REST API (파일시스템 기반)
 */
@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceFileController implements ResourceFileControllerDocs {

    private final ResourceFileService resourceFileService;

    /**
     * 카테고리 목록 조회
     *
     * @return 카테고리 목록 응답
     */
    @Override
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<ResourceFileDto.CategoriesResponse>> getCategories() {
        log.info("카테고리 목록 조회 API 호출");

        ResourceFileDto.CategoriesResponse response = resourceFileService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리 생성
     *
     * @param request 카테고리 생성 요청
     * @return 생성 결과 응답
     */
    @Override
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<ResourceFileDto.CategoryCreateResponse>> createCategory(
            @RequestBody ResourceFileDto.CategoryCreateRequest request) {

        log.info("카테고리 생성 API 호출 - categoryName: {}", request.categoryName());

        ResourceFileDto.CategoryCreateResponse response = resourceFileService.createCategory(request.categoryName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 카테고리 삭제
     *
     * @param category 카테고리명
     * @return 삭제 결과 응답
     */
    @Override
    @DeleteMapping("/categories/{category}")
    public ResponseEntity<ApiResponse<ResourceFileDto.CategoryDeleteResponse>> deleteCategory(
            @PathVariable String category) {

        log.info("카테고리 삭제 API 호출 - category: {}", category);

        ResourceFileDto.CategoryDeleteResponse response = resourceFileService.deleteCategory(category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 파일 트리 조회
     *
     * @param category 카테고리명
     * @return 파일 트리 응답
     */
    @Override
    @GetMapping("/{category}/files")
    public ResponseEntity<ApiResponse<ResourceFileDto.FilesResponse>> getFiles(
            @PathVariable String category) {

        log.info("파일 트리 조회 API 호출 - category: {}", category);

        ResourceFileDto.FilesResponse response = resourceFileService.getFiles(category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 디렉토리 생성
     *
     * @param category 카테고리명
     * @param path     생성할 디렉토리 경로
     * @return 생성 결과 응답
     */
    @Override
    @PostMapping("/{category}/files/directory")
    public ResponseEntity<ApiResponse<ResourceFileDto.DirectoryResponse>> createDirectory(
            @PathVariable String category,
            @RequestParam String path) {

        log.info("디렉토리 생성 API 호출 - category: {}, path: {}", category, path);

        ResourceFileDto.DirectoryResponse response = resourceFileService.createDirectory(category, path);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 파일 업로드
     *
     * @param category   카테고리명
     * @param file       업로드할 파일 (ZIP 또는 단일 파일)
     * @param targetPath 대상 경로 (선택)
     * @param extractZip ZIP 파일 압축 해제 여부 (기본값: true)
     * @return 업로드 결과 응답
     */
    @Override
    @PostMapping(value = "/{category}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResourceFileDto.UploadResponse>> uploadFile(
            @PathVariable String category,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String targetPath,
            @RequestParam(required = false, defaultValue = "true") Boolean extractZip) {

        log.info("파일 업로드 API 호출 - category: {}, fileName: {}, targetPath: {}, extractZip: {}",
                category, file.getOriginalFilename(), targetPath, extractZip);

        ResourceFileDto.UploadResponse response =
                resourceFileService.uploadFile(category, file, targetPath, extractZip);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 파일/디렉토리 삭제
     *
     * @param category 카테고리명
     * @param filePath 삭제할 파일/디렉토리 경로
     * @return 삭제 결과 응답
     */
    @Override
    @DeleteMapping("/{category}/files")
    public ResponseEntity<ApiResponse<ResourceFileDto.DeleteResponse>> deleteFile(
            @PathVariable String category,
            @RequestParam String filePath) {

        log.info("파일 삭제 API 호출 - category: {}, filePath: {}", category, filePath);

        ResourceFileDto.DeleteResponse response = resourceFileService.deleteFile(category, filePath);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 카테고리 전체 파일 ZIP 다운로드
     *
     * @param category 카테고리명
     * @param response HTTP 응답
     */
    @Override
    @GetMapping("/{category}/files/zip-download")
    public void downloadAllFiles(
            @PathVariable String category,
            HttpServletResponse response) throws IOException {

        log.info("전체 파일 다운로드 API 호출 - category: {}", category);

        String zipFileName = category + "_files.zip";
        long totalSize = resourceFileService.getTotalSize(category);

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(zipFileName));
        response.setHeader("X-Uncompressed-Size", String.valueOf(totalSize));

        resourceFileService.downloadAllFiles(category, response.getOutputStream());

        log.info("전체 파일 다운로드 완료 - category: {}", category);
    }
}
