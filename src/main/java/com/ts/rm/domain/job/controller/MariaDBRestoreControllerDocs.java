package com.ts.rm.domain.job.controller;

import com.ts.rm.domain.job.dto.JobResponse;
import com.ts.rm.domain.job.dto.MariaDBRestoreRequest;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * MariaDBRestoreController Swagger 문서화 인터페이스
 */
@Tag(name = "작업", description = "작업 관리 API")
@SwaggerResponse
public interface MariaDBRestoreControllerDocs {

    @Operation(
            summary = "MariaDB 복원",
            description = "백업 파일을 사용하여 MariaDB 서버의 데이터베이스를 비동기로 복원합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<JobResponse>> executeRestore(
            @RequestBody MariaDBRestoreRequest request
    );

    @Operation(
            summary = "복원 작업 상태 조회",
            description = "복원 작업의 현재 상태를 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<JobResponse>> getRestoreJobStatus(
            @PathVariable String jobId
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 작업 응답
     */
    @Schema(description = "작업 API 응답")
    class JobApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "작업 정보")
        public JobResponse data;
    }
}
