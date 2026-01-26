package com.ts.rm.domain.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * BoardPost DTO 통합 클래스
 */
public final class BoardPostDto {

    private BoardPostDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 게시글 생성 요청
     */
    @Builder
    @Schema(description = "게시글 생성 요청")
    public record CreateRequest(
            @Schema(description = "토픽 ID", example = "QNA")
            @NotBlank(message = "토픽 ID는 필수입니다")
            String topicId,

            @Schema(description = "제목", example = "로그인 기능 개선 요청")
            @NotBlank(message = "제목은 필수입니다")
            @Size(max = 200, message = "제목은 200자 이하여야 합니다")
            String title,

            @Schema(description = "본문 (마크다운)", example = "## 개선 내용\n로그인 시 2FA 지원을 요청합니다.")
            String content,

            @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumb.png")
            @Size(max = 500, message = "썸네일 URL은 500자 이하여야 합니다")
            String thumbnailUrl,

            @Schema(description = "상단 고정 여부", example = "false")
            Boolean isPinned,

            @Schema(description = "발행 여부 (false면 임시저장)", example = "true")
            Boolean isPublished
    ) {

    }

    /**
     * 게시글 수정 요청
     */
    @Builder
    @Schema(description = "게시글 수정 요청")
    public record UpdateRequest(
            @Schema(description = "제목", example = "로그인 기능 개선 요청 (수정)")
            @Size(max = 200, message = "제목은 200자 이하여야 합니다")
            String title,

            @Schema(description = "본문 (마크다운)", example = "## 개선 내용\n수정된 내용입니다.")
            String content,

            @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumb.png")
            @Size(max = 500, message = "썸네일 URL은 500자 이하여야 합니다")
            String thumbnailUrl,

            @Schema(description = "상단 고정 여부", example = "false")
            Boolean isPinned,

            @Schema(description = "발행 여부", example = "true")
            Boolean isPublished
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 게시글 상세 응답
     */
    @Schema(description = "게시글 상세 응답")
    public record Response(
            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "토픽 ID", example = "QNA")
            String topicId,

            @Schema(description = "토픽명", example = "QnA")
            String topicName,

            @Schema(description = "제목", example = "로그인 기능 개선 요청")
            String title,

            @Schema(description = "본문 (마크다운)", example = "## 개선 내용\n로그인 시 2FA 지원을 요청합니다.")
            String content,

            @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumb.png")
            String thumbnailUrl,

            @Schema(description = "조회수", example = "42")
            Integer viewCount,

            @Schema(description = "좋아요 수", example = "10")
            Integer likeCount,

            @Schema(description = "댓글 수", example = "5")
            Integer commentCount,

            @Schema(description = "상단 고정 여부", example = "false")
            Boolean isPinned,

            @Schema(description = "발행 여부", example = "true")
            Boolean isPublished,

            @Schema(description = "현재 사용자가 좋아요 했는지 여부", example = "false")
            Boolean isLikedByMe,

            @Schema(description = "작성자 계정 ID", example = "1")
            Long createdById,

            @Schema(description = "작성자 이메일", example = "user@company.com")
            String createdByEmail,

            @Schema(description = "작성자 이름", example = "홍길동")
            String createdByName,

            @Schema(description = "작성자 아바타 스타일", example = "lorelei")
            String createdByAvatarStyle,

            @Schema(description = "작성자 아바타 시드", example = "abc123")
            String createdByAvatarSeed,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }

    /**
     * 게시글 목록 응답 (간략)
     */
    @Schema(description = "게시글 목록 응답")
    public record ListResponse(
            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "토픽 ID", example = "QNA")
            String topicId,

            @Schema(description = "제목", example = "로그인 기능 개선 요청")
            String title,

            @Schema(description = "내용 미리보기 (최대 200자)", example = "로그인 시 2FA 지원을 요청합니다...")
            String contentPreview,

            @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumb.png")
            String thumbnailUrl,

            @Schema(description = "조회수", example = "42")
            Integer viewCount,

            @Schema(description = "좋아요 수", example = "10")
            Integer likeCount,

            @Schema(description = "댓글 수", example = "5")
            Integer commentCount,

            @Schema(description = "상단 고정 여부", example = "false")
            Boolean isPinned,

            @Schema(description = "작성자 이메일", example = "user@company.com")
            String createdByEmail,

            @Schema(description = "작성자 이름", example = "홍길동")
            String createdByName,

            @Schema(description = "작성자 아바타 스타일", example = "lorelei")
            String createdByAvatarStyle,

            @Schema(description = "작성자 아바타 시드", example = "abc123")
            String createdByAvatarSeed,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }
}
