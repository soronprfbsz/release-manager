package com.ts.rm.domain.release.controller;

import com.ts.rm.domain.release.dto.ReleaseFileDto;
import com.ts.rm.domain.release.service.ReleaseFileService;
import com.ts.rm.global.common.response.ApiResponse;
import com.ts.rm.global.common.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseFile Controller
 *
 * <p>릴리즈 파일 관리 REST API
 */
@Tag(name = "ReleaseFile", description = "릴리즈 파일 관리 API")
@RestController
@RequestMapping("/api/v1/releases")
@RequiredArgsConstructor
@SwaggerResponse
public class ReleaseFileController {

    private final ReleaseFileService releaseFileService;

    /**
     * 버전별 릴리즈 파일 목록 조회
     */
    @Operation(summary = "버전별 릴리즈 파일 목록 조회", description = "릴리즈 버전별 파일 목록을 조회합니다")
    @GetMapping("/versions/{versionId}/files")
    public ResponseEntity<ApiResponse<List<ReleaseFileDto.SimpleResponse>>> getReleaseFilesByVersion(
            @Parameter(description = "릴리즈 버전 ID", required = true) @PathVariable Long versionId,
            @Parameter(description = "데이터베이스 타입") @RequestParam(required = false) String databaseType) {

        List<ReleaseFileDto.SimpleResponse> response;
        if (databaseType != null) {
            response = releaseFileService.getReleaseFilesByVersionAndDbType(versionId, databaseType);
        } else {
            response = releaseFileService.getReleaseFilesByVersion(versionId);
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 파일 상세 조회
     */
    @Operation(summary = "릴리즈 파일 상세 조회", description = "ID로 릴리즈 파일 상세 정보를 조회합니다")
    @GetMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<ReleaseFileDto.DetailResponse>> getReleaseFileById(
            @Parameter(description = "릴리즈 파일 ID", required = true) @PathVariable Long fileId) {
        ReleaseFileDto.DetailResponse response = releaseFileService.getReleaseFileById(fileId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 파일 업로드
     */
    @Operation(summary = "릴리즈 파일 업로드", description = "릴리즈 버전에 파일을 업로드합니다")
    @PostMapping("/versions/{versionId}/files/upload")
    public ResponseEntity<ApiResponse<List<ReleaseFileDto.DetailResponse>>> uploadReleaseFiles(
            @Parameter(description = "릴리즈 버전 ID", required = true) @PathVariable Long versionId,
            @Parameter(description = "업로드할 파일 목록", required = true) @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "데이터베이스 타입", required = true) @RequestParam String databaseType,
            @Parameter(description = "업로드 사용자", required = true) @RequestParam String uploadedBy) {

        ReleaseFileDto.UploadRequest request = ReleaseFileDto.UploadRequest.builder()
                .databaseType(databaseType)
                .uploadedBy(uploadedBy)
                .build();

        List<ReleaseFileDto.DetailResponse> response = releaseFileService.uploadReleaseFiles(versionId,
                files, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 릴리즈 파일 다운로드
     */
    @Operation(summary = "릴리즈 파일 다운로드", description = "릴리즈 파일을 다운로드합니다")
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadReleaseFile(
            @Parameter(description = "릴리즈 파일 ID", required = true) @PathVariable Long fileId) {

        Resource resource = releaseFileService.downloadReleaseFile(fileId);
        ReleaseFileDto.DetailResponse releaseFile = releaseFileService.getReleaseFileById(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + releaseFile.fileName() + "\"")
                .body(resource);
    }

    /**
     * 릴리즈 파일 정보 수정
     */
    @Operation(summary = "릴리즈 파일 정보 수정", description = "릴리즈 파일의 설명 및 실행 순서를 수정합니다")
    @PutMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<ReleaseFileDto.DetailResponse>> updateReleaseFile(
            @Parameter(description = "릴리즈 파일 ID", required = true) @PathVariable Long fileId,
            @Valid @RequestBody ReleaseFileDto.UpdateRequest request) {
        ReleaseFileDto.DetailResponse response = releaseFileService.updateReleaseFile(fileId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 파일 삭제
     */
    @Operation(summary = "릴리즈 파일 삭제", description = "릴리즈 파일을 삭제합니다")
    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteReleaseFile(
            @Parameter(description = "릴리즈 파일 ID", required = true) @PathVariable Long fileId) {
        releaseFileService.deleteReleaseFile(fileId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
