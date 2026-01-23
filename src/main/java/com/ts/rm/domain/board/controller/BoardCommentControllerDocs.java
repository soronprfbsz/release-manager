package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardCommentDto;
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

/**
 * BoardCommentController Swagger 문서화 인터페이스
 */
@Tag(name = "댓글", description = "게시글 댓글 CRUD 및 좋아요 API")
@SwaggerResponse
public interface BoardCommentControllerDocs {

    @Operation(
            summary = "댓글 목록 조회",
            description = "게시글의 댓글 목록을 페이징하여 조회합니다.\n\n"
                    + "- 최상위 댓글만 조회되며, 대댓글은 각 댓글의 replies에 포함됩니다.\n"
                    + "- 무한 스크롤 적용 가능 (page, size 파라미터 사용)\n"
                    + "- 삭제된 댓글은 조회되지 않습니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardCommentPageApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<Page<BoardCommentDto.Response>>> getComments(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id,

            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "대댓글 목록 조회",
            description = "특정 댓글의 대댓글 목록을 조회합니다.\n\n"
                    + "- 삭제된 대댓글은 조회되지 않습니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardCommentListApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<List<BoardCommentDto.Response>>> getReplies(
            @Parameter(description = "부모 댓글 ID", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "댓글 생성",
            description = "게시글에 댓글을 작성합니다.\n\n"
                    + "- parentCommentId를 지정하면 대댓글로 등록됩니다.\n"
                    + "- parentCommentId가 null이면 최상위 댓글로 등록됩니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardCommentApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<BoardCommentDto.Response>> createComment(
            @Valid @RequestBody BoardCommentDto.CreateRequest request
    );

    @Operation(
            summary = "댓글 수정",
            description = "기존 댓글을 수정합니다.\n\n"
                    + "**권한**: ADMIN 역할이거나 작성자만 수정 가능",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardCommentApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<BoardCommentDto.Response>> updateComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody BoardCommentDto.UpdateRequest request
    );

    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다.\n\n"
                    + "**권한**: ADMIN 역할이거나 작성자만 삭제 가능\n\n"
                    + "- 대댓글이 있는 경우 소프트 삭제 (내용만 삭제됨)\n"
                    + "- 대댓글이 없는 경우 물리 삭제",
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
    ResponseEntity<ApiResponse<Void>> deleteComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "댓글 좋아요 토글",
            description = "댓글에 좋아요를 추가하거나 취소합니다.\n\n"
                    + "- 이미 좋아요한 상태면 취소됩니다.\n"
                    + "- 응답의 `data`가 `true`면 좋아요 추가, `false`면 좋아요 취소",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardCommentLikeApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<Boolean>> toggleCommentLike(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long id
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 댓글 응답
     */
    @Schema(description = "댓글 API 응답")
    class BoardCommentApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "댓글 정보")
        public BoardCommentDto.Response data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 댓글 목록 응답
     */
    @Schema(description = "댓글 목록 API 응답")
    class BoardCommentListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "댓글 목록")
        public List<BoardCommentDto.Response> data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 댓글 페이지 응답
     */
    @Schema(description = "댓글 페이지 API 응답")
    class BoardCommentPageApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "댓글 페이지")
        public Page<BoardCommentDto.Response> data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 좋아요 응답
     */
    @Schema(description = "좋아요 API 응답")
    class BoardCommentLikeApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "좋아요 여부 (true: 좋아요 추가, false: 좋아요 취소)", example = "true")
        public Boolean data;
    }
}
