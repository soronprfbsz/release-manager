package com.ts.rm.domain.dashboard.controller;

import com.ts.rm.domain.dashboard.dto.DashboardDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * DashboardController Swagger 문서화 인터페이스
 */
@Tag(name = "대시보드", description = "대시보드 API")
@SwaggerResponse
public interface DashboardControllerDocs {

    @Operation(
            summary = "대시보드 최근 데이터 조회",
            description = "프로젝트별 최신 설치본 1개, 최근 릴리즈 버전 N개, 최근 생성 패치 N개를 조회합니다.\n\n"
                    + "**파라미터**:\n"
                    + "- `versionLimit`: 최근 릴리즈 버전 조회 개수 (기본값: 3)\n"
                    + "- `patchLimit`: 최근 생성 패치 조회 개수 (기본값: 3)",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DashboardResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<DashboardDto.Response>> getRecentData(
            @Parameter(description = "프로젝트 ID", required = true, example = "infraeye2")
            @PathVariable String projectId,

            @Parameter(description = "최근 릴리즈 버전 조회 개수", example = "3")
            @RequestParam(defaultValue = "3") int versionLimit,

            @Parameter(description = "최근 생성 패치 조회 개수", example = "3")
            @RequestParam(defaultValue = "3") int patchLimit
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 대시보드 응답
     */
    @Schema(description = "대시보드 API 응답")
    class DashboardResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "대시보드 데이터")
        public DashboardDto.Response data;
    }
}
