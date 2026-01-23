package com.ts.rm.domain.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * BoardComment DTO 통합 클래스
 */
public final class BoardCommentDto {

    private BoardCommentDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 댓글 생성 요청
     */
    @Builder
    @Schema(description = "댓글 생성 요청")
    public record CreateRequest(
            @Schema(description = "게시글 ID", example = "1")
            @NotNull(message = "게시글 ID는 필수입니다")
            Long postId,

            @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "null")
            Long parentCommentId,

            @Schema(description = "댓글 내용", example = "좋은 제안입니다!")
            @NotBlank(message = "댓글 내용은 필수입니다")
            String content
    ) {

    }

    /**
     * 댓글 수정 요청
     */
    @Builder
    @Schema(description = "댓글 수정 요청")
    public record UpdateRequest(
            @Schema(description = "댓글 내용", example = "수정된 댓글 내용입니다.")
            @NotBlank(message = "댓글 내용은 필수입니다")
            String content
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 댓글 응답
     */
    @Schema(description = "댓글 응답")
    public record Response(
            @Schema(description = "댓글 ID", example = "1")
            Long commentId,

            @Schema(description = "게시글 ID", example = "1")
            Long postId,

            @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "null")
            Long parentCommentId,

            @Schema(description = "댓글 내용", example = "좋은 제안입니다!")
            String content,

            @Schema(description = "좋아요 수", example = "3")
            Integer likeCount,

            @Schema(description = "삭제 여부", example = "false")
            Boolean isDeleted,

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

            @Schema(description = "대댓글 목록")
            List<Response> replies,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }

    /**
     * 댓글 목록 응답 (간략, 대댓글 미포함)
     */
    @Schema(description = "댓글 목록 응답")
    public record ListResponse(
            @Schema(description = "댓글 ID", example = "1")
            Long commentId,

            @Schema(description = "댓글 내용", example = "좋은 제안입니다!")
            String content,

            @Schema(description = "좋아요 수", example = "3")
            Integer likeCount,

            @Schema(description = "삭제 여부", example = "false")
            Boolean isDeleted,

            @Schema(description = "대댓글 개수", example = "2")
            Integer replyCount,

            @Schema(description = "현재 사용자가 좋아요 했는지 여부", example = "false")
            Boolean isLikedByMe,

            @Schema(description = "작성자 이름", example = "홍길동")
            String createdByName,

            @Schema(description = "작성자 아바타 스타일", example = "lorelei")
            String createdByAvatarStyle,

            @Schema(description = "작성자 아바타 시드", example = "abc123")
            String createdByAvatarSeed,

            @Schema(description = "생성일시")
            LocalDateTime createdAt
    ) {

    }
}
