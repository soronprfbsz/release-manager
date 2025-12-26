package com.ts.rm.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 파일 타입/확장자 추출 유틸리티 클래스
 *
 * <p>파일명에서 확장자를 추출하는 기능을 제공합니다.
 *
 * <p>사용 예시:
 * <pre>{@code
 * String fileType = FileTypeExtractor.extractFileType("document.pdf");  // "PDF"
 * String extension = FileTypeExtractor.extractExtension("document.pdf"); // "pdf"
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileTypeExtractor {

    private static final String UNKNOWN_TYPE = "UNKNOWN";

    /**
     * 파일명에서 확장자 추출 (대문자)
     *
     * <p>파일명에서 마지막 점(.) 이후의 문자열을 대문자로 반환합니다.
     *
     * @param fileName 파일명
     * @return 확장자 (대문자) 또는 "UNKNOWN"
     */
    public static String extractFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return UNKNOWN_TYPE;
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == fileName.length() - 1) {
            return UNKNOWN_TYPE;
        }
        return fileName.substring(lastDot + 1).toUpperCase();
    }

    /**
     * 파일명에서 확장자 추출 (소문자)
     *
     * @param fileName 파일명
     * @return 확장자 (소문자) 또는 null
     */
    public static String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }
}
