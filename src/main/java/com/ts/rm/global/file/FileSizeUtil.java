package com.ts.rm.global.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 파일 크기 포맷팅 유틸리티
 *
 * <p>파일 크기를 사람이 읽기 쉬운 형태로 변환합니다.
 *
 * <p>사용 예시:
 * <pre>
 * long fileSize = 1536;
 * String formatted = FileSizeUtil.formatBytes(fileSize); // "1.5 KB"
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileSizeUtil {

    private static final long BYTE = 1L;
    private static final long KB = BYTE << 10;
    private static final long MB = KB << 10;
    private static final long GB = MB << 10;
    private static final long TB = GB << 10;

    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 포맷팅
     *
     * @param bytes 파일 크기 (bytes)
     * @return 포맷된 문자열 (예: "1.5 KB", "2.3 MB")
     */
    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("파일 크기는 음수일 수 없습니다: " + bytes);
        }

        if (bytes < KB) {
            return bytes + " B";
        } else if (bytes < MB) {
            return formatSize(bytes, KB, "KB");
        } else if (bytes < GB) {
            return formatSize(bytes, MB, "MB");
        } else if (bytes < TB) {
            return formatSize(bytes, GB, "GB");
        } else {
            return formatSize(bytes, TB, "TB");
        }
    }

    /**
     * 크기 포맷팅 헬퍼 메서드
     */
    private static String formatSize(long bytes, long unit, String unitName) {
        double value = (double) bytes / unit;
        // 소수점 1자리까지 표시, 정수인 경우 소수점 생략
        if (value == Math.floor(value)) {
            return String.format("%d %s", (long) value, unitName);
        } else {
            return String.format("%.1f %s", value, unitName);
        }
    }
}
