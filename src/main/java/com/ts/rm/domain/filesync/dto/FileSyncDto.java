package com.ts.rm.domain.filesync.dto;

import com.ts.rm.domain.filesync.enums.FileSyncAction;
import com.ts.rm.domain.filesync.enums.FileSyncStatus;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 동기화 API Request/Response DTO
 */
public class FileSyncDto {

    private FileSyncDto() {
    }

    // ========================================
    // 분석 API (analyze)
    // ========================================

    /**
     * 분석 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyzeRequest {

        /** 동기화 대상 (null이면 전체) */
        private List<FileSyncTarget> targets;

        /** 스캔 기준 경로 (null이면 각 대상의 기본 경로) */
        private String basePath;
    }

    /**
     * 분석 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyzeResponse {

        /** 분석 시각 */
        private LocalDateTime analyzedAt;

        /** 요약 정보 */
        private Summary summary;

        /** 대상별 불일치 건수 */
        private Map<FileSyncTarget, Integer> discrepanciesByTarget;

        /** 불일치 항목 목록 */
        private List<FileSyncDiscrepancy> discrepancies;
    }

    /**
     * 분석 요약 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {

        /** 총 스캔 파일 수 */
        private int totalScanned;

        /** 동기화된 파일 수 */
        private int synced;

        /** 불일치 파일 수 */
        private int discrepancies;
    }

    // ========================================
    // 적용 API (apply)
    // ========================================

    /**
     * 적용 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyRequest {

        /** 적용할 액션 목록 */
        @NotEmpty(message = "적용할 액션 목록이 비어있습니다")
        @Valid
        private List<ActionItem> actions;
    }

    /**
     * 적용할 개별 액션 항목
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionItem {

        /** 불일치 항목 ID */
        @NotBlank(message = "불일치 항목 ID가 필요합니다")
        private String id;

        /** 수행할 액션 */
        @NotNull(message = "수행할 액션이 필요합니다")
        private FileSyncAction action;

        /** 추가 메타데이터 (REGISTER 시 사용) */
        private Map<String, Object> metadata;
    }

    /**
     * 적용 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyResponse {

        /** 적용 시각 */
        private LocalDateTime appliedAt;

        /** 개별 결과 목록 */
        private List<ActionResult> results;

        /** 요약 정보 */
        private ApplySummary summary;
    }

    /**
     * 개별 액션 실행 결과
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionResult {

        /** 불일치 항목 ID */
        private String id;

        /** 파일 경로 */
        private String filePath;

        /** 수행한 액션 */
        private FileSyncAction action;

        /** 성공 여부 */
        private boolean success;

        /** 결과 메시지 */
        private String message;
    }

    /**
     * 적용 요약 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplySummary {

        /** 총 처리 건수 */
        private int total;

        /** 성공 건수 */
        private int success;

        /** 실패 건수 */
        private int failed;
    }

    // ========================================
    // 무시 목록 API (ignore)
    // ========================================

    /**
     * 무시된 파일 항목
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IgnoredFile {

        /** 무시 항목 ID */
        private Long ignoreId;

        /** 파일 경로 */
        private String filePath;

        /** 대상 유형 */
        private FileSyncTarget targetType;

        /** 무시 당시 상태 */
        private FileSyncStatus status;

        /** 무시 처리자 */
        private String ignoredBy;

        /** 등록일시 */
        private LocalDateTime createdAt;
    }
}
