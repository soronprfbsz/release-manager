package com.ts.rm.global.file;

import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공통 파일 API Controller
 *
 * <p>파일 내용 조회 등 공통 파일 관련 API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "파일", description = "공통 파일 API")
public class FileController {

    @Value("${app.release.base-path:data/release-manager}")
    private String baseReleasePath;

    /**
     * 파일 내용 조회
     *
     * <p>상대 경로를 받아 파일 내용을 반환합니다.
     * 텍스트 파일은 UTF-8 문자열로, 바이너리 파일은 Base64 인코딩하여 반환합니다.
     *
     * @param filePath 파일 경로 (baseReleasePath 기준 상대 경로)
     * @return 파일 내용 응답
     */
    @Operation(
            summary = "파일 내용 조회",
            description = """
                    파일 경로를 받아 파일 내용을 반환합니다.

                    **지원 파일 형식**:
                    - 텍스트 파일: UTF-8 문자열로 반환
                    - 바이너리 파일: Base64 인코딩하여 반환

                    **경로 예시**:
                    - `versions/infraeye2/standard/1.0.x/1.0.0/mariadb/1.patch.sql`
                    - `resources/file/script/MARIADB/backup.sh`
                    - `onboardings/infraeye1/mariadb/init.sql`

                    **최대 파일 크기**: 10MB
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileContentApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파일 경로"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음"
            )
    })
    @GetMapping("/content")
    public ApiResponse<FileContentResponse> getFileContent(
            @Parameter(description = "파일 경로 (baseReleasePath 기준 상대 경로)", required = true,
                    example = "versions/infraeye2/standard/1.0.x/1.0.0/mariadb/1.patch.sql")
            @RequestParam String filePath) {

        log.info("파일 내용 조회 API 호출 - filePath: {}", filePath);

        // 경로 검증 (경로 탐색 공격 방지)
        Path basePath = Paths.get(baseReleasePath);
        Path resolvedPath = FileContentUtil.validateAndResolvePath(basePath, filePath);

        // 파일 내용 읽기
        FileContentUtil.FileContentResult result = FileContentUtil.readFileContent(resolvedPath);

        log.info("파일 내용 조회 완료 - filePath: {}, size: {} bytes, isBinary: {}",
                filePath, result.size(), result.isBinary());

        return ApiResponse.success(new FileContentResponse(
                filePath,
                result.fileName(),
                result.size(),
                result.mimeType(),
                result.isBinary(),
                result.content()
        ));
    }

    /**
     * 파일 다운로드
     *
     * <p>상대 경로를 받아 파일을 다운로드합니다.
     *
     * @param filePath 파일 경로 (baseReleasePath 기준 상대 경로)
     * @param response HTTP 응답
     */
    @Operation(
            summary = "파일 다운로드",
            description = """
                    파일 경로를 받아 파일을 다운로드합니다.

                    **경로 예시**:
                    - `versions/infraeye2/standard/1.0.x/1.0.0/mariadb/1.patch.sql`
                    - `resources/file/script/MARIADB/backup.sh`
                    - `onboardings/infraeye1/mariadb/init.sql`
                    - `resources/publishing/{publishingName}/docs/guide.pdf`
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 - 파일 다운로드"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파일 경로"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음"
            )
    })
    @GetMapping("/download")
    public void downloadFile(
            @Parameter(description = "파일 경로 (baseReleasePath 기준 상대 경로)", required = true,
                    example = "versions/infraeye2/standard/1.0.x/1.0.0/mariadb/1.patch.sql")
            @RequestParam String filePath,
            HttpServletResponse response) throws IOException {

        log.info("파일 다운로드 API 호출 - filePath: {}", filePath);

        // 경로 검증 (경로 탐색 공격 방지)
        Path basePath = Paths.get(baseReleasePath);
        Path resolvedPath = FileContentUtil.validateAndResolvePath(basePath, filePath);

        // 파일명 추출
        String fileName = resolvedPath.getFileName().toString();
        long fileSize = Files.size(resolvedPath);

        // HTTP 응답 헤더 설정
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                HttpFileDownloadUtil.buildContentDisposition(fileName));
        response.setContentLengthLong(fileSize);

        // 파일 스트리밍
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(resolvedPath, out);
            out.flush();
        }

        log.info("파일 다운로드 완료 - filePath: {}, size: {} bytes", filePath, fileSize);
    }

    /**
     * 파일 내용 응답 DTO
     */
    @Schema(description = "파일 내용 응답")
    public record FileContentResponse(
            @Schema(description = "파일 경로", example = "versions/infraeye2/standard/1.0.x/1.0.0/mariadb/1.patch.sql")
            String filePath,

            @Schema(description = "파일명", example = "1.patch.sql")
            String fileName,

            @Schema(description = "파일 크기 (bytes)", example = "1024")
            long size,

            @Schema(description = "MIME 타입", example = "text/plain")
            String mimeType,

            @Schema(description = "바이너리 파일 여부 (true면 content가 Base64 인코딩됨)", example = "false")
            boolean isBinary,

            @Schema(description = "파일 내용 (텍스트 또는 Base64)")
            String content
    ) {
    }

    /**
     * Swagger 스키마용 wrapper 클래스
     */
    @Schema(description = "파일 내용 API 응답", example = """
            {
              "status": "success",
              "data": {
                "filePath": "versions/infraeye2/standard/1.0.x/1.0.0/mariadb/1.patch.sql",
                "fileName": "1.patch.sql",
                "size": 1024,
                "mimeType": "text/plain",
                "isBinary": false,
                "content": "-- SQL Patch Script\\nALTER TABLE users ADD COLUMN..."
              }
            }
            """)
    static class FileContentApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "파일 내용 정보")
        public FileContentResponse data;
    }
}
