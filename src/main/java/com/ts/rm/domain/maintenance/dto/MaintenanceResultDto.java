package com.ts.rm.domain.maintenance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Maintenance Result DTO
 */
public final class MaintenanceResultDto {

    private MaintenanceResultDto() {
    }

    /**
     * 정리 작업 결과 응답
     */
    @Schema(description = "정리 작업 결과")
    public record CleanupResult(
            @Schema(description = "작업 유형", example = "board-image-cleanup")
            String taskType,

            @Schema(description = "삭제된 항목 수", example = "15")
            int deletedCount,

            @Schema(description = "삭제된 파일 크기 (bytes)", example = "1048576")
            Long deletedSizeBytes,

            @Schema(description = "실행 시각")
            LocalDateTime executedAt,

            @Schema(description = "메시지", example = "24시간 이상 미사용 이미지 15건 삭제 완료")
            String message
    ) {

        public static CleanupResult of(String taskType, int deletedCount, String message) {
            return new CleanupResult(taskType, deletedCount, null, LocalDateTime.now(), message);
        }

        public static CleanupResult of(String taskType, int deletedCount, Long deletedSizeBytes, String message) {
            return new CleanupResult(taskType, deletedCount, deletedSizeBytes, LocalDateTime.now(), message);
        }
    }
}
