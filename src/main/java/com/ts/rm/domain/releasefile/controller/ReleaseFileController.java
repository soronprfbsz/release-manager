package com.ts.rm.domain.releasefile.controller;

import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.domain.releasefile.service.ReleaseFileService;
import com.ts.rm.global.file.HttpFileDownloadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
                        HttpFileDownloadUtil.buildContentDisposition(releaseFile.fileName()))
                .body(resource);
    }

    /**
     * 버전별 모든 파일 일괄 다운로드 (ZIP) - 스트리밍 방식
     *
     * <p>메모리 효율적인 스트리밍 방식으로 ZIP 파일을 생성하여 다운로드합니다.
     * 대용량 파일도 OOM 없이 안전하게 다운로드 가능합니다.
     *
     * <p>응답 헤더:
     * <ul>
     *   <li><b>X-Uncompressed-Size</b>: 압축 전 총 파일 크기 (바이트) - 진행률 표시용</li>
     * </ul>
     */
    @Operation(summary = "버전별 파일 일괄 다운로드 (스트리밍)",
            description = "특정 버전의 모든 파일을 ZIP 형식으로 스트리밍 다운로드합니다.\n\n"
                    + "각 폴더 내에는 실행 순서대로 정렬된 파일들이 포함됩니다.\n\n"
                    + "**응답 헤더**:\n"
                    + "- `X-Uncompressed-Size`: 압축 전 총 파일 크기 (바이트) - 진행률 표시용")
    @GetMapping("/versions/{versionId}/download")
    public void downloadVersionFiles(
            @Parameter(description = "릴리즈 버전 ID", required = true)
            @PathVariable("versionId") Long versionId,
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

        // Content-Length는 스트리밍 방식에서 미리 알 수 없으므로 설정하지 않음
        // 브라우저는 Transfer-Encoding: chunked로 처리

        // 스트리밍 방식으로 ZIP 생성 및 전송
        releaseFileService.streamVersionFilesAsZip(versionId, response.getOutputStream());

        log.info("버전별 파일 스트리밍 다운로드 완료 - versionId: {}, fileName: {}, uncompressedSize: {} bytes",
                versionId, fileName, uncompressedSize);
    }
}
