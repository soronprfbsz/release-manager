package com.ts.rm.domain.resourcefile.controller;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import com.ts.rm.domain.resourcefile.mapper.ResourceFileDtoMapper;
import com.ts.rm.domain.resourcefile.service.ResourceFileService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 리소스 파일 API Controller
 *
 * <p>스크립트, 문서 등 리소스 파일 업로드/다운로드/삭제 API
 */
@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceFileController implements ResourceFileControllerDocs {

    private final ResourceFileService resourceFileService;
    private final ResourceFileDtoMapper resourceFileDtoMapper;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리소스 파일 업로드
     */
    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ResourceFileDto.DetailResponse> uploadFile(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestPart("file") MultipartFile file,
            @RequestParam String fileCategory,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String description) {

        log.info("리소스 파일 업로드 요청 - 파일명: {}, 파일카테고리: {}, 서브카테고리: {}",
                file.getOriginalFilename(), fileCategory, subCategory);

        String token = authorizationHeader.substring(7);
        String createdBy = jwtTokenProvider.getEmail(token);

        ResourceFileDto.UploadRequest request = new ResourceFileDto.UploadRequest(
                fileCategory, subCategory, description, createdBy
        );

        ResourceFile resourceFile = resourceFileService.uploadFile(file, request);
        ResourceFileDto.DetailResponse response = resourceFileDtoMapper.toDetailResponse(resourceFile);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 파일 상세 조회
     */
    @Override
    @GetMapping("/{id}")
    public ApiResponse<ResourceFileDto.DetailResponse> getResourceFile(@PathVariable Long id) {

        log.info("리소스 파일 조회 요청 - ID: {}", id);

        ResourceFile resourceFile = resourceFileService.getResourceFile(id);
        ResourceFileDto.DetailResponse response = resourceFileDtoMapper.toDetailResponse(resourceFile);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 파일 목록 조회
     */
    @Override
    @GetMapping
    public ApiResponse<List<ResourceFileDto.SimpleResponse>> listResourceFiles(
            @RequestParam(required = false) String fileCategory) {

        log.info("리소스 파일 목록 조회 요청 - 파일카테고리: {}", fileCategory);

        List<ResourceFile> resourceFiles;
        if (fileCategory != null && !fileCategory.isBlank()) {
            resourceFiles = resourceFileService.listFilesByCategory(fileCategory);
        } else {
            resourceFiles = resourceFileService.listAllFiles();
        }

        List<ResourceFileDto.SimpleResponse> response = resourceFileDtoMapper.toSimpleResponseList(resourceFiles);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 파일 분류 가이드 조회
     */
    @Override
    @GetMapping("/categories")
    public ApiResponse<List<ResourceFileDto.CategoryGuideResponse>> getCategoryGuide() {
        log.info("리소스 파일 분류 가이드 조회 요청");

        java.util.List<ResourceFileDto.CategoryGuideResponse> guides = java.util.List.of(
                new ResourceFileDto.CategoryGuideResponse(
                        "SCRIPT",
                        "스크립트",
                        "스크립트 파일 (백업, 복원 등)",
                        java.util.List.of(
                                new ResourceFileDto.SubCategoryInfo("MARIADB", "MariaDB", "MariaDB 관련 스크립트"),
                                new ResourceFileDto.SubCategoryInfo("CRATEDB", "CrateDB", "CrateDB 관련 스크립트"),
                                new ResourceFileDto.SubCategoryInfo("ETC", "기타", "기타 스크립트")
                        )
                ),
                new ResourceFileDto.CategoryGuideResponse(
                        "DOCKER",
                        "Docker",
                        "Docker 관련 파일 (컴포즈, Dockerfile 등)",
                        java.util.List.of(
                                new ResourceFileDto.SubCategoryInfo("SERVICE", "서비스 실행", "Docker 서비스 실행 관련 파일"),
                                new ResourceFileDto.SubCategoryInfo("DOCKERFILE", "Dockerfile", "Dockerfile 및 빌드 관련 파일"),
                                new ResourceFileDto.SubCategoryInfo("ETC", "기타", "기타 Docker 파일")
                        )
                ),
                new ResourceFileDto.CategoryGuideResponse(
                        "DOCUMENT",
                        "문서",
                        "설치 가이드 및 기타 문서",
                        java.util.List.of(
                                new ResourceFileDto.SubCategoryInfo("INFRAEYE1", "Infraeye 1", "Infraeye 1 관련 문서"),
                                new ResourceFileDto.SubCategoryInfo("INFRAEYE2", "Infraeye 2", "Infraeye 2 관련 문서"),
                                new ResourceFileDto.SubCategoryInfo("ETC", "기타", "기타 문서")
                        )
                ),
                new ResourceFileDto.CategoryGuideResponse(
                        "ETC",
                        "기타",
                        "기타 리소스 파일",
                        java.util.List.of(
                                new ResourceFileDto.SubCategoryInfo("ETC", "기타", "기타 분류되지 않은 파일")
                        )
                )
        );

        return ApiResponse.success(guides);
    }

    /**
     * 리소스 파일 다운로드
     */
    @Override
    @GetMapping("/{id}/download")
    public void downloadResourceFile(
            @PathVariable Long id,
            HttpServletResponse response) throws IOException {

        log.info("리소스 파일 다운로드 요청 - ID: {}", id);

        String fileName = resourceFileService.getFileName(id);
        long fileSize = resourceFileService.getFileSize(id);

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(fileName));
        response.setContentLengthLong(fileSize);

        resourceFileService.downloadFile(id, response.getOutputStream());

        log.info("리소스 파일 다운로드 완료 - ID: {}, fileName: {}", id, fileName);
    }

    /**
     * 리소스 파일 삭제
     */
    @Override
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResourceFile(@PathVariable Long id) {

        log.info("리소스 파일 삭제 요청 - ID: {}", id);

        resourceFileService.deleteFile(id);

        return ApiResponse.success(null);
    }
}
