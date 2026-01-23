package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardPostDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * BoardPostController Swagger 문서화 인터페이스
 */
@Tag(name = "게시글", description = "게시글 CRUD 및 좋아요 API")
@SwaggerResponse
public interface BoardPostControllerDocs {

    @Operation(
            summary = "게시글 목록 조회",
            description = "게시글 목록을 페이징하여 조회합니다.\n\n"
                    + "- 상단 고정 게시글이 먼저 표시됩니다.\n"
                    + "- 토픽별 필터링 및 키워드 검색을 지원합니다.\n"
                    + "- 무한 스크롤 적용 가능 (page, size 파라미터 사용)\n\n"
                    + "**정렬 가능 필드**: postId, title, viewCount, likeCount, commentCount, createdAt",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardPostPageApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<Page<BoardPostDto.ListResponse>>> getPosts(
            @Parameter(description = "토픽 ID (null이면 전체)", example = "QNA")
            @RequestParam(required = false) String topicId,

            @Parameter(description = "검색 키워드 (제목, 내용 검색)")
            @RequestParam(required = false) String keyword,

            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "게시글 상세 조회",
            description = "게시글의 상세 정보를 조회합니다.\n\n"
                    + "- 조회 시 조회수가 자동으로 증가합니다.\n"
                    + "- 이슈 트래킹 토픽의 경우 이슈 정보가 포함됩니다.\n"
                    + "- 현재 사용자의 좋아요 여부가 포함됩니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardPostApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<BoardPostDto.Response>> getPost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "게시글 생성",
            description = "새 게시글을 작성합니다.\n\n"
                    + "- 마크다운 본문을 지원합니다.\n"
                    + "- 이슈 트래킹 토픽의 경우 이슈 정보를 함께 등록할 수 있습니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "생성됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardPostApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<BoardPostDto.Response>> createPost(
            @Valid @RequestBody BoardPostDto.CreateRequest request
    );

    @Operation(
            summary = "게시글 수정",
            description = "기존 게시글을 수정합니다.\n\n"
                    + "**권한**: ADMIN 역할이거나 작성자만 수정 가능",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardPostApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<BoardPostDto.Response>> updatePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody BoardPostDto.UpdateRequest request
    );

    @Operation(
            summary = "게시글 삭제",
            description = "게시글을 삭제합니다.\n\n"
                    + "**권한**: ADMIN 역할이거나 작성자만 삭제 가능\n\n"
                    + "관련 댓글, 좋아요도 함께 삭제됩니다.",
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
    ResponseEntity<ApiResponse<Void>> deletePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "게시글 좋아요 토글",
            description = "게시글에 좋아요를 추가하거나 취소합니다.\n\n"
                    + "- 이미 좋아요한 상태면 취소됩니다.\n"
                    + "- 응답의 `data`가 `true`면 좋아요 추가, `false`면 좋아요 취소",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardPostLikeApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<Boolean>> togglePostLike(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long id
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 게시글 응답
     */
    @Schema(description = "게시글 API 응답")
    class BoardPostApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "게시글 정보")
        public BoardPostDto.Response data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 게시글 페이지 응답
     */
    @Schema(description = "게시글 페이지 API 응답")
    class BoardPostPageApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "게시글 페이지")
        public Page<BoardPostDto.ListResponse> data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 좋아요 응답
     */
    @Schema(description = "좋아요 API 응답")
    class BoardPostLikeApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "좋아요 여부 (true: 좋아요 추가, false: 좋아요 취소)", example = "true")
        public Boolean data;
    }
}
