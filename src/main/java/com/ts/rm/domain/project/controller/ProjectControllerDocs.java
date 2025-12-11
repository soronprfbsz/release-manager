package com.ts.rm.domain.project.controller;

import com.ts.rm.domain.project.dto.ProjectDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ProjectController Swagger 문서화 인터페이스
 */
@Tag(name = "프로젝트", description = "프로젝트 관리 API")
@SwaggerResponse
public interface ProjectControllerDocs {

    @Operation(
            summary = "프로젝트 생성",
            description = "새로운 프로젝트를 생성합니다",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectDetailApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> createProject(
            @Valid @RequestBody ProjectDto.CreateRequest request
    );

    @Operation(
            summary = "프로젝트 조회",
            description = "ID로 프로젝트 정보를 조회합니다",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectDetailApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> getProjectById(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable String id
    );

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "전체 프로젝트 목록을 조회합니다",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectListApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<ProjectDto.DetailResponse>>> getAllProjects();

    @Operation(
            summary = "프로젝트 수정",
            description = "프로젝트 정보를 수정합니다 (프로젝트 ID는 변경 불가)",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProjectDetailApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<ProjectDto.DetailResponse>> updateProject(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable String id,

            @Valid @RequestBody ProjectDto.UpdateRequest request
    );

    @Operation(
            summary = "프로젝트 삭제",
            description = "프로젝트를 삭제합니다. 연관된 릴리즈 버전/패치가 있으면 삭제할 수 없습니다",
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
    ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable String id
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 프로젝트 상세 응답
     */
    @Schema(description = "프로젝트 상세 API 응답", example = """
            {
              "status": "success",
              "data": {
                "projectId": "infraeye1",
                "projectName": "Infraeye 1",
                "description": "Infraeye 1.0 - 레거시 NMS 솔루션",
                "createdAt": "2025-01-15T10:30:00",
                "createdBy": "SYSTEM"
              }
            }
            """)
    class ProjectDetailApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "프로젝트 상세 정보")
        public ProjectDto.DetailResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 프로젝트 목록 응답
     */
    @Schema(description = "프로젝트 목록 API 응답", example = """
            {
              "status": "success",
              "data": [
                {
                  "projectId": "infraeye1",
                  "projectName": "Infraeye 1",
                  "description": "Infraeye 1.0 - 레거시 NMS 솔루션",
                  "createdAt": "2025-01-15T10:30:00",
                  "createdBy": "SYSTEM"
                },
                {
                  "projectId": "infraeye2",
                  "projectName": "Infraeye 2",
                  "description": "Infraeye 2.0 - 신규 NMS 솔루션",
                  "createdAt": "2025-01-16T09:00:00",
                  "createdBy": "SYSTEM"
                }
              ]
            }
            """)
    class ProjectListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "프로젝트 목록")
        public List<ProjectDto.DetailResponse> data;
    }
}
