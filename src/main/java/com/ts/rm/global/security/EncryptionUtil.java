package com.ts.rm.global.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AES-256 암호화/복호화 유틸리티
 *
 * <p>민감한 데이터(비밀번호 등)를 암호화하여 DB에 저장하고 복호화하여 사용합니다.
 *
 * <p>특징:
 * <ul>
 *   <li>AES-256 CBC 모드 사용</li>
 *   <li>IV(Initialization Vector) 무작위 생성으로 동일 평문도 매번 다른 암호문 생성</li>
 *   <li>Base64 인코딩으로 DB 저장 가능한 문자열 변환</li>
 * </ul>
 *
 * <p>사용 예시:
 * <pre>
 * String encrypted = EncryptionUtil.encrypt("password123");
 * String decrypted = EncryptionUtil.decrypt(encrypted);
 * </pre>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptionUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_SIZE = 16; // 128 bits

    private static String secretKey;

    /**
     * 암호화 키 설정 (애플리케이션 시작 시 한 번만 호출)
     *
     * @param key 32바이트(256비트) 암호화 키
     */
    public static void setSecretKey(String key) {
        if (key == null || key.length() != 32) {
            throw new IllegalArgumentException("암호화 키는 정확히 32자(256비트)여야 합니다");
        }
        secretKey = key;
        log.info("암호화 키가 설정되었습니다");
    }

    /**
     * 평문을 암호화합니다
     *
     * @param plainText 암호화할 평문
     * @return Base64로 인코딩된 암호문 (IV 포함)
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        validateSecretKey();

        try {
            // IV 생성
            byte[] iv = generateIv();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 암호화 키 생성
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    KEY_ALGORITHM
            );

            // 암호화 수행
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 암호문을 합쳐서 Base64 인코딩
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("암호화 실패: {}", e.getMessage(), e);
            throw new EncryptionException("암호화에 실패했습니다", e);
        }
    }

    /**
     * 암호문을 복호화합니다
     *
     * @param cipherText Base64로 인코딩된 암호문 (IV 포함)
     * @return 복호화된 평문
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        validateSecretKey();

        try {
            // Base64 디코딩
            byte[] combined = Base64.getDecoder().decode(cipherText);

            // IV와 암호문 분리
            byte[] iv = new byte[IV_SIZE];
            byte[] encrypted = new byte[combined.length - IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, IV_SIZE);
            System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 복호화 키 생성
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    KEY_ALGORITHM
            );

            // 복호화 수행
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("복호화 실패: {}", e.getMessage(), e);
            throw new EncryptionException("복호화에 실패했습니다", e);
        }
    }

    /**
     * 무작위 IV 생성
     */
    private static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    /**
     * 암호화 키 설정 여부 검증
     */
    private static void validateSecretKey() {
        if (secretKey == null) {
            throw new IllegalStateException("암호화 키가 설정되지 않았습니다. EncryptionConfig를 확인하세요.");
        }
    }

    /**
     * 암호화/복호화 예외
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
