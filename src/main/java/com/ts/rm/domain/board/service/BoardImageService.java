package com.ts.rm.domain.board.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.global.account.AccountLookupService;
import com.ts.rm.domain.board.dto.BoardImageDto;
import com.ts.rm.domain.board.entity.BoardImage;
import com.ts.rm.domain.board.entity.BoardPost;
import com.ts.rm.domain.board.repository.BoardImageRepository;
import com.ts.rm.domain.common.service.FileStorageService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.file.FileContentUtil;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 게시판 이미지 서비스
 *
 * <p>게시판 이미지 업로드, 조회, 삭제 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardImageService {

    private final FileStorageService fileStorageService;
    private final BoardImageRepository boardImageRepository;
    private final AccountLookupService accountLookupService;

    private static final String BOARD_IMAGES_DIR = "board/images";

    /**
     * 이미지 URL 패턴 (content에서 이미지 URL 추출용)
     */
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(
            "/api/board/images/(\\d{4})/(\\d{2})/([^\"'\\s]+)"
    );

    /**
     * 허용된 이미지 확장자
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp"
    );

    /**
     * 허용된 이미지 MIME 타입
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            "image/bmp"
    );

    /**
     * 최대 파일 크기 (10MB)
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 이미지 업로드
     *
     * @param file         업로드할 이미지 파일
     * @param uploaderEmail 업로드한 사용자 이메일
     * @return 업로드 결과
     */
    @Transactional
    public BoardImageDto.UploadResponse uploadImage(MultipartFile file, String uploaderEmail) {
        log.info("게시판 이미지 업로드 요청 - fileName: {}, size: {}, uploader: {}",
                file.getOriginalFilename(), file.getSize(), uploaderEmail);

        // 파일 검증
        validateImageFile(file);

        // 파일명 생성 (UUID + 원본 파일명)
        String originalFileName = file.getOriginalFilename();
        String safeFileName = sanitizeFileName(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + safeFileName;

        // 년/월 기반 경로 생성
        LocalDate now = LocalDate.now();
        String yearMonth = String.format("%d/%02d", now.getYear(), now.getMonthValue());
        String relativePath = BOARD_IMAGES_DIR + "/" + yearMonth + "/" + uniqueFileName;

        // 파일 저장
        fileStorageService.saveFile(file, relativePath);

        // MIME 타입 조회
        Path savedPath = fileStorageService.getAbsolutePath(relativePath);
        String mimeType = FileContentUtil.getMimeType(savedPath);

        // 업로더 조회
        Account uploader = accountLookupService.findByEmail(uploaderEmail);

        // 메타데이터 저장
        BoardImage boardImage = BoardImage.builder()
                .fileName(uniqueFileName)
                .originalFileName(originalFileName)
                .filePath(relativePath)
                .fileSize(file.getSize())
                .mimeType(mimeType)
                .post(null)  // 아직 게시글에 연결되지 않음
                .uploader(uploader)
                .uploadedByEmail(uploaderEmail)
                .build();

        boardImageRepository.save(boardImage);

        // URL 생성
        String url = "/api/board/images/" + yearMonth + "/" + uniqueFileName;

        log.info("게시판 이미지 업로드 완료 - url: {}, imageId: {}", url, boardImage.getImageId());

        return new BoardImageDto.UploadResponse(
                uniqueFileName,
                url,
                file.getSize(),
                mimeType
        );
    }

    /**
     * 이미지 조회 (Resource 반환)
     *
     * @param year     년도
     * @param month    월
     * @param fileName 파일명
     * @return Resource
     */
    public Resource loadImage(int year, int month, String fileName) {
        log.debug("게시판 이미지 조회 - year: {}, month: {}, fileName: {}", year, month, fileName);

        String relativePath = String.format("%s/%d/%02d/%s", BOARD_IMAGES_DIR, year, month, fileName);

        return fileStorageService.loadFile(relativePath);
    }

    /**
     * 이미지 MIME 타입 조회
     *
     * @param year     년도
     * @param month    월
     * @param fileName 파일명
     * @return MIME 타입
     */
    public String getImageMimeType(int year, int month, String fileName) {
        String relativePath = String.format("%s/%d/%02d/%s", BOARD_IMAGES_DIR, year, month, fileName);
        Path absolutePath = fileStorageService.getAbsolutePath(relativePath);
        return FileContentUtil.getMimeType(absolutePath);
    }

    /**
     * 이미지 삭제
     *
     * @param year     년도
     * @param month    월
     * @param fileName 파일명
     */
    @Transactional
    public void deleteImage(int year, int month, String fileName) {
        log.info("게시판 이미지 삭제 요청 - year: {}, month: {}, fileName: {}", year, month, fileName);

        String relativePath = String.format("%s/%d/%02d/%s", BOARD_IMAGES_DIR, year, month, fileName);

        if (!fileStorageService.fileExists(relativePath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "이미지를 찾을 수 없습니다: " + fileName);
        }

        // 메타데이터 삭제
        boardImageRepository.findByFilePath(relativePath)
                .ifPresent(boardImageRepository::delete);

        // 파일 삭제
        fileStorageService.deleteFile(relativePath);

        log.info("게시판 이미지 삭제 완료 - path: {}", relativePath);
    }

    /**
     * 게시글 content에서 이미지 URL 추출하여 연결
     *
     * @param post    게시글
     * @param content 게시글 내용
     */
    @Transactional
    public void linkImagesToPost(BoardPost post, String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        List<String> filePaths = extractImageFilePaths(content);

        if (filePaths.isEmpty()) {
            return;
        }

        log.info("게시글 이미지 연결 - postId: {}, imageCount: {}", post.getPostId(), filePaths.size());

        List<BoardImage> images = boardImageRepository.findByFilePathIn(filePaths);

        for (BoardImage image : images) {
            image.linkToPost(post);
        }

        boardImageRepository.saveAll(images);
    }

    /**
     * 게시글의 이미지 연결 업데이트 (수정 시)
     *
     * @param post       게시글
     * @param newContent 새로운 게시글 내용
     */
    @Transactional
    public void updatePostImages(BoardPost post, String newContent) {
        Long postId = post.getPostId();

        // 기존 연결된 이미지 조회
        List<BoardImage> existingImages = boardImageRepository.findByPostPostId(postId);

        // 새 content에서 이미지 URL 추출
        List<String> newFilePaths = extractImageFilePaths(newContent);

        // 기존 이미지 중 새 content에 없는 것들 연결 해제
        for (BoardImage image : existingImages) {
            if (!newFilePaths.contains(image.getFilePath())) {
                image.unlinkFromPost();
            }
        }

        // 새 이미지 연결
        if (!newFilePaths.isEmpty()) {
            List<BoardImage> newImages = boardImageRepository.findByFilePathIn(newFilePaths);
            for (BoardImage image : newImages) {
                if (image.getPost() == null) {
                    image.linkToPost(post);
                }
            }
        }

        log.info("게시글 이미지 연결 업데이트 - postId: {}", postId);
    }

    /**
     * 게시글의 이미지 연결 해제 (삭제 시)
     *
     * @param postId 게시글 ID
     */
    @Transactional
    public void unlinkImagesFromPost(Long postId) {
        long count = boardImageRepository.unlinkImagesFromPost(postId);
        log.info("게시글 이미지 연결 해제 - postId: {}, count: {}", postId, count);
    }

    /**
     * 유령 이미지 삭제 (배치 작업용)
     *
     * @param hoursOld 업로드 후 경과 시간 (시간 단위)
     * @return 삭제된 이미지 수
     */
    @Transactional
    public int deleteOrphanedImages(int hoursOld) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hoursOld);

        log.info("유령 이미지 삭제 시작 - threshold: {}", threshold);

        // 미사용 이미지 조회
        List<BoardImage> orphanedImages = boardImageRepository
                .findByPostIsNullAndUploadedAtBefore(threshold);

        int deletedCount = 0;

        for (BoardImage image : orphanedImages) {
            try {
                // 파일 삭제
                if (fileStorageService.fileExists(image.getFilePath())) {
                    fileStorageService.deleteFile(image.getFilePath());
                }

                // 메타데이터 삭제
                boardImageRepository.delete(image);
                deletedCount++;

            } catch (Exception e) {
                log.error("유령 이미지 삭제 실패 - imageId: {}, path: {}, error: {}",
                        image.getImageId(), image.getFilePath(), e.getMessage());
            }
        }

        log.info("유령 이미지 삭제 완료 - deletedCount: {}", deletedCount);

        return deletedCount;
    }

    /**
     * content에서 첫 번째 이미지 URL 추출 (썸네일 자동 설정용)
     *
     * @param content 게시글 내용
     * @return 첫 번째 이미지 URL (없으면 null)
     */
    public String extractFirstImageUrl(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        Matcher matcher = IMAGE_URL_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(0);  // 전체 매칭된 URL 반환
        }

        return null;
    }

    /**
     * content에서 이미지 파일 경로 추출
     */
    private List<String> extractImageFilePaths(String content) {
        List<String> filePaths = new ArrayList<>();

        Matcher matcher = IMAGE_URL_PATTERN.matcher(content);
        while (matcher.find()) {
            String year = matcher.group(1);
            String month = matcher.group(2);
            String fileName = matcher.group(3);
            String filePath = BOARD_IMAGES_DIR + "/" + year + "/" + month + "/" + fileName;
            filePaths.add(filePath);
        }

        return filePaths;
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        // 빈 파일 체크
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일이 비어있습니다");
        }

        // 파일 크기 체크
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "파일 크기가 10MB를 초과합니다");
        }

        // 파일명 체크
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일명이 없습니다");
        }

        // 확장자 체크
        String extension = getExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "허용되지 않은 파일 형식입니다. 허용: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // MIME 타입 체크
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "허용되지 않은 이미지 형식입니다");
        }
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * 파일명 정제 (특수문자 제거, 공백을 언더스코어로)
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "image";
        }

        // 경로 구분자 제거
        String name = fileName.replace("\\", "/");
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }

        // 특수문자 제거 (영문, 숫자, 점, 언더스코어, 하이픈만 허용)
        name = name.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");

        // 연속된 언더스코어 제거
        name = name.replaceAll("_+", "_");

        // 빈 파일명 처리
        if (name.isBlank() || name.equals("_")) {
            return "image";
        }

        return name;
    }
}
