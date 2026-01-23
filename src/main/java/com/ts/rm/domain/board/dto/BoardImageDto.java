package com.ts.rm.domain.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 게시판 이미지 DTO
 */
public class BoardImageDto {

    private BoardImageDto() {
    }

    /**
     * 이미지 업로드 응답
     */
    @Schema(description = "이미지 업로드 응답")
    public record UploadResponse(
            @Schema(description = "저장된 파일명", example = "a1b2c3d4_screenshot.png")
            String fileName,

            @Schema(description = "이미지 URL", example = "/api/board/images/2026/01/a1b2c3d4_screenshot.png")
            String url,

            @Schema(description = "파일 크기 (bytes)", example = "102400")
            long size,

            @Schema(description = "MIME 타입", example = "image/png")
            String mimeType
    ) {
    }
}
