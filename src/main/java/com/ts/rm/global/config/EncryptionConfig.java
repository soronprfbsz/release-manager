package com.ts.rm.global.config;

import com.ts.rm.global.security.EncryptionUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 암호화 설정
 *
 * <p>애플리케이션 시작 시 암호화 키를 EncryptionUtil에 주입합니다.
 */
@Slf4j
@Configuration
public class EncryptionConfig {

    @Value("${encryption.secret-key}")
    private String secretKey;

    /**
     * 애플리케이션 시작 시 암호화 키 설정
     */
    @PostConstruct
    public void init() {
        try {
            log.info("암호화 키 로드 중... (길이: {} 문자)", secretKey != null ? secretKey.length() : "null");
            EncryptionUtil.setSecretKey(secretKey);
            log.info("암호화 설정 초기화 완료");
        } catch (Exception e) {
            log.error("암호화 설정 초기화 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("암호화 설정 초기화에 실패했습니다. 암호화 키를 확인하세요.", e);
        }
    }
}
