package com.ts.rm.domain.maintenance.service;

import com.ts.rm.domain.board.entity.BoardImage;
import com.ts.rm.domain.board.repository.BoardImageRepository;
import com.ts.rm.domain.maintenance.dto.MaintenanceResultDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Board Image Cleanup Service
 *
 * <p>게시판 유령 이미지 정리 서비스
 * <p>post_id가 NULL이고 업로드 후 일정 시간이 지난 이미지를 삭제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardImageCleanupService {

    private final BoardImageRepository imageRepository;

    @Value("${release.base-path:data/release-manager}")
    private String basePath;

    /**
     * 유령 이미지 정리
     *
     * @param retentionHours 보관 시간 (시간)
     * @return 정리 결과
     */
    @Transactional
    public MaintenanceResultDto.CleanupResult cleanupOrphanImages(int retentionHours) {
        log.info("게시판 유령 이미지 정리 시작 - retentionHours: {}", retentionHours);

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(retentionHours);

        // 유령 이미지 조회 (post_id IS NULL AND uploaded_at < cutoffTime)
        List<BoardImage> orphanImages = imageRepository.findByPostIsNullAndUploadedAtBefore(cutoffTime);

        if (orphanImages.isEmpty()) {
            log.info("삭제할 유령 이미지 없음");
            return MaintenanceResultDto.CleanupResult.of(
                    "board-image-cleanup",
                    0,
                    "삭제할 유령 이미지가 없습니다.");
        }

        int deletedCount = 0;
        long deletedSizeBytes = 0;

        for (BoardImage image : orphanImages) {
            try {
                // 파일 삭제
                Path filePath = Path.of(basePath, image.getFilePath());
                if (Files.exists(filePath)) {
                    deletedSizeBytes += Files.size(filePath);
                    Files.delete(filePath);
                    log.debug("파일 삭제 - path: {}", filePath);
                }

                // DB 레코드 삭제
                imageRepository.delete(image);
                deletedCount++;

            } catch (IOException e) {
                log.warn("파일 삭제 실패 - imageId: {}, path: {}, error: {}",
                        image.getImageId(), image.getFilePath(), e.getMessage());
            }
        }

        String message = String.format("%d시간 이상 미사용 이미지 %d건 삭제 완료 (%.2f MB)",
                retentionHours, deletedCount, deletedSizeBytes / (1024.0 * 1024.0));

        log.info("게시판 유령 이미지 정리 완료 - {}", message);

        return MaintenanceResultDto.CleanupResult.of(
                "board-image-cleanup",
                deletedCount,
                deletedSizeBytes,
                message);
    }
}
