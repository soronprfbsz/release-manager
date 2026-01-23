package com.ts.rm.domain.board.controller;

import com.ts.rm.domain.board.dto.BoardImageDto;
import com.ts.rm.domain.board.service.BoardImageService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import com.ts.rm.global.security.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * BoardImage Controller
 *
 * <p>게시판 이미지 업로드, 조회, 삭제 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/board/images")
@RequiredArgsConstructor
public class BoardImageController implements BoardImageControllerDocs {

    private final BoardImageService boardImageService;

    /**
     * 이미지 업로드
     *
     * @param file 이미지 파일
     * @return 업로드 결과
     */
    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BoardImageDto.UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        log.info("게시판 이미지 업로드 요청 - fileName: {}", file.getOriginalFilename());

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        BoardImageDto.UploadResponse response = boardImageService.uploadImage(
                file, tokenInfo.email());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 이미지 조회
     *
     * @param year     년도
     * @param month    월
     * @param fileName 파일명
     * @return 이미지 Resource
     */
    @Override
    @GetMapping("/{year}/{month}/{fileName}")
    public ResponseEntity<Resource> getImage(
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable String fileName) {

        log.debug("게시판 이미지 조회 요청 - year: {}, month: {}, fileName: {}", year, month, fileName);

        Resource resource = boardImageService.loadImage(year, month, fileName);
        String mimeType = boardImageService.getImageMimeType(year, month, fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                .body(resource);
    }

    /**
     * 이미지 삭제
     *
     * @param year     년도
     * @param month    월
     * @param fileName 파일명
     * @return 성공 응답
     */
    @Override
    @DeleteMapping("/{year}/{month}/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable String fileName) {

        log.info("게시판 이미지 삭제 요청 - year: {}, month: {}, fileName: {}", year, month, fileName);

        boardImageService.deleteImage(year, month, fileName);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
