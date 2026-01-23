package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardImageDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * BoardImageController Swagger 문서화 인터페이스
 */
@Tag(name = "게시판 이미지", description = "게시판 이미지 업로드, 조회, 삭제 API")
@SwaggerResponse
public interface BoardImageControllerDocs {

    @Operation(
            summary = "이미지 업로드",
            description = """
                    게시글 작성/수정 시 이미지를 업로드합니다.

                    **지원 형식**: jpg, jpeg, png, gif, webp, svg, bmp

                    **최대 크기**: 10MB

                    **저장 경로**: board/images/{year}/{month}/{uuid}_{fileName}

                    **TipTap 에디터 연동**:
                    1. 이미지 드래그&드롭 또는 붙여넣기
                    2. 이 API로 업로드
                    3. 응답의 `url`을 에디터에 삽입
                    """,
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "업로드 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BoardImageUploadApiResponse.class)
                    )
            )
    )
    ResponseEntity<ApiResponse<BoardImageDto.UploadResponse>> uploadImage(
            @Parameter(description = "이미지 파일", required = true)
            @RequestParam("file") MultipartFile file
    );

    @Operation(
            summary = "이미지 조회",
            description = """
                    업로드된 이미지를 조회합니다.

                    게시글 본문에 포함된 이미지 URL로 직접 접근 가능합니다.

                    **응답**: 이미지 바이너리 (Content-Type: image/*)
                    """,
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이미지 반환",
                    content = @Content(mediaType = "image/*")
            )
    )
    ResponseEntity<Resource> getImage(
            @Parameter(description = "년도", required = true, example = "2026")
            @PathVariable int year,

            @Parameter(description = "월", required = true, example = "1")
            @PathVariable int month,

            @Parameter(description = "파일명", required = true, example = "a1b2c3d4_screenshot.png")
            @PathVariable String fileName
    );

    @Operation(
            summary = "이미지 삭제",
            description = """
                    업로드된 이미지를 삭제합니다.

                    **주의**: 게시글에서 참조 중인 이미지를 삭제하면 이미지가 깨집니다.
                    """,
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"status\": \"success\", \"data\": null}"
                            )
                    )
            )
    )
    ResponseEntity<ApiResponse<Void>> deleteImage(
            @Parameter(description = "년도", required = true, example = "2026")
            @PathVariable int year,

            @Parameter(description = "월", required = true, example = "1")
            @PathVariable int month,

            @Parameter(description = "파일명", required = true, example = "a1b2c3d4_screenshot.png")
            @PathVariable String fileName
    );

    /**
     * Swagger 스키마용 wrapper 클래스 - 이미지 업로드 응답
     */
    @Schema(description = "이미지 업로드 API 응답")
    class BoardImageUploadApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "업로드된 이미지 정보")
        public BoardImageDto.UploadResponse data;
    }
}
