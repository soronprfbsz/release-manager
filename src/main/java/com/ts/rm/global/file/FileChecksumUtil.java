package com.ts.rm.global.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 파일 체크섬 유틸리티
 *
 * <p>파일의 SHA-256 체크섬을 계산합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileChecksumUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192;

    /**
     * 파일의 SHA-256 체크섬 계산
     *
     * @param filePath 파일 경로
     * @return SHA-256 체크섬 (hex string)
     * @throws IOException 파일 읽기 실패 시
     */
    public static String calculateChecksum(Path filePath) throws IOException {
        try (InputStream is = Files.newInputStream(filePath)) {
            return calculateChecksum(is);
        }
    }

    /**
     * InputStream의 SHA-256 체크섬 계산
     *
     * @param inputStream 입력 스트림
     * @return SHA-256 체크섬 (hex string)
     * @throws IOException 스트림 읽기 실패 시
     */
    public static String calculateChecksum(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
