package com.ts.rm.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret;
    private long testExpiration;
    private long testRefreshExpiration;
    private String testIssuer;

    @BeforeEach
    void setUp() {
        testSecret = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
        testExpiration = 3600000L; // 1시간
        testRefreshExpiration = 604800000L; // 7일
        testIssuer = "test-issuer";
        jwtTokenProvider = new JwtTokenProvider(testSecret, testExpiration, testRefreshExpiration, testIssuer);
    }

    @Test
    @DisplayName("JWT 토큰 생성 성공")
    void generateToken_Success() {
        // given
        String email = "test@example.com";
        String role = "USER";

        // when
        String token = jwtTokenProvider.generateToken(email, role);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 형태
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 성공")
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        String email = "test@example.com";
        String role = "USER";
        String token = jwtTokenProvider.generateToken(email, role);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 형식의 JWT 토큰 검증 실패")
    void validateToken_MalformedToken_ReturnsFalse() {
        // given
        String malformedToken = "invalid.token.format";

        // when
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_EmptyToken_ReturnsFalse() {
        // given
        String emptyToken = "";

        // when
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("JWT 토큰에서 이메일 추출 성공")
    void getEmail_ValidToken_ReturnsEmail() {
        // given
        String expectedEmail = "test@example.com";
        String role = "USER";
        String token = jwtTokenProvider.generateToken(expectedEmail, role);

        // when
        String actualEmail = jwtTokenProvider.getEmail(token);

        // then
        assertThat(actualEmail).isEqualTo(expectedEmail);
    }

    @Test
    @DisplayName("JWT 토큰에서 권한 추출 성공")
    void getRole_ValidToken_ReturnsRole() {
        // given
        String email = "test@example.com";
        String expectedRole = "ADMIN";
        String token = jwtTokenProvider.generateToken(email, expectedRole);

        // when
        String actualRole = jwtTokenProvider.getRole(token);

        // then
        assertThat(actualRole).isEqualTo(expectedRole);
    }

    @Test
    @DisplayName("JWT 토큰에서 Claims 추출 성공")
    void getClaims_ValidToken_ReturnsClaims() {
        // given
        String email = "test@example.com";
        String role = "USER";
        String token = jwtTokenProvider.generateToken(email, role);

        // when
        Claims claims = jwtTokenProvider.getClaims(token);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(email);
        assertThat(claims.get("role", String.class)).isEqualTo(role);
        assertThat(claims.getIssuer()).isEqualTo(testIssuer);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("토큰 만료 시간 확인")
    void getExpirationInSeconds_ReturnsCorrectValue() {
        // when
        long expirationInSeconds = jwtTokenProvider.getExpirationInSeconds();

        // then
        assertThat(expirationInSeconds).isEqualTo(3600L); // 3600000ms = 3600s
    }

    @Test
    @DisplayName("만료 시간이 올바르게 설정됨")
    void generateToken_ExpirationTimeIsCorrect() throws InterruptedException {
        // given
        String email = "test@example.com";
        String role = "USER";
        long beforeGeneration = System.currentTimeMillis();

        // when
        String token = jwtTokenProvider.generateToken(email, role);
        Claims claims = jwtTokenProvider.getClaims(token);

        // then
        long tokenExpiration = claims.getExpiration().getTime();
        long expectedExpiration = beforeGeneration + testExpiration;

        // 오차 범위 1초 이내 허용
        assertThat(tokenExpiration).isBetween(expectedExpiration - 1000, expectedExpiration + 1000);
    }
}
