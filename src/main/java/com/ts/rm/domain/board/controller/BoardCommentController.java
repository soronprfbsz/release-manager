package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardCommentDto;
import com.ts.rm.domain.board.service.BoardCommentService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import com.ts.rm.global.security.TokenInfo;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BoardComment Controller
 *
 * <p>게시글 댓글 CRUD 및 좋아요 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardCommentController implements BoardCommentControllerDocs {

    private final BoardCommentService commentService;

    /**
     * 댓글 목록 조회 (페이징)
     *
     * @param id       게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    @Override
    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<ApiResponse<Page<BoardCommentDto.Response>>> getComments(
            @PathVariable Long id,
            @ParameterObject Pageable pageable) {

        log.info("댓글 목록 조회 요청 - postId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        Page<BoardCommentDto.Response> response = commentService.getComments(
                id, tokenInfo.email(), pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 대댓글 목록 조회
     *
     * @param id 부모 댓글 ID
     * @return 대댓글 목록
     */
    @Override
    @GetMapping("/comments/{id}/replies")
    public ResponseEntity<ApiResponse<List<BoardCommentDto.Response>>> getReplies(
            @PathVariable Long id) {

        log.info("대댓글 목록 조회 요청 - parentCommentId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        List<BoardCommentDto.Response> response = commentService.getReplies(
                id, tokenInfo.email());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 댓글 생성
     *
     * @param request 생성 요청
     * @return 생성된 댓글
     */
    @Override
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<BoardCommentDto.Response>> createComment(
            @Valid @RequestBody BoardCommentDto.CreateRequest request) {

        log.info("댓글 생성 요청 - postId: {}, parentCommentId: {}",
                request.postId(), request.parentCommentId());

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        BoardCommentDto.Response response = commentService.createComment(
                request, tokenInfo.email());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 댓글 수정
     *
     * @param id      댓글 ID
     * @param request 수정 요청
     * @return 수정된 댓글
     */
    @Override
    @PutMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<BoardCommentDto.Response>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody BoardCommentDto.UpdateRequest request) {

        log.info("댓글 수정 요청 - commentId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        BoardCommentDto.Response response = commentService.updateComment(
                id, request, tokenInfo.email(), tokenInfo.role());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 댓글 삭제
     *
     * @param id 댓글 ID
     * @return 성공 응답
     */
    @Override
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id) {

        log.info("댓글 삭제 요청 - commentId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        commentService.deleteComment(id, tokenInfo.email(), tokenInfo.role());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 댓글 좋아요 토글
     *
     * @param id 댓글 ID
     * @return 좋아요 여부 (true: 좋아요 추가, false: 좋아요 취소)
     */
    @Override
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<ApiResponse<Boolean>> toggleCommentLike(
            @PathVariable Long id) {

        log.info("댓글 좋아요 토글 요청 - commentId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        boolean isLiked = commentService.toggleCommentLike(id, tokenInfo.email());

        return ResponseEntity.ok(ApiResponse.success(isLiked));
    }
}
