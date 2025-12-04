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
            description = "스크립트 또는 문서 파일을 업로드합니다.\n\n"
                    + "**파일 카테고리 (fileCategory)**:\n"
                    + "- `SCRIPT`: 쉘 스크립트 파일\n"
                    + "- `DOCUMENT`: 문서 파일\n"
                    + "- `ETC`: 기타 파일\n\n"
                    + "**서브 카테고리 (subCategory)**:\n"
                    + "- SCRIPT: MARIADB_BACKUP, MARIADB_RESTORE, CRATEDB_BACKUP, CRATEDB_RESTORE, ETC\n"
                    + "- DOCUMENT: PDF, TXT, MD, DOC, HWP, ETC\n"
                    + "- ETC: 없음"
    )
    public ApiResponse<ResourceFileDto.DetailResponse> uploadFile(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @Parameter(description = "업로드할 파일", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "파일 카테고리 (SCRIPT/DOCUMENT/ETC)", required = true, example = "SCRIPT")
            @RequestParam String fileCategory,

            @Parameter(description = "서브 카테고리 (예: MARIADB_BACKUP, PDF)", example = "MARIADB_BACKUP")
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
                    + "- `fileCategory` 파라미터로 파일 카테고리별 필터링 가능 (SCRIPT/DOCUMENT/ETC)\n"
                    + "- 생략 시 전체 목록 반환"
    )
    public ApiResponse<List<ResourceFileDto.SimpleResponse>> listResourceFiles(
            @Parameter(description = "파일 카테고리 필터 (SCRIPT/DOCUMENT/ETC)")
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
