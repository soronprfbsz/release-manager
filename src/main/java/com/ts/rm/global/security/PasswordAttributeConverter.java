package com.ts.rm.global.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호 필드 자동 암호화/복호화 JPA Converter
 *
 * <p>Entity의 비밀번호 필드에 {@code @Convert(converter = PasswordAttributeConverter.class)} 적용 시
 * JPA가 자동으로 암호화/복호화를 처리합니다.
 *
 * <p>동작:
 * <ul>
 *   <li>DB 저장 시: 평문 → 암호문</li>
 *   <li>DB 조회 시: 암호문 → 평문</li>
 * </ul>
 *
 * <p>사용 예시:
 * <pre>
 * {@code @Convert(converter = PasswordAttributeConverter.class)}
 * {@code @Column(name = "password", length = 1000)}
 * private String password;
 * </pre>
 */
@Slf4j
@Converter
public class PasswordAttributeConverter implements AttributeConverter<String, String> {

    /**
     * Entity → Database 변환 (평문 → 암호문)
     *
     * @param attribute Entity의 평문 비밀번호
     * @return 암호화된 비밀번호
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }

        try {
            String encrypted = EncryptionUtil.encrypt(attribute);
            log.debug("비밀번호 암호화 완료");
            return encrypted;
        } catch (Exception e) {
            log.error("비밀번호 암호화 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Database → Entity 변환 (암호문 → 평문)
     *
     * @param dbData DB의 암호화된 비밀번호
     * @return 복호화된 평문 비밀번호
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }

        try {
            String decrypted = EncryptionUtil.decrypt(dbData);
            log.debug("비밀번호 복호화 완료");
            return decrypted;
        } catch (Exception e) {
            log.error("비밀번호 복호화 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}
