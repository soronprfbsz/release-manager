package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardPostDto;
import com.ts.rm.domain.board.service.BoardService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import com.ts.rm.global.security.TokenInfo;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BoardPost Controller
 *
 * <p>게시글 CRUD 및 좋아요 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/board/posts")
@RequiredArgsConstructor
public class BoardPostController implements BoardPostControllerDocs {

    private final BoardService boardService;

    /**
     * 게시글 목록 조회 (페이징)
     *
     * @param topicId  토픽 ID (null이면 전체)
     * @param keyword  검색 키워드
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardPostDto.ListResponse>>> getPosts(
            @RequestParam(required = false) String topicId,
            @RequestParam(required = false) String keyword,
            @ParameterObject Pageable pageable) {

        log.info("게시글 목록 조회 요청 - topicId: {}, keyword: {}", topicId, keyword);

        Page<BoardPostDto.ListResponse> response = boardService.getPosts(topicId, keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 상세 조회
     *
     * @param id 게시글 ID
     * @return 게시글 상세
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostDto.Response>> getPost(
            @PathVariable Long id) {

        log.info("게시글 상세 조회 요청 - postId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        BoardPostDto.Response response = boardService.getPost(id, tokenInfo.email());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 생성
     *
     * @param request 생성 요청
     * @return 생성된 게시글
     */
    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<BoardPostDto.Response>> createPost(
            @Valid @RequestBody BoardPostDto.CreateRequest request) {

        log.info("게시글 생성 요청 - topicId: {}, title: {}", request.topicId(), request.title());

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        BoardPostDto.Response response = boardService.createPost(request, tokenInfo.email());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 게시글 수정
     *
     * @param id      게시글 ID
     * @param request 수정 요청
     * @return 수정된 게시글
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardPostDto.Response>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody BoardPostDto.UpdateRequest request) {

        log.info("게시글 수정 요청 - postId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        BoardPostDto.Response response = boardService.updatePost(
                id, request, tokenInfo.email(), tokenInfo.role());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시글 삭제
     *
     * @param id 게시글 ID
     * @return 성공 응답
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id) {

        log.info("게시글 삭제 요청 - postId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        boardService.deletePost(id, tokenInfo.email(), tokenInfo.role());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 게시글 좋아요 토글
     *
     * @param id 게시글 ID
     * @return 좋아요 여부 (true: 좋아요 추가, false: 좋아요 취소)
     */
    @Override
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Boolean>> togglePostLike(
            @PathVariable Long id) {

        log.info("게시글 좋아요 토글 요청 - postId: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        boolean isLiked = boardService.togglePostLike(id, tokenInfo.email());

        return ResponseEntity.ok(ApiResponse.success(isLiked));
    }
}
