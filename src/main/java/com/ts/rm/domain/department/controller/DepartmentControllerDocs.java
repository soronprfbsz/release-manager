package com.ts.rm.domain.department.controller;

import com.ts.rm.domain.department.dto.DepartmentDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

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

    /**
     * Swagger 스키마용 wrapper 클래스 - 부서 목록
     */
    @Schema(description = "부서 목록 API 응답")
    class DepartmentListResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "부서 목록")
        public List<DepartmentDto.Response> data;
    }
}
