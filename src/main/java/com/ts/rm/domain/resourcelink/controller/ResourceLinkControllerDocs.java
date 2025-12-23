package com.ts.rm.domain.resourcelink.controller;

import com.ts.rm.domain.resourcelink.dto.ResourceLinkDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ResourceLinkController Swagger 문서화 인터페이스
 */
@Tag(name = "리소스 링크", description = "리소스 링크(구글시트, 노션 등) 관리 API")
@SwaggerResponse
public interface ResourceLinkControllerDocs {

    @Operation(
            summary = "리소스 링크 생성",
            description = "구글 시트, 노션 등 외부 리소스 링크를 등록합니다.\n\n"
                    + "**링크 카테고리 (linkCategory)**:\n"
                    + "- `DOCUMENT`: 문서 링크 (구글 시트, 노션 등)\n"
                    + "- `TOOL`: 개발 도구 링크\n"
                    + "- `ETC`: 기타 링크\n\n"
                    + "**하위 카테고리 (subCategory)**:\n"
                    + "- DOCUMENT: INFRAEYE1, INFRAEYE2, ETC\n"
                    + "- TOOL: DEV_TOOL, DESIGN_TOOL, ETC\n"
                    + "- ETC: ETC",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceLinkDetailApiResponse.class)
                    )
            )
    )
    ApiResponse<ResourceLinkDto.DetailResponse> createLink(
            @Parameter(description = "리소스 링크 생성 요청", required = true)
            @RequestBody @Valid ResourceLinkDto.CreateRequest request
    );

    @Operation(
            summary = "리소스 링크 수정",
            description = "기존 리소스 링크 정보를 수정합니다.\n\n"
                    + "**수정 가능 항목**:\n"
                    + "- 링크 카테고리\n"
                    + "- 하위 카테고리\n"
                    + "- 링크 이름\n"
                    + "- 링크 주소\n"
                    + "- 링크 설명",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceLinkDetailApiResponse.class)
                    )
            )
    )
    ApiResponse<ResourceLinkDto.DetailResponse> updateLink(
            @Parameter(description = "리소스 링크 ID", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "리소스 링크 수정 요청", required = true)
            @RequestBody @Valid ResourceLinkDto.UpdateRequest request
    );

    @Operation(
            summary = "리소스 링크 상세 조회",
            description = "리소스 링크 ID로 상세 정보를 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceLinkDetailApiResponse.class)
                    )
            )
    )
    ApiResponse<ResourceLinkDto.DetailResponse> getResourceLink(
            @Parameter(description = "리소스 링크 ID", required = true, example = "1")
            @PathVariable Long id
    );

    @Operation(
            summary = "리소스 링크 목록 조회",
            description = "리소스 링크 목록을 조회합니다.\n\n"
                    + "**필터링**:\n"
                    + "- `linkCategory` 파라미터로 링크 카테고리별 필터링 가능 (DOCUMENT/TOOL/ETC)\n"
                    + "- 생략 시 전체 목록 반환",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceLinkListApiResponse.class)
                    )
            )
    )
    ApiResponse<List<ResourceLinkDto.SimpleResponse>> listResourceLinks(
            @Parameter(description = "링크 카테고리 필터 (DOCUMENT/TOOL/ETC)")
            @RequestParam(required = false) String linkCategory,

            @Parameter(description = "검색 키워드 (링크명, 링크URL, 설명 통합 검색)")
            @RequestParam(required = false) String keyword
    );

    @Operation(
            summary = "리소스 링크 삭제",
            description = "리소스 링크를 삭제합니다.\n\n"
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
    ApiResponse<Void> deleteResourceLink(
            @Parameter(description = "리소스 링크 ID", required = true, example = "1")
            @PathVariable Long id
    );

    @Operation(
            summary = "리소스 링크 순서 변경",
            description = "특정 링크 카테고리(linkCategory) 내에서 리소스 링크의 정렬 순서를 변경합니다.\n\n"
                    + "**동작 방식**:\n"
                    + "- 동일한 linkCategory에 속한 리소스 링크들의 순서만 변경 가능\n"
                    + "- 요청받은 리소스 링크 ID 순서대로 sortOrder를 1부터 재부여\n\n"
                    + "**요청 예시**:\n"
                    + "```json\n"
                    + "{\n"
                    + "  \"linkCategory\": \"DOCUMENT\",\n"
                    + "  \"resourceLinkIds\": [3, 1, 2, 4]\n"
                    + "}\n"
                    + "```\n\n"
                    + "**주의사항**:\n"
                    + "- 모든 리소스 링크 ID가 존재해야 합니다\n"
                    + "- 모든 리소스 링크가 지정한 linkCategory에 속해야 합니다\n"
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
    ApiResponse<Void> reorderResourceLinks(
            @Parameter(description = "순서 변경 요청", required = true)
            @RequestBody @Valid ResourceLinkDto.ReorderResourceLinksRequest request
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 리소스 링크 상세 응답
     */
    @Schema(description = "리소스 링크 상세 API 응답")
    class ResourceLinkDetailApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "리소스 링크 상세 정보")
        public ResourceLinkDto.DetailResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 리소스 링크 목록 응답
     */
    @Schema(description = "리소스 링크 목록 API 응답")
    class ResourceLinkListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "리소스 링크 목록")
        public List<ResourceLinkDto.SimpleResponse> data;
    }
}
