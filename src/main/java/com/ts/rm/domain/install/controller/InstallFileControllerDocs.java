package com.ts.rm.domain.install.controller;

import com.ts.rm.domain.install.dto.InstallFileDto;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Install File Controller API 문서 (Swagger)
 */
@Tag(name = "Install File", description = "프로젝트별 인스톨 파일 관리 API")
public interface InstallFileControllerDocs {

    @Operation(summary = "인스톨 파일 트리 조회", description = "프로젝트별 인스톨 파일 트리를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = InstallFileDto.FilesResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<InstallFileDto.FilesResponse>> getInstallFiles(
            @Parameter(description = "프로젝트 ID", example = "infraeye2") String id);

    @Operation(summary = "인스톨 디렉토리 생성", description = "프로젝트별 인스톨 디렉토리를 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = InstallFileDto.DirectoryResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 경로")
    })
    ResponseEntity<ApiResponse<InstallFileDto.DirectoryResponse>> createDirectory(
            @Parameter(description = "프로젝트 ID", example = "infraeye2") String id,
            @Parameter(description = "생성할 디렉토리 경로", example = "/mariadb/scripts") String path);

    @Operation(summary = "인스톨 파일 업로드", description = "프로젝트별 인스톨 파일을 업로드합니다. ZIP 파일은 압축 해제 여부를 선택할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = InstallFileDto.UploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResponse<InstallFileDto.UploadResponse>> uploadFile(
            @Parameter(description = "프로젝트 ID", example = "infraeye2") String id,
            @Parameter(description = "업로드할 파일") MultipartFile file,
            @Parameter(description = "대상 경로 (선택)", example = "/mariadb") String targetPath,
            @Parameter(description = "ZIP 파일 압축 해제 여부 (기본값: true)") Boolean extractZip);

    @Operation(summary = "인스톨 파일 삭제", description = "프로젝트별 인스톨 파일 또는 디렉토리를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = InstallFileDto.DeleteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<InstallFileDto.DeleteResponse>> deleteFile(
            @Parameter(description = "프로젝트 ID", example = "infraeye2") String id,
            @Parameter(description = "삭제할 파일/디렉토리 경로", example = "/mariadb/init.sql") String filePath);

    @Operation(summary = "인스톨 전체 파일 ZIP 다운로드", description = "프로젝트별 인스톨 파일 전체를 ZIP으로 다운로드합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    void downloadAllFiles(
            @Parameter(description = "프로젝트 ID", example = "infraeye2") String id,
            HttpServletResponse response) throws IOException;
}
