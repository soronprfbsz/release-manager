package com.ts.rm.domain.releasefile.controller;

import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.domain.releasefile.service.ReleaseFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

    /**
     * 버전별 모든 파일 일괄 다운로드 (ZIP)
     */
    @Operation(summary = "버전별 파일 일괄 다운로드",
            description = "특정 버전의 모든 파일을 ZIP 형식으로 다운로드합니다.\n\n"
                    + "ZIP 파일에는 다음 구조로 파일이 포함됩니다:\n"
                    + "- mariadb/ : MariaDB SQL 파일들\n"
                    + "- cratedb/ : CrateDB SQL 파일들\n\n"
                    + "각 폴더 내에는 실행 순서대로 정렬된 SQL 파일들이 포함됩니다.")
    @GetMapping("/versions/{versionId}/download")
    public ResponseEntity<byte[]> downloadVersionFiles(
            @Parameter(description = "릴리즈 버전 ID", required = true)
            @PathVariable("versionId") Long versionId) {

        log.info("버전별 파일 일괄 다운로드 API 호출 - versionId: {}", versionId);

        byte[] zipBytes = releaseFileService.downloadVersionFilesAsZip(versionId);
        String fileName = releaseFileService.getVersionZipFileName(versionId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipBytes.length))
                .body(zipBytes);
    }
}
