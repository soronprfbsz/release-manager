package com.ts.rm.domain.board.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.board.dto.BoardPostDto;
import com.ts.rm.domain.board.dto.BoardTopicDto;
import com.ts.rm.domain.board.entity.BoardPost;
import com.ts.rm.domain.board.entity.BoardPostLike;
import com.ts.rm.domain.board.entity.BoardPostView;
import com.ts.rm.domain.board.entity.BoardTopic;
import com.ts.rm.domain.board.mapper.BoardPostDtoMapper;
import com.ts.rm.domain.board.mapper.BoardTopicDtoMapper;
import com.ts.rm.domain.board.repository.BoardPostLikeRepository;
import com.ts.rm.domain.board.repository.BoardPostRepository;
import com.ts.rm.domain.board.repository.BoardPostViewRepository;
import com.ts.rm.domain.board.repository.BoardTopicRepository;
import com.ts.rm.global.account.AccountLookupService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Board Service
 *
 * <p>게시판 토픽 및 게시글 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardTopicRepository topicRepository;
    private final BoardPostRepository postRepository;
    private final BoardPostLikeRepository postLikeRepository;
    private final BoardPostViewRepository postViewRepository;
    private final BoardTopicDtoMapper topicMapper;
    private final BoardPostDtoMapper postMapper;
    private final AccountLookupService accountLookupService;
    private final BoardImageService boardImageService;

    private static final String ROLE_ADMIN = "ADMIN";

    // ========================================
    // Topic Operations
    // ========================================

    /**
     * 토픽 목록 조회 (활성화된 토픽만)
     *
     * @return 토픽 목록
     */
    public List<BoardTopicDto.ListResponse> getTopics() {
        log.debug("토픽 목록 조회");
        List<BoardTopic> topics = topicRepository.findAllByIsEnabledTrueOrderBySortOrderAsc();
        return topicMapper.toListResponseList(topics);
    }

    /**
     * 토픽 상세 조회
     *
     * @param topicId 토픽 ID
     * @return 토픽 상세
     */
    public BoardTopicDto.Response getTopic(String topicId) {
        log.debug("토픽 상세 조회 - topicId: {}", topicId);
        BoardTopic topic = findTopicById(topicId);
        long postCount = postRepository.countByTopic_TopicId(topicId);

        BoardTopicDto.Response response = topicMapper.toResponse(topic);
        return new BoardTopicDto.Response(
                response.topicId(),
                response.topicName(),
                response.description(),
                response.icon(),
                response.sortOrder(),
                response.isEnabled(),
                postCount,
                response.createdAt(),
                response.updatedAt()
        );
    }

    // ========================================
    // Post Operations
    // ========================================

    /**
     * 게시글 목록 조회 (페이징)
     *
     * @param topicId   토픽 ID (null이면 전체)
     * @param keyword   검색 키워드
     * @param pageable  페이징 정보
     * @return 게시글 페이지
     */
    public Page<BoardPostDto.ListResponse> getPosts(String topicId, String keyword, Pageable pageable) {
        log.debug("게시글 목록 조회 - topicId: {}, keyword: {}", topicId, keyword);
        // 발행된 게시글만 조회
        Page<BoardPost> posts = postRepository.findAllWithFilters(topicId, keyword, true, pageable);
        return posts.map(postMapper::toListResponse);
    }

    /**
     * 게시글 상세 조회
     *
     * @param postId       게시글 ID
     * @param currentEmail 현재 사용자 이메일 (좋아요 여부 확인용)
     * @return 게시글 상세
     */
    @Transactional
    public BoardPostDto.Response getPost(Long postId, String currentEmail) {
        log.debug("게시글 상세 조회 - postId: {}", postId);

        // 게시글 조회
        BoardPost post = postRepository.findByIdWithTopic(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "게시글을 찾을 수 없습니다: " + postId));

        // 조회수 증가 (동일 계정은 1회만)
        boolean viewCountIncremented = false;
        boolean isLikedByMe = false;

        if (currentEmail != null) {
            Account currentUser = accountLookupService.findByEmail(currentEmail);
            Long accountId = currentUser.getAccountId();

            // 조회 이력 확인 및 조회수 증가
            if (!postViewRepository.existsByPostIdAndAccountId(postId, accountId)) {
                // 첫 조회: 조회 이력 저장 + 조회수 증가
                BoardPostView postView = BoardPostView.builder()
                        .postId(postId)
                        .accountId(accountId)
                        .build();
                postViewRepository.save(postView);
                postRepository.incrementViewCount(postId);
                viewCountIncremented = true;
                log.debug("게시글 조회수 증가 - postId: {}, accountId: {}", postId, accountId);
            }

            // 좋아요 여부 확인
            isLikedByMe = postLikeRepository.existsByPostIdAndAccountId(postId, accountId);
        }

        BoardPostDto.Response response = postMapper.toResponse(post);
        return new BoardPostDto.Response(
                response.postId(),
                response.topicId(),
                response.topicName(),
                response.title(),
                response.content(),
                response.thumbnailUrl(),
                viewCountIncremented ? response.viewCount() + 1 : response.viewCount(),
                response.likeCount(),
                response.commentCount(),
                response.isPinned(),
                response.isPublished(),
                isLikedByMe,
                response.createdById(),
                response.createdByEmail(),
                response.createdByName(),
                response.createdByAvatarStyle(),
                response.createdByAvatarSeed(),
                response.createdAt(),
                response.updatedAt()
        );
    }

    /**
     * 게시글 생성
     *
     * @param request        생성 요청
     * @param createdByEmail 생성자 이메일
     * @return 생성된 게시글
     */
    @Transactional
    public BoardPostDto.Response createPost(BoardPostDto.CreateRequest request, String createdByEmail) {
        log.info("게시글 생성 - topicId: {}, title: {}", request.topicId(), request.title());

        // 토픽 조회
        BoardTopic topic = findTopicById(request.topicId());

        // 생성자 조회
        Account creator = accountLookupService.findByEmail(createdByEmail);

        // 게시글 엔티티 생성
        BoardPost post = postMapper.toEntity(request);
        post.setTopic(topic);
        post.setCreator(creator);
        post.setCreatedByEmail(creator.getEmail());

        // 기본값 설정
        if (request.isPinned() == null) {
            post.setIsPinned(false);
        }
        if (request.isPublished() == null) {
            post.setIsPublished(true);
        }

        // 썸네일 자동 설정 (지정하지 않은 경우 content의 첫 번째 이미지 사용)
        if (post.getThumbnailUrl() == null || post.getThumbnailUrl().isBlank()) {
            String firstImageUrl = boardImageService.extractFirstImageUrl(post.getContent());
            post.setThumbnailUrl(firstImageUrl);
        }

        BoardPost savedPost = postRepository.save(post);

        // 이미지 연결 (content에서 이미지 URL 추출하여 연결)
        boardImageService.linkImagesToPost(savedPost, savedPost.getContent());

        log.info("게시글 생성 완료 - postId: {}", savedPost.getPostId());
        return postMapper.toResponse(savedPost);
    }

    /**
     * 게시글 수정
     *
     * @param postId         게시글 ID
     * @param request        수정 요청
     * @param updatedByEmail 수정자 이메일
     * @param role           수정자 역할
     * @return 수정된 게시글
     */
    @Transactional
    public BoardPostDto.Response updatePost(Long postId, BoardPostDto.UpdateRequest request,
            String updatedByEmail, String role) {
        log.info("게시글 수정 - postId: {}", postId);

        // 게시글 조회
        BoardPost post = postRepository.findByIdWithTopic(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "게시글을 찾을 수 없습니다: " + postId));

        // 권한 검증
        validateModifyPermission(post.getCreatedByEmail(), updatedByEmail, role);

        // 필드 수정 (null이 아닌 필드만)
        if (request.title() != null) {
            post.setTitle(request.title());
        }
        if (request.content() != null) {
            post.setContent(request.content());
        }
        if (request.isPinned() != null) {
            post.setIsPinned(request.isPinned());
        }
        if (request.isPublished() != null) {
            post.setIsPublished(request.isPublished());
        }

        // 썸네일 처리
        if (request.thumbnailUrl() != null) {
            // 사용자가 명시적으로 지정한 경우
            post.setThumbnailUrl(request.thumbnailUrl());
        } else if (request.content() != null) {
            // content가 변경되고 thumbnailUrl이 명시적으로 지정되지 않은 경우
            // → 첫 번째 이미지로 자동 갱신 (없으면 null)
            String firstImageUrl = boardImageService.extractFirstImageUrl(post.getContent());
            post.setThumbnailUrl(firstImageUrl);
        }

        // 이미지 연결 업데이트 (content가 수정된 경우)
        if (request.content() != null) {
            boardImageService.updatePostImages(post, post.getContent());
        }

        log.info("게시글 수정 완료 - postId: {}", postId);
        return postMapper.toResponse(post);
    }

    /**
     * 게시글 삭제
     *
     * @param postId       게시글 ID
     * @param requestEmail 요청자 이메일
     * @param role         요청자 역할
     */
    @Transactional
    public void deletePost(Long postId, String requestEmail, String role) {
        log.info("게시글 삭제 - postId: {}", postId);

        BoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "게시글을 찾을 수 없습니다: " + postId));

        // 권한 검증
        validateModifyPermission(post.getCreatedByEmail(), requestEmail, role);

        // 이미지 연결 해제 (유령 이미지 관리용)
        boardImageService.unlinkImagesFromPost(postId);

        postRepository.delete(post);

        log.info("게시글 삭제 완료 - postId: {}", postId);
    }

    // ========================================
    // Like Operations
    // ========================================

    /**
     * 게시글 좋아요 토글
     *
     * @param postId      게시글 ID
     * @param likerEmail  좋아요 사용자 이메일
     * @return 좋아요 여부 (true: 좋아요, false: 좋아요 취소)
     */
    @Transactional
    public boolean togglePostLike(Long postId, String likerEmail) {
        log.debug("게시글 좋아요 토글 - postId: {}", postId);

        // 게시글 존재 확인
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "게시글을 찾을 수 없습니다: " + postId);
        }

        // 사용자 조회
        Account liker = accountLookupService.findByEmail(likerEmail);

        // 이미 좋아요 했는지 확인
        boolean alreadyLiked = postLikeRepository.existsByPostIdAndAccountId(postId, liker.getAccountId());

        if (alreadyLiked) {
            // 좋아요 취소
            postLikeRepository.deleteByPostIdAndAccountId(postId, liker.getAccountId());
            postRepository.decrementLikeCount(postId);
            log.debug("게시글 좋아요 취소 - postId: {}, accountId: {}", postId, liker.getAccountId());
            return false;
        } else {
            // 좋아요 추가
            BoardPostLike like = BoardPostLike.builder()
                    .postId(postId)
                    .accountId(liker.getAccountId())
                    .build();
            postLikeRepository.save(like);
            postRepository.incrementLikeCount(postId);
            log.debug("게시글 좋아요 추가 - postId: {}, accountId: {}", postId, liker.getAccountId());
            return true;
        }
    }

    // ========================================
    // Private Helper Methods
    // ========================================

    /**
     * 토픽 ID로 조회
     */
    private BoardTopic findTopicById(String topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "토픽을 찾을 수 없습니다: " + topicId));
    }

    /**
     * 수정/삭제 권한 검증
     *
     * <p>ADMIN 역할이거나 작성자만 수정/삭제 가능
     */
    private void validateModifyPermission(String creatorEmail, String requestEmail, String role) {
        // ADMIN은 모든 게시글 수정/삭제 가능
        if (ROLE_ADMIN.equals(role)) {
            return;
        }

        // 작성자 본인만 수정/삭제 가능
        if (creatorEmail != null && creatorEmail.equals(requestEmail)) {
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN,
                "게시글 수정/삭제 권한이 없습니다. ADMIN 또는 작성자만 가능합니다.");
    }
}
