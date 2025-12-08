package com.ts.rm.domain.resourcefile.controller;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import com.ts.rm.domain.resourcefile.mapper.ResourceFileDtoMapper;
import com.ts.rm.domain.resourcefile.service.ResourceFileService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "리소스 파일", description = "리소스 파일(스크립트, 문서) 관리 API")
public class ResourceFileController {

    private final ResourceFileService resourceFileService;
    private final ResourceFileDtoMapper resourceFileDtoMapper;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 리소스 파일 업로드
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "리소스 파일 업로드",
            description = "스크립트, Docker, 문서 등 리소스 파일을 업로드합니다.\n\n"
                    + "**파일 카테고리 (fileCategory)**:\n"
                    + "- `SCRIPT`: 스크립트 파일\n"
                    + "- `DOCKER`: Docker 관련 파일\n"
                    + "- `DOCUMENT`: 문서 파일\n"
                    + "- `ETC`: 기타 파일\n\n"
                    + "**하위 카테고리 (subCategory)**:\n"
                    + "- SCRIPT: MARIADB, CRATEDB, ETC\n"
                    + "- DOCKER: SERVICE, DOCKERFILE, ETC\n"
                    + "- DOCUMENT: INFRAEYE1, INFRAEYE2, ETC\n"
                    + "- ETC: ETC"
    )
    public ApiResponse<ResourceFileDto.DetailResponse> uploadFile(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(description = "업로드할 파일", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "파일 카테고리 (SCRIPT/DOCKER/DOCUMENT/ETC)", required = true, example = "SCRIPT")
            @RequestParam String fileCategory,

            @Parameter(description = "하위 카테고리 (예: MARIADB, INFRAEYE2)", example = "MARIADB")
            @RequestParam(required = false) String subCategory,

            @Parameter(description = "파일 설명", example = "MariaDB 백업 스크립트")
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
    @GetMapping("/{id}")
    @Operation(summary = "리소스 파일 상세 조회", description = "리소스 파일 ID로 상세 정보를 조회합니다.")
    public ApiResponse<ResourceFileDto.DetailResponse> getResourceFile(
            @PathVariable Long id) {

        log.info("리소스 파일 조회 요청 - ID: {}", id);

        ResourceFile resourceFile = resourceFileService.getResourceFile(id);
        ResourceFileDto.DetailResponse response = resourceFileDtoMapper.toDetailResponse(resourceFile);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 파일 목록 조회
     */
    @GetMapping
    @Operation(
            summary = "리소스 파일 목록 조회",
            description = "리소스 파일 목록을 조회합니다.\n\n"
                    + "**필터링**:\n"
                    + "- `fileCategory` 파라미터로 파일 카테고리별 필터링 가능 (SCRIPT/DOCKER/DOCUMENT/ETC)\n"
                    + "- 생략 시 전체 목록 반환"
    )
    public ApiResponse<List<ResourceFileDto.SimpleResponse>> listResourceFiles(
            @Parameter(description = "파일 카테고리 필터 (SCRIPT/DOCKER/DOCUMENT/ETC)")
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
    @GetMapping("/categories")
    @Operation(
            summary = "리소스 파일 분류 가이드 조회",
            description = "리소스 파일 업로드 시 사용 가능한 카테고리 및 하위 카테고리 목록을 조회합니다."
    )
    public ApiResponse<java.util.List<ResourceFileDto.CategoryGuideResponse>> getCategoryGuide() {
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
    @GetMapping("/{id}/download")
    @Operation(
            summary = "리소스 파일 다운로드",
            description = "리소스 파일을 다운로드합니다."
    )
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
    @DeleteMapping("/{id}")
    @Operation(
            summary = "리소스 파일 삭제",
            description = "리소스 파일을 삭제합니다.\n\n"
                    + "**삭제 범위**:\n"
                    + "- DB 레코드 (resource_file 테이블)\n"
                    + "- 실제 파일\n\n"
                    + "**주의사항**:\n"
                    + "- 삭제된 데이터는 복구할 수 없습니다."
    )
    public ApiResponse<Void> deleteResourceFile(@PathVariable Long id) {

        log.info("리소스 파일 삭제 요청 - ID: {}", id);

        resourceFileService.deleteFile(id);

        return ApiResponse.success(null);
    }
}
