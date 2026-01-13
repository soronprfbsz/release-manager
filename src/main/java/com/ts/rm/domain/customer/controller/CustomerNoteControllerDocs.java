package com.ts.rm.domain.customer.controller;

import com.ts.rm.domain.customer.dto.CustomerNoteDto;
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
 * CustomerNoteController Swagger 문서화 인터페이스
 */
@Tag(name = "고객사 특이사항", description = "고객사 특이사항(메모) 관리 API")
@SwaggerResponse
public interface CustomerNoteControllerDocs {

    @Operation(
            summary = "특이사항 목록 조회",
            description = "고객사의 특이사항 목록을 최신순으로 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerNoteListApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<CustomerNoteDto.Response>>> getNotes(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable Long customerId
    );

    @Operation(
            summary = "특이사항 생성",
            description = "고객사에 새로운 특이사항을 등록합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerNoteApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<CustomerNoteDto.Response>> createNote(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable Long customerId,

            @Valid @RequestBody CustomerNoteDto.CreateRequest request
    );

    @Operation(
            summary = "특이사항 수정",
            description = "기존 특이사항을 수정합니다.\n\n"
                    + "**권한**: ADMIN 역할이거나 작성자만 수정 가능",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerNoteApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<CustomerNoteDto.Response>> updateNote(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable Long customerId,

            @Parameter(description = "특이사항 ID", required = true)
            @PathVariable Long noteId,

            @Valid @RequestBody CustomerNoteDto.UpdateRequest request
    );

    @Operation(
            summary = "특이사항 삭제",
            description = "특이사항을 삭제합니다.\n\n"
                    + "**권한**: ADMIN 역할이거나 작성자만 삭제 가능",
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
    ResponseEntity<ApiResponse<Void>> deleteNote(
            @Parameter(description = "고객사 ID", required = true)
            @PathVariable Long customerId,

            @Parameter(description = "특이사항 ID", required = true)
            @PathVariable Long noteId
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 특이사항 응답
     */
    @Schema(description = "특이사항 API 응답")
    class CustomerNoteApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "특이사항 정보")
        public CustomerNoteDto.Response data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 특이사항 목록 응답
     */
    @Schema(description = "특이사항 목록 API 응답")
    class CustomerNoteListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "특이사항 목록")
        public List<CustomerNoteDto.Response> data;
    }
}
