package com.ts.rm.domain.resourcefile.controller;

import com.ts.rm.domain.resourcefile.dto.ResourceFileDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * ResourceFileController Swagger 문서화 인터페이스
 */
@Tag(name = "리소스 파일", description = "리소스 파일(스크립트, 문서) 관리 API")
@SwaggerResponse
public interface ResourceFileControllerDocs {

    @Operation(
            summary = "리소스 파일 업로드",
            description = "스크립트, Docker, 문서 등 리소스 파일을 업로드합니다.\n\n"
                    + "**파일 카테고리 (fileCategory)**:\n"
                    + "- `SCRIPT`: 스크립트 파일\n"
                    + "- `DOCKER`: Docker 관련 파일\n"
                    + "- `DOCUMENT`: 문서 파일\n"
                    + "- `ETC`: 기타 파일\n\n"
                    + "**하위 카테고리 (subCategory)**:\n"
                    + "- SCRIPT: MARIADB, CRATEDB, ETC\n"
                    + "- DOCKER: SERVICE, DOCKERFILE, ETC\n"
                    + "- DOCUMENT: INFRAEYE1, INFRAEYE2, ETC\n"
                    + "- ETC: ETC",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceFileDetailApiResponse.class)
                    )
            )
    )
    ApiResponse<ResourceFileDto.DetailResponse> uploadFile(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authorizationHeader,

            @Parameter(description = "업로드할 파일", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "파일 카테고리 (SCRIPT/DOCKER/DOCUMENT/ETC)", required = true, example = "SCRIPT")
            @RequestParam String fileCategory,

            @Parameter(description = "하위 카테고리 (예: MARIADB, INFRAEYE2)", example = "MARIADB")
            @RequestParam(required = false) String subCategory,

            @Parameter(description = "리소스 파일 관리용 이름", required = true, example = "MariaDB 백업 스크립트 v1.0")
            @RequestParam String resourceFileName,

            @Parameter(description = "파일 설명", example = "MariaDB 백업 스크립트")
            @RequestParam(required = false) String description
    );

    @Operation(
            summary = "리소스 파일 상세 조회",
            description = "리소스 파일 ID로 상세 정보를 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceFileDetailApiResponse.class)
                    )
            )
    )
    ApiResponse<ResourceFileDto.DetailResponse> getResourceFile(
            @PathVariable Long id
    );

    @Operation(
            summary = "리소스 파일 수정",
            description = "기존 리소스 파일의 메타데이터 정보를 수정합니다.\n\n"
                    + "**수정 가능 항목**:\n"
                    + "- 파일 카테고리 (SCRIPT/DOCKER/DOCUMENT/ETC)\n"
                    + "- 하위 카테고리\n"
                    + "- 리소스 파일 관리용 이름\n"
                    + "- 파일 설명\n\n"
                    + "**주의사항**:\n"
                    + "- 실제 파일 자체는 수정되지 않으며, 메타데이터만 수정됩니다\n"
                    + "- 파일명, 파일 경로 등은 수정할 수 없습니다",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceFileDetailApiResponse.class)
                    )
            )
    )
    ApiResponse<ResourceFileDto.DetailResponse> updateFile(
            @Parameter(description = "리소스 파일 ID", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "리소스 파일 수정 요청", required = true)
            @RequestBody @Valid ResourceFileDto.UpdateRequest request
    );

    @Operation(
            summary = "리소스 파일 목록 조회",
            description = "리소스 파일 목록을 조회합니다.\n\n"
                    + "**필터링**:\n"
                    + "- `fileCategory` 파라미터로 파일 카테고리별 필터링 가능 (SCRIPT/DOCKER/DOCUMENT/ETC)\n"
                    + "- 생략 시 전체 목록 반환",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceFileListApiResponse.class)
                    )
            )
    )
    ApiResponse<List<ResourceFileDto.SimpleResponse>> listResourceFiles(
            @Parameter(description = "파일 카테고리 필터 (SCRIPT/DOCKER/DOCUMENT/ETC)")
            @RequestParam(required = false) String fileCategory
    );

    @Operation(
            summary = "리소스 파일 분류 가이드 조회",
            description = "리소스 파일 업로드 시 사용 가능한 카테고리 및 하위 카테고리 목록을 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CategoryGuideListApiResponse.class)
                    )
            )
    )
    ApiResponse<List<ResourceFileDto.CategoryGuideResponse>> getCategoryGuide();

    @Operation(
            summary = "리소스 파일 다운로드",
            description = "리소스 파일을 다운로드합니다."
    )
    void downloadResourceFile(
            @PathVariable Long id,
            HttpServletResponse response
    ) throws IOException;

    @Operation(
            summary = "리소스 파일 삭제",
            description = "리소스 파일을 삭제합니다.\n\n"
                    + "**삭제 범위**:\n"
                    + "- DB 레코드 (resource_file 테이블)\n"
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
    ApiResponse<Void> deleteResourceFile(@PathVariable Long id);

    @Operation(
            summary = "리소스 파일 순서 변경",
            description = "특정 파일 카테고리(fileCategory) 내에서 리소스 파일의 정렬 순서를 변경합니다.\n\n"
                    + "**동작 방식**:\n"
                    + "- 동일한 fileCategory에 속한 리소스 파일들의 순서만 변경 가능\n"
                    + "- 요청받은 리소스 파일 ID 순서대로 sortOrder를 1부터 재부여\n\n"
                    + "**요청 예시**:\n"
                    + "```json\n"
                    + "{\n"
                    + "  \"fileCategory\": \"SCRIPT\",\n"
                    + "  \"resourceFileIds\": [3, 1, 2, 4]\n"
                    + "}\n"
                    + "```\n\n"
                    + "**주의사항**:\n"
                    + "- 모든 리소스 파일 ID가 존재해야 합니다\n"
                    + "- 모든 리소스 파일이 지정한 fileCategory에 속해야 합니다\n"
                    + "- 빈 목록은 허용되지 않습니다",
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
    ApiResponse<Void> reorderResourceFiles(
            @Parameter(description = "순서 변경 요청", required = true)
            @RequestBody ResourceFileDto.ReorderResourceFilesRequest request
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 리소스 파일 상세 응답
     */
    @Schema(description = "리소스 파일 상세 API 응답")
    class ResourceFileDetailApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "리소스 파일 상세 정보")
        public ResourceFileDto.DetailResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 리소스 파일 목록 응답
     */
    @Schema(description = "리소스 파일 목록 API 응답")
    class ResourceFileListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "리소스 파일 목록")
        public List<ResourceFileDto.SimpleResponse> data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 카테고리 가이드 목록 응답
     */
    @Schema(description = "카테고리 가이드 목록 API 응답")
    class CategoryGuideListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "카테고리 가이드 목록")
        public List<ResourceFileDto.CategoryGuideResponse> data;
    }
}
