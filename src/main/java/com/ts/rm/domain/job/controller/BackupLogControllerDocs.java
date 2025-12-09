package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.BackupLogDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * BackupLogController Swagger 문서화 인터페이스
 */
@Tag(name = "작업", description = "작업 관리 API")
@SwaggerResponse
public interface BackupLogControllerDocs {

    @Operation(
            summary = "백업 로그 파일 목록 조회",
            description = """
                    백업 파일과 관련된 로그 파일 목록을 조회합니다.

                    **반환되는 로그 유형**:
                    - `BACKUP`: 백업 파일 생성 시 생성된 로그
                    - `RESTORE`: 해당 백업 파일로 복원을 시도한 로그들

                    **사용 예시**:
                    1. 이 API로 로그 파일 목록을 조회
                    2. 응답의 `logFileName`을 사용하여 `/download` API로 로그 다운로드
                    """,
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LogListApiResponse.class)
                    )
            )
    )
    ApiResponse<BackupLogDto.LogListResponse> getLogFiles(
            @Parameter(description = "백업 파일 ID", example = "1")
            @PathVariable Long id
    );

    @Operation(
            summary = "로그 파일 다운로드",
            description = """
                    백업 파일 관련 로그 파일을 다운로드합니다.

                    **사용 방법**:
                    1. 먼저 `GET /api/jobs/backup-files/{id}/logs` API로 로그 파일 목록 조회
                    2. 응답에서 원하는 로그의 `logFileName` 값 확인
                    3. 해당 값을 `logFileName` 파라미터로 전달하여 다운로드

                    **예시**:
                    ```
                    GET /api/jobs/backup-files/1/logs/download?logFileName=backup_mariadb_20251205_120000.log
                    ```
                    """
    )
    void downloadLogFile(
            @Parameter(description = "백업 파일 ID", example = "1")
            @PathVariable Long id,

            @Parameter(description = "다운로드할 로그 파일명 (로그 목록 조회 API에서 확인)",
                    example = "backup_mariadb_20251205_120000.log")
            @RequestParam String logFileName,

            HttpServletResponse response
    ) throws IOException;

    /**
     * Swagger 스키마용 wrapper 클래스 - 로그 목록 응답
     */
    @Schema(description = "백업 로그 목록 API 응답")
    class LogListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "로그 목록 데이터")
        public BackupLogDto.LogListResponse data;
    }
}
