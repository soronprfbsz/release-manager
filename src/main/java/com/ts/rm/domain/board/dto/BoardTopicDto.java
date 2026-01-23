package com.ts.rm.domain.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * BoardTopic DTO 통합 클래스
 */
public final class BoardTopicDto {

    private BoardTopicDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 토픽 생성 요청
     */
    @Builder
    @Schema(description = "토픽 생성 요청")
    public record CreateRequest(
            @Schema(description = "토픽 ID (고유, PK)", example = "FEATURE_REQUEST")
            @NotBlank(message = "토픽 ID는 필수입니다")
            @Size(max = 50, message = "토픽 ID는 50자 이하여야 합니다")
            String topicId,

            @Schema(description = "토픽명", example = "기능 요청")
            @NotBlank(message = "토픽명은 필수입니다")
            @Size(max = 100, message = "토픽명은 100자 이하여야 합니다")
            String topicName,

            @Schema(description = "토픽 설명", example = "새로운 기능 요청 게시판입니다")
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description,

            @Schema(description = "아이콘 (Lucide React 아이콘명)", example = "lightbulb")
            @Size(max = 50, message = "아이콘명은 50자 이하여야 합니다")
            String icon,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled
    ) {

    }

    /**
     * 토픽 수정 요청
     */
    @Builder
    @Schema(description = "토픽 수정 요청")
    public record UpdateRequest(
            @Schema(description = "토픽명", example = "기능 요청")
            @Size(max = 100, message = "토픽명은 100자 이하여야 합니다")
            String topicName,

            @Schema(description = "토픽 설명", example = "새로운 기능 요청 게시판입니다")
            @Size(max = 500, message = "설명은 500자 이하여야 합니다")
            String description,

            @Schema(description = "아이콘 (Lucide React 아이콘명)", example = "lightbulb")
            @Size(max = 50, message = "아이콘명은 50자 이하여야 합니다")
            String icon,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled
    ) {

    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 토픽 응답
     */
    @Schema(description = "토픽 응답")
    public record Response(
            @Schema(description = "토픽 ID", example = "QNA")
            String topicId,

            @Schema(description = "토픽명", example = "QnA")
            String topicName,

            @Schema(description = "토픽 설명", example = "질문과 답변 게시판입니다")
            String description,

            @Schema(description = "아이콘", example = "message-circle-question")
            String icon,

            @Schema(description = "정렬 순서", example = "3")
            Integer sortOrder,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled,

            @Schema(description = "게시글 수", example = "42")
            Long postCount,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt
    ) {

    }

    /**
     * 토픽 목록 응답 (간략)
     */
    @Schema(description = "토픽 목록 응답")
    public record ListResponse(
            @Schema(description = "토픽 ID", example = "QNA")
            String topicId,

            @Schema(description = "토픽명", example = "QnA")
            String topicName,

            @Schema(description = "토픽 설명", example = "질문과 답변 게시판입니다")
            String description,

            @Schema(description = "아이콘", example = "message-circle-question")
            String icon,

            @Schema(description = "활성화 여부", example = "true")
            Boolean isEnabled
    ) {

    }
}
