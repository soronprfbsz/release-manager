package com.ts.rm.domain.filesync.dto;

import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 메타데이터 공통 DTO
 *
 * <p>DB 레코드와 파일시스템 정보를 동일한 구조로 표현합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSyncMetadata {

    /** DB ID (파일시스템 스캔 시 null) */
    private Long id;

    /** 상대 경로 */
    private String filePath;

    /** 파일명 */
    private String fileName;

    /** 파일 크기 (바이트) */
    private Long fileSize;

    /** SHA-256 체크섬 */
    private String checksum;

    /** 파일시스템 수정일 */
    private LocalDateTime lastModified;

    /** DB 등록일 */
    private LocalDateTime registeredAt;

    /** 파일 유형 */
    private FileSyncTarget target;
}
