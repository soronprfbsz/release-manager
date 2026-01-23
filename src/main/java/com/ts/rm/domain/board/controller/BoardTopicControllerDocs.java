package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardTopicDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * BoardTopicController Swagger 문서화 인터페이스
 */
@Tag(name = "게시판 토픽", description = "게시판 토픽(카테고리) 조회 API")
@SwaggerResponse
public interface BoardTopicControllerDocs {

    @Operation(
            summary = "토픽 목록 조회",
            description = "활성화된 게시판 토픽 목록을 정렬 순서대로 조회합니다.\n\n"
                    + "토픽 예시: 공지사항, QnA, 개선제안 등",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardTopicListApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<BoardTopicDto.ListResponse>>> getTopics();

    @Operation(
            summary = "토픽 상세 조회",
            description = "특정 토픽의 상세 정보를 조회합니다.\n\n"
                    + "게시글 수 등 추가 정보가 포함됩니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardTopicApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<BoardTopicDto.Response>> getTopic(
            @Parameter(description = "토픽 ID", required = true, example = "QNA")
            @PathVariable String id
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 토픽 응답
     */
    @Schema(description = "토픽 API 응답")
    class BoardTopicApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "토픽 정보")
        public BoardTopicDto.Response data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 토픽 목록 응답
     */
    @Schema(description = "토픽 목록 API 응답")
    class BoardTopicListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "토픽 목록")
        public List<BoardTopicDto.ListResponse> data;
    }
}
