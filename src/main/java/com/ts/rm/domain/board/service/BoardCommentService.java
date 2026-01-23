package com.ts.rm.domain.board.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.board.dto.BoardCommentDto;
import com.ts.rm.domain.board.entity.BoardComment;
import com.ts.rm.domain.board.entity.BoardCommentLike;
import com.ts.rm.domain.board.entity.BoardPost;
import com.ts.rm.domain.board.mapper.BoardCommentDtoMapper;
import com.ts.rm.domain.board.repository.BoardCommentLikeRepository;
import com.ts.rm.domain.board.repository.BoardCommentRepository;
import com.ts.rm.domain.board.repository.BoardPostRepository;
import com.ts.rm.global.account.AccountLookupService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BoardComment Service
 *
 * <p>댓글 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCommentService {

    private final BoardCommentRepository commentRepository;
    private final BoardCommentLikeRepository commentLikeRepository;
    private final BoardPostRepository postRepository;
    private final BoardCommentDtoMapper mapper;
    private final AccountLookupService accountLookupService;

    private static final String ROLE_ADMIN = "ADMIN";

    // ========================================
    // Comment Operations
    // ========================================

    /**
     * 댓글 목록 조회 (페이징)
     *
     * <p>최상위 댓글만 조회하며, 대댓글은 별도 API로 조회
     *
     * @param postId       게시글 ID
     * @param currentEmail 현재 사용자 이메일 (좋아요 여부 확인용)
     * @param pageable     페이징 정보
     * @return 댓글 페이지
     */
    public Page<BoardCommentDto.Response> getComments(Long postId, String currentEmail, Pageable pageable) {
        log.debug("댓글 목록 조회 - postId: {}", postId);

        // 게시글 존재 확인
        validatePostExists(postId);

        // 현재 사용자 조회
        Long currentAccountId = null;
        if (currentEmail != null) {
            Account currentUser = accountLookupService.findByEmail(currentEmail);
            currentAccountId = currentUser.getAccountId();
        }

        Page<BoardComment> comments = commentRepository.findRootCommentsByPostId(postId, pageable);

        final Long accountId = currentAccountId;
        return comments.map(comment -> {
            BoardCommentDto.Response response = mapper.toResponse(comment);

            // 대댓글 목록 조회
            List<BoardComment> replies = commentRepository.findRepliesByParentCommentId(comment.getCommentId());
            List<BoardCommentDto.Response> replyResponses = replies.stream()
                    .map(reply -> {
                        BoardCommentDto.Response replyResponse = mapper.toResponse(reply);
                        boolean replyLiked = accountId != null &&
                                commentLikeRepository.existsByCommentIdAndAccountId(reply.getCommentId(), accountId);
                        return buildResponseWithLike(replyResponse, replyLiked, null);
                    })
                    .toList();

            // 좋아요 여부 확인
            boolean isLikedByMe = accountId != null &&
                    commentLikeRepository.existsByCommentIdAndAccountId(comment.getCommentId(), accountId);

            return buildResponseWithLike(response, isLikedByMe, replyResponses);
        });
    }

    /**
     * 대댓글 목록 조회
     *
     * @param parentCommentId 부모 댓글 ID
     * @param currentEmail    현재 사용자 이메일 (좋아요 여부 확인용)
     * @return 대댓글 목록
     */
    public List<BoardCommentDto.Response> getReplies(Long parentCommentId, String currentEmail) {
        log.debug("대댓글 목록 조회 - parentCommentId: {}", parentCommentId);

        // 현재 사용자 조회
        Long currentAccountId = null;
        if (currentEmail != null) {
            Account currentUser = accountLookupService.findByEmail(currentEmail);
            currentAccountId = currentUser.getAccountId();
        }

        List<BoardComment> replies = commentRepository.findRepliesByParentCommentId(parentCommentId);

        final Long accountId = currentAccountId;
        return replies.stream()
                .map(reply -> {
                    BoardCommentDto.Response response = mapper.toResponse(reply);
                    boolean isLikedByMe = accountId != null &&
                            commentLikeRepository.existsByCommentIdAndAccountId(reply.getCommentId(), accountId);
                    return buildResponseWithLike(response, isLikedByMe, null);
                })
                .toList();
    }

    /**
     * 댓글 생성
     *
     * @param request        생성 요청
     * @param createdByEmail 생성자 이메일
     * @return 생성된 댓글
     */
    @Transactional
    public BoardCommentDto.Response createComment(BoardCommentDto.CreateRequest request, String createdByEmail) {
        log.info("댓글 생성 - postId: {}, parentCommentId: {}", request.postId(), request.parentCommentId());

        // 게시글 조회
        BoardPost post = findPostById(request.postId());

        // 생성자 조회
        Account creator = accountLookupService.findByEmail(createdByEmail);

        // 댓글 엔티티 생성
        BoardComment comment = mapper.toEntity(request);
        comment.setPost(post);
        comment.setCreator(creator);
        comment.setCreatedByEmail(creator.getEmail());

        // 대댓글인 경우 부모 댓글 설정
        if (request.parentCommentId() != null) {
            BoardComment parentComment = findCommentById(request.parentCommentId());
            // 부모 댓글이 같은 게시글의 댓글인지 확인
            if (!parentComment.getPost().getPostId().equals(request.postId())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "부모 댓글이 해당 게시글의 댓글이 아닙니다");
            }
            comment.setParentComment(parentComment);
        }

        BoardComment savedComment = commentRepository.save(comment);

        // 게시글 댓글 수 증가
        postRepository.incrementCommentCount(request.postId());

        log.info("댓글 생성 완료 - commentId: {}", savedComment.getCommentId());
        return mapper.toResponse(savedComment);
    }

    /**
     * 댓글 수정
     *
     * @param commentId      댓글 ID
     * @param request        수정 요청
     * @param updatedByEmail 수정자 이메일
     * @param role           수정자 역할
     * @return 수정된 댓글
     */
    @Transactional
    public BoardCommentDto.Response updateComment(Long commentId, BoardCommentDto.UpdateRequest request,
            String updatedByEmail, String role) {
        log.info("댓글 수정 - commentId: {}", commentId);

        // 댓글 조회
        BoardComment comment = findCommentById(commentId);

        // 권한 검증
        validateModifyPermission(comment.getCreatedByEmail(), updatedByEmail, role);

        // 내용 수정
        comment.setContent(request.content());
        comment.setUpdatedAt(LocalDateTime.now());

        log.info("댓글 수정 완료 - commentId: {}", commentId);
        return mapper.toResponse(comment);
    }

    /**
     * 댓글 삭제 (소프트 삭제)
     *
     * <p>대댓글이 있는 경우 소프트 삭제, 없는 경우 물리 삭제
     *
     * @param commentId    댓글 ID
     * @param requestEmail 요청자 이메일
     * @param role         요청자 역할
     */
    @Transactional
    public void deleteComment(Long commentId, String requestEmail, String role) {
        log.info("댓글 삭제 - commentId: {}", commentId);

        BoardComment comment = findCommentById(commentId);

        // 권한 검증
        validateModifyPermission(comment.getCreatedByEmail(), requestEmail, role);

        Long postId = comment.getPost().getPostId();

        // 대댓글 존재 여부 확인
        boolean hasReplies = commentRepository.existsByParentComment_CommentIdAndIsDeletedFalse(commentId);

        if (hasReplies) {
            // 대댓글이 있으면 소프트 삭제
            comment.softDelete();
            log.info("댓글 소프트 삭제 완료 - commentId: {}", commentId);
        } else {
            // 대댓글이 없으면 물리 삭제
            commentRepository.delete(comment);
            // 게시글 댓글 수 감소
            postRepository.decrementCommentCount(postId);
            log.info("댓글 물리 삭제 완료 - commentId: {}", commentId);
        }
    }

    // ========================================
    // Like Operations
    // ========================================

    /**
     * 댓글 좋아요 토글
     *
     * @param commentId  댓글 ID
     * @param likerEmail 좋아요 사용자 이메일
     * @return 좋아요 여부 (true: 좋아요, false: 좋아요 취소)
     */
    @Transactional
    public boolean toggleCommentLike(Long commentId, String likerEmail) {
        log.debug("댓글 좋아요 토글 - commentId: {}", commentId);

        // 댓글 존재 확인
        if (!commentRepository.existsById(commentId)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "댓글을 찾을 수 없습니다: " + commentId);
        }

        // 사용자 조회
        Account liker = accountLookupService.findByEmail(likerEmail);

        // 이미 좋아요 했는지 확인
        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndAccountId(commentId, liker.getAccountId());

        if (alreadyLiked) {
            // 좋아요 취소
            commentLikeRepository.deleteByCommentIdAndAccountId(commentId, liker.getAccountId());
            commentRepository.decrementLikeCount(commentId);
            log.debug("댓글 좋아요 취소 - commentId: {}, accountId: {}", commentId, liker.getAccountId());
            return false;
        } else {
            // 좋아요 추가
            BoardCommentLike like = BoardCommentLike.builder()
                    .commentId(commentId)
                    .accountId(liker.getAccountId())
                    .build();
            commentLikeRepository.save(like);
            commentRepository.incrementLikeCount(commentId);
            log.debug("댓글 좋아요 추가 - commentId: {}, accountId: {}", commentId, liker.getAccountId());
            return true;
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * 게시글 존재 확인
     */
    private void validatePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "게시글을 찾을 수 없습니다: " + postId);
        }
    }

    /**
     * 게시글 ID로 조회
     */
    private BoardPost findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "게시글을 찾을 수 없습니다: " + postId));
    }

    /**
     * 댓글 ID로 조회
     */
    private BoardComment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "댓글을 찾을 수 없습니다: " + commentId));
    }

    /**
     * 수정/삭제 권한 검증
     */
    private void validateModifyPermission(String creatorEmail, String requestEmail, String role) {
        // ADMIN은 모든 댓글 수정/삭제 가능
        if (ROLE_ADMIN.equals(role)) {
            return;
        }

        // 작성자 본인만 수정/삭제 가능
        if (creatorEmail != null && creatorEmail.equals(requestEmail)) {
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN,
                "댓글 수정/삭제 권한이 없습니다. ADMIN 또는 작성자만 가능합니다.");
    }

    /**
     * 좋아요 정보를 포함한 Response 빌드
     */
    private BoardCommentDto.Response buildResponseWithLike(BoardCommentDto.Response response,
            boolean isLikedByMe, List<BoardCommentDto.Response> replies) {
        return new BoardCommentDto.Response(
                response.commentId(),
                response.postId(),
                response.parentCommentId(),
                response.content(),
                response.likeCount(),
                response.isDeleted(),
                isLikedByMe,
                response.createdById(),
                response.createdByEmail(),
                response.createdByName(),
                response.createdByAvatarStyle(),
                response.createdByAvatarSeed(),
                replies,
                response.createdAt(),
                response.updatedAt()
        );
    }
}
