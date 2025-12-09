package com.ts.rm.domain.engineer.controller;

import com.ts.rm.domain.engineer.dto.EngineerDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * EngineerController Swagger 문서화 인터페이스
 */
@Tag(name = "엔지니어", description = "엔지니어 관리 API")
@SwaggerResponse
public interface EngineerControllerDocs {

    @Operation(
            summary = "엔지니어 생성",
            description = "새로운 엔지니어를 등록합니다. Authorization 헤더에 JWT 토큰 필수 (Bearer {token})",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EngineerDetailApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> createEngineer(
            @Valid @RequestBody EngineerDto.CreateRequest request
    );

    @Operation(
            summary = "엔지니어 조회 (ID)",
            description = "ID로 엔지니어 정보를 조회합니다",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EngineerDetailApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> getEngineerById(
            @Parameter(description = "엔지니어 ID", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "엔지니어 목록 조회",
            description = "엔지니어 목록을 조회합니다. departmentId로 부서 필터링, keyword로 이름 검색 가능. page, size, sort 파라미터 사용 가능",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EngineerListApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<Page<EngineerDto.ListResponse>>> getEngineers(
            @Parameter(description = "부서 ID 필터")
            @RequestParam(required = false) Long departmentId,

            @Parameter(description = "이름 검색 키워드")
            @RequestParam(required = false) String keyword,

            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "엔지니어 정보 수정",
            description = "엔지니어 정보를 수정합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EngineerDetailApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<EngineerDto.DetailResponse>> updateEngineer(
            @Parameter(description = "엔지니어 ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody EngineerDto.UpdateRequest request
    );

    @Operation(
            summary = "엔지니어 삭제",
            description = "엔지니어를 삭제합니다",
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
    ResponseEntity<ApiResponse<Void>> deleteEngineer(
            @Parameter(description = "엔지니어 ID", required = true)
            @PathVariable Long id
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 엔지니어 상세 응답
     */
    @Schema(description = "엔지니어 상세 API 응답")
    class EngineerDetailApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "엔지니어 상세 정보")
        public EngineerDto.DetailResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 엔지니어 목록 응답
     */
    @Schema(description = "엔지니어 목록 API 응답")
    class EngineerListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "페이징된 엔지니어 목록")
        public PageData data;

        @Schema(description = "페이지 데이터")
        static class PageData {
            @Schema(description = "엔지니어 목록")
            public List<EngineerDto.ListResponse> content;

            @Schema(description = "전체 페이지 수", example = "1")
            public int totalPages;

            @Schema(description = "전체 요소 수", example = "10")
            public long totalElements;

            @Schema(description = "페이지 크기", example = "10")
            public int size;

            @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
            public int number;
        }
    }
}
