package com.ts.rm.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * EncryptionUtil 테스트
 */
@DisplayName("암호화 유틸리티 테스트")
class EncryptionUtilTest {

    @BeforeAll
    static void setUp() {
        // 테스트용 32바이트 암호화 키 설정
        EncryptionUtil.setSecretKey("test-secret-key-32-characters!");
    }

    @Test
    @DisplayName("평문을 암호화하고 복호화하면 원래 평문과 동일해야 한다")
    void encrypt_decrypt_shouldReturnOriginalText() {
        // given
        String plainText = "password123!@#";

        // when
        String encrypted = EncryptionUtil.encrypt(plainText);
        String decrypted = EncryptionUtil.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
        assertThat(encrypted).isNotEqualTo(plainText);
    }

    @Test
    @DisplayName("동일한 평문을 암호화하면 매번 다른 암호문이 생성되어야 한다 (IV 무작위)")
    void encrypt_sameTextMultipleTimes_shouldGenerateDifferentCipherText() {
        // given
        String plainText = "password123";

        // when
        String encrypted1 = EncryptionUtil.encrypt(plainText);
        String encrypted2 = EncryptionUtil.encrypt(plainText);

        // then
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(EncryptionUtil.decrypt(encrypted1)).isEqualTo(plainText);
        assertThat(EncryptionUtil.decrypt(encrypted2)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("null 값을 암호화하면 null을 반환해야 한다")
    void encrypt_null_shouldReturnNull() {
        // when
        String encrypted = EncryptionUtil.encrypt(null);

        // then
        assertThat(encrypted).isNull();
    }

    @Test
    @DisplayName("빈 문자열을 암호화하면 빈 문자열을 반환해야 한다")
    void encrypt_emptyString_shouldReturnEmptyString() {
        // when
        String encrypted = EncryptionUtil.encrypt("");

        // then
        assertThat(encrypted).isEmpty();
    }

    @Test
    @DisplayName("한글, 특수문자 포함 평문도 정상적으로 암호화/복호화되어야 한다")
    void encrypt_decrypt_koreanAndSpecialChars_shouldWork() {
        // given
        String plainText = "비밀번호!@#$%^&*()_+한글123";

        // when
        String encrypted = EncryptionUtil.encrypt(plainText);
        String decrypted = EncryptionUtil.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("긴 문자열도 정상적으로 암호화/복호화되어야 한다")
    void encrypt_decrypt_longText_shouldWork() {
        // given
        String plainText = "a".repeat(500);

        // when
        String encrypted = EncryptionUtil.encrypt(plainText);
        String decrypted = EncryptionUtil.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
        assertThat(decrypted).hasSize(500);
    }

    @Test
    @DisplayName("잘못된 암호문을 복호화하면 예외가 발생해야 한다")
    void decrypt_invalidCipherText_shouldThrowException() {
        // given
        String invalidCipherText = "invalid-cipher-text";

        // when & then
        assertThatThrownBy(() -> EncryptionUtil.decrypt(invalidCipherText))
                .isInstanceOf(EncryptionUtil.EncryptionException.class)
                .hasMessageContaining("복호화에 실패했습니다");
    }
}
