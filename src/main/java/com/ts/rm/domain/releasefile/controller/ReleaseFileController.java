package com.ts.rm.domain.releasefile.controller;

import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.domain.releasefile.service.ReleaseFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReleaseFile Controller
 *
 * <p>릴리즈 파일 관리 REST API
 */
@Tag(name = "릴리즈 파일", description = "릴리즈 파일 관리 API")
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseFileController {

    private final ReleaseFileService releaseFileService;

    /**
     * 릴리즈 파일 다운로드
     */
    @Operation(summary = "릴리즈 파일 다운로드", description = "릴리즈 파일을 다운로드합니다")
    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadReleaseFile(
            @Parameter(description = "릴리즈 파일 ID", required = true) @PathVariable("id") Long fileId) {

        Resource resource = releaseFileService.downloadReleaseFile(fileId);
        ReleaseFileDto.DetailResponse releaseFile = releaseFileService.getReleaseFileById(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + releaseFile.fileName() + "\"")
                .body(resource);
    }
}
