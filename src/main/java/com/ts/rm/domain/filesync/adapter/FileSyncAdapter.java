package com.ts.rm.domain.filesync.adapter;

import com.ts.rm.domain.filesync.dto.FileSyncMetadata;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * 파일 동기화 어댑터 인터페이스
 *
 * <p>각 파일 유형(ReleaseFile, ResourceFile, BackupFile)에서 구현합니다.
 * FileSyncService는 이 인터페이스를 통해 도메인별 로직에 접근합니다.
 */
public interface FileSyncAdapter {

    /**
     * 동기화 대상 유형 반환
     *
     * @return 이 어댑터가 담당하는 파일 유형
     */
    FileSyncTarget getTarget();

    /**
     * 스캔 기준 경로 반환 (상대 경로)
     *
     * <p>예: "versions", "resource", "job"
     *
     * @return 스캔 시작 경로
     */
    String getBaseScanPath();

    /**
     * DB에 등록된 파일 메타데이터 목록 조회
     *
     * @param subPath 세부 경로 필터 (null이면 전체 조회)
     * @return 등록된 파일 메타데이터 목록
     */
    List<FileSyncMetadata> getRegisteredFiles(@Nullable String subPath);

    /**
     * 미등록 파일을 DB에 신규 등록
     *
     * @param metadata 파일 메타데이터
     * @param additionalData 추가 데이터 (도메인별로 필요한 정보)
     * @return 생성된 레코드의 ID
     */
    Long registerFile(FileSyncMetadata metadata, @Nullable Map<String, Object> additionalData);

    /**
     * 메타데이터 갱신 (실제 파일 정보로 DB 업데이트)
     *
     * @param id DB 레코드 ID
     * @param newMetadata 갱신할 메타데이터
     */
    void updateMetadata(Long id, FileSyncMetadata newMetadata);

    /**
     * 메타데이터 삭제
     *
     * @param id DB 레코드 ID
     */
    void deleteMetadata(Long id);

    /**
     * 파일 확장자 필터 (스캔 시 적용)
     *
     * <p>null 반환 시 모든 파일 포함
     *
     * @return 허용할 확장자 목록 (예: [".sql", ".sh"])
     */
    @Nullable
    default List<String> getAllowedExtensions() {
        return null;
    }

    /**
     * 스캔 제외 디렉토리
     *
     * <p>null 반환 시 제외 없음
     *
     * @return 제외할 디렉토리 이름 목록
     */
    @Nullable
    default List<String> getExcludedDirectories() {
        return null;
    }

    /**
     * 주어진 경로가 동기화 대상으로 유효한지 확인
     *
     * <p>예: ReleaseFile의 경우, 해당 경로에 대응하는 ReleaseVersion이 DB에 존재하는지 확인합니다.
     * 존재하지 않는 버전의 파일은 UNREGISTERED로 간주하지 않습니다.
     *
     * @param filePath 확인할 파일 경로
     * @return true면 동기화 대상, false면 무시
     */
    default boolean isValidSyncPath(String filePath) {
        return true;
    }

    /**
     * 폴더 기반 동기화 여부
     *
     * <p>true 반환 시 파일시스템 스캔 시 폴더(디렉토리)를 대상으로 합니다.
     * <p>예: Patch는 폴더 단위로 관리되므로 true를 반환합니다.
     *
     * @return true면 폴더 스캔, false면 파일 스캔 (기본값)
     */
    default boolean isFolderBased() {
        return false;
    }

    /**
     * 폴더 스캔 시 깊이 제한
     *
     * <p>isFolderBased()가 true일 때만 적용됩니다.
     * <p>1 = baseScanPath 바로 아래의 폴더만 스캔
     *
     * @return 스캔할 폴더 깊이 (기본값: 1)
     */
    default int getFolderScanDepth() {
        return 1;
    }
}
