package com.ts.rm.domain.common.controller;

import com.ts.rm.domain.common.dto.CodeDto;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * CodeController Swagger 문서화 인터페이스
 */
@Tag(name = "코드", description = "공통 코드 API")
public interface CodeControllerDocs {

    @Operation(
            summary = "코드 타입별 코드 목록 조회",
            description = "특정 코드 타입의 활성화된 코드 목록을 정렬 순서대로 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CodeListResponse.class)
            )
    )
    ResponseEntity<ApiResponse<List<CodeDto.SimpleResponse>>> getCodesByType(
            @Parameter(description = "코드 타입 ID (예: RELEASE_CATEGORY, FILE_CATEGORY, DATABASE_TYPE 등)", example = "RELEASE_CATEGORY")
            @PathVariable String codeTypeId
    );

    /**
     * Swagger 스키마용 wrapper 클래스
     */
    @Schema(description = "코드 목록 API 응답")
    class CodeListResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "코드 목록")
        public List<CodeDto.SimpleResponse> data;
    }
}
