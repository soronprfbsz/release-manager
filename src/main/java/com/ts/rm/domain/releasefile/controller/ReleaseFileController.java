package com.ts.rm.domain.releasefile.controller;

import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.domain.releasefile.service.ReleaseFileService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseFileController implements ReleaseFileControllerDocs {

    private final ReleaseFileService releaseFileService;

    @Override
    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadReleaseFile(@PathVariable("id") Long fileId) {

        Resource resource = releaseFileService.downloadReleaseFile(fileId);
        ReleaseFileDto.DetailResponse releaseFile = releaseFileService.getReleaseFileById(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        HttpFileDownloadUtil.buildContentDisposition(releaseFile.fileName()))
                .body(resource);
    }

    @Override
    @GetMapping("/versions/{versionId}/download")
    public void downloadVersionFiles(@PathVariable("versionId") Long versionId,
                                      HttpServletResponse response) throws IOException {

        log.info("버전별 파일 스트리밍 다운로드 API 호출 - versionId: {}", versionId);

        String fileName = releaseFileService.getVersionZipFileName(versionId);
        long uncompressedSize = releaseFileService.calculateUncompressedSize(versionId);

        // HTTP 응답 헤더 설정
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(fileName));

        // 압축 전 크기를 커스텀 헤더로 전달 (프론트엔드 진행률 표시용)
        response.setHeader("X-Uncompressed-Size", String.valueOf(uncompressedSize));

        // 스트리밍 방식으로 ZIP 생성 및 전송
        releaseFileService.streamVersionFilesAsZip(versionId, response.getOutputStream());

        log.info("버전별 파일 스트리밍 다운로드 완료 - versionId: {}, fileName: {}, uncompressedSize: {} bytes",
                versionId, fileName, uncompressedSize);
    }
}
