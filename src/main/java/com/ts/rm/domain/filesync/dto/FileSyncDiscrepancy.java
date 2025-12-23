package com.ts.rm.domain.filesync.dto;

import com.ts.rm.domain.filesync.enums.FileSyncAction;
import com.ts.rm.domain.filesync.enums.FileSyncStatus;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 동기화 불일치 항목 DTO
 *
 * <p>파일시스템과 DB 간 불일치가 발견된 항목의 정보를 담습니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSyncDiscrepancy {

    /** 고유 식별자 (UUID) */
    private String id;

    /** 파일 유형 */
    private FileSyncTarget target;

    /** 파일 유형 이름 */
    private String targetName;

    /** 파일 경로 (상대 경로) */
    private String filePath;

    /** 파일명 */
    private String fileName;

    /** 동기화 상태 */
    private FileSyncStatus status;

    /** 상태 메시지 */
    private String message;

    /** 파일시스템 정보 (파일이 있는 경우) */
    private FileInfo fileInfo;

    /** DB 메타데이터 정보 (DB에 있는 경우) */
    private DbInfo dbInfo;

    /** 선택 가능한 액션 목록 */
    private List<FileSyncAction> availableActions;

    /**
     * 파일시스템 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private Long size;
        private String checksum;
        private String lastModified;
    }

    /**
     * DB 메타데이터 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DbInfo {
        private Long id;
        private Long size;
        private String checksum;
        private String registeredAt;
    }
}
