package com.ts.rm.global.file;

import java.nio.file.Path;

/**
 * ZIP 파일 압축 유틸리티
 *
 * <p>ZIP 엔트리 정보를 담는 레코드 클래스만 제공합니다.
 * 실제 압축 기능은 {@link StreamingZipUtil}을 사용하세요.
 */
public class ZipUtil {

    private ZipUtil() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * ZIP 엔트리 정보 (파일 경로와 ZIP 내 경로 매핑)
     *
     * @param sourcePath   실제 파일 경로
     * @param zipEntryPath ZIP 내부 경로 (예: database/mariadb/1.patch.sql)
     */
    public record ZipFileEntry(Path sourcePath, String zipEntryPath) {
    }
}
