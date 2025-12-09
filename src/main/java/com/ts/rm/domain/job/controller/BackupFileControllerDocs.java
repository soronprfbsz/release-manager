package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.BackupFileDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * BackupFileController Swagger 문서화 인터페이스
 */
@Tag(name = "작업", description = "작업 관리 API")
@SwaggerResponse
public interface BackupFileControllerDocs {

    @Operation(
            summary = "백업 파일 목록 조회",
            description = "백업 파일 목록을 검색 조건과 페이징으로 조회합니다.\n\n"
                    + "**검색 조건**:\n"
                    + "- `fileCategory`: 파일 카테고리 (MARIADB, CRATEDB)\n"
                    + "- `fileType`: 파일 타입 (확장자 대문자, 예: SQL, GZ, ZIP)\n"
                    + "- `fileName`: 파일명 (부분 일치)",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BackupFileListResponse.class)
                    )
            )
    )
    ApiResponse<Page<BackupFileDto.ListResponse>> listBackupFiles(
            @Parameter(description = "파일 카테고리 (MARIADB, CRATEDB)")
            @RequestParam(required = false) String fileCategory,

            @Parameter(description = "파일 타입 (예: SQL, GZ, ZIP)")
            @RequestParam(required = false) String fileType,

            @Parameter(description = "파일명 (부분 일치)")
            @RequestParam(required = false) String fileName,

            Pageable pageable
    );

    @Operation(
            summary = "백업 파일 다운로드",
            description = "백업 파일을 다운로드합니다."
    )
    void downloadBackupFile(
            @PathVariable Long id,
            HttpServletResponse response
    ) throws IOException;

    @Operation(
            summary = "백업 파일 삭제",
            description = "백업 파일을 삭제합니다.\n\n"
                    + "**삭제 범위**:\n"
                    + "- DB 레코드 (backup_file 테이블)\n"
                    + "- 실제 파일\n\n"
                    + "**주의사항**:\n"
                    + "- 삭제된 데이터는 복구할 수 없습니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"status\": \"success\", \"data\": null}"
                            )
                    )
            )
    )
    ApiResponse<Void> deleteBackupFile(@PathVariable Long id);

    /**
     * Swagger 스키마용 wrapper 클래스 - 백업 파일 목록 응답
     */
    @Schema(description = "백업 파일 목록 API 응답")
    class BackupFileListResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "페이징된 백업 파일 목록")
        public PageData data;

        @Schema(description = "페이지 데이터")
        static class PageData {
            @Schema(description = "백업 파일 목록")
            public List<BackupFileDto.ListResponse> content;

            @Schema(description = "전체 페이지 수", example = "1")
            public int totalPages;

            @Schema(description = "전체 요소 수", example = "5")
            public long totalElements;

            @Schema(description = "페이지 크기", example = "20")
            public int size;

            @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
            public int number;
        }
    }
}
