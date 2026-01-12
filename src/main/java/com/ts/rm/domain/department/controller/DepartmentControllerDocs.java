package com.ts.rm.domain.department.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.ts.rm.domain.department.dto.DepartmentDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * DepartmentController Swagger 문서화 인터페이스
 */
@Tag(name = "부서", description = "부서 관리 API")
@SwaggerResponse
public interface DepartmentControllerDocs {

    @Operation(
            summary = "부서 목록 조회",
            description = "전체 부서 목록을 조회합니다. 셀렉트 박스 등에서 활용 가능합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentListResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> getAllDepartments();

    @Operation(
            summary = "부서 트리 조회",
            description = "부서 계층 구조를 트리 형태로 조회합니다. 조직도 등에서 활용 가능합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentTreeListResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<DepartmentDto.TreeResponse>>> getDepartmentTree();

    @Operation(
            summary = "부서 상세 조회",
            description = "부서 ID로 부서 상세 정보를 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentDetailResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<DepartmentDto.DetailResponse>> getDepartmentById(
            @Parameter(description = "부서 ID", required = true) Long id);

    @Operation(
            summary = "직계 하위 부서 조회",
            description = "특정 부서의 직계 하위 부서 목록을 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentListResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> getChildDepartments(
            @Parameter(description = "부서 ID", required = true) Long id);

    @Operation(
            summary = "모든 하위 부서 조회",
            description = "특정 부서의 모든 하위 부서 목록을 조회합니다 (손자 포함).",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentListResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<DepartmentDto.Response>>> getDescendantDepartments(
            @Parameter(description = "부서 ID", required = true) Long id);

    @Operation(
            summary = "부서 생성",
            description = "새 부서를 생성합니다. parentDepartmentId가 null이면 루트 부서 하위로 생성됩니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<DepartmentDto.Response>> createDepartment(
            DepartmentDto.CreateRequest request);

    @Operation(
            summary = "부서 수정",
            description = "부서 정보를 수정합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<DepartmentDto.Response>> updateDepartment(
            @Parameter(description = "부서 ID", required = true) Long id,
            DepartmentDto.UpdateRequest request);

    @Operation(
            summary = "부서 이동",
            description = "부서를 다른 상위 부서 하위로 이동합니다. newParentId가 null이면 루트 부서 하위로 이동합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DepartmentResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<DepartmentDto.Response>> moveDepartment(
            @Parameter(description = "부서 ID", required = true) Long id,
            DepartmentDto.MoveRequest request);

    @Operation(
            summary = "부서 삭제",
            description = "부서를 삭제합니다. 하위 부서나 소속 계정이 있으면 삭제할 수 없습니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @Parameter(description = "부서 ID", required = true) Long id);

    // ========================================
    // Swagger 스키마용 wrapper 클래스
    // ========================================

    @Schema(description = "부서 목록 API 응답")
    class DepartmentListResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "부서 목록")
        public List<DepartmentDto.Response> data;
    }

    @Schema(description = "부서 트리 API 응답")
    class DepartmentTreeListResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "부서 트리")
        public List<DepartmentDto.TreeResponse> data;
    }

    @Schema(description = "부서 상세 API 응답")
    class DepartmentDetailResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "부서 상세")
        public DepartmentDto.DetailResponse data;
    }

    @Schema(description = "부서 API 응답")
    class DepartmentResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "부서")
        public DepartmentDto.Response data;
    }
}
