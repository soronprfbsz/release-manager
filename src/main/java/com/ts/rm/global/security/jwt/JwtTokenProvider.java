package com.ts.rm.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증을 담당하는 컴포넌트
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationTime;
    private final long refreshExpirationTime;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expirationTime,
            @Value("${app.jwt.refresh-expiration}") long refreshExpirationTime,
            @Value("${app.jwt.issuer}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
        this.issuer = issuer;
    }

    /**
     * JWT 토큰 생성
     *
     * @param accountId    계정 ID
     * @param role         사용자 권한
     * @param departmentId 부서 ID (null 가능)
     * @return 생성된 JWT 토큰
     */
    public String generateToken(Long accountId, String role, Long departmentId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        var builder = Jwts.builder()
                .subject(String.valueOf(accountId))
                .claim("role", role)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate);

        // 부서 ID 추가 (null이 아닌 경우에만)
        if (departmentId != null) {
            builder.claim("departmentId", departmentId);
        }

        return builder.signWith(secretKey, Jwts.SIG.HS256).compact();
    }

    /**
     * JWT 토큰 검증
     *
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰에서 Claims 추출
     *
     * @param token JWT 토큰
     * @return Claims 객체
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT 토큰에서 계정 ID 추출
     *
     * @param token JWT 토큰
     * @return 계정 ID
     */
    public Long getAccountId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * JWT 토큰에서 권한 추출
     *
     * @param token JWT 토큰
     * @return 사용자 권한
     */
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * JWT 토큰에서 부서 ID 추출
     *
     * @param token JWT 토큰
     * @return 부서 ID (없으면 null)
     */
    public Long getDepartmentId(String token) {
        return getClaims(token).get("departmentId", Long.class);
    }

    /**
     * Refresh Token 생성
     *
     * @param email 사용자 이메일
     * @return 생성된 Refresh Token
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .subject(email)
                .claim("type", "refresh")
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Refresh Token 검증
     *
     * @param token Refresh Token
     * @return 유효성 여부
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // type이 "refresh"인지 확인
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (SignatureException e) {
            log.error("Invalid Refresh Token signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid Refresh Token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired Refresh Token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported Refresh Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Refresh Token claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰 만료 시간(초) 반환
     *
     * @return 토큰 만료 시간 (초 단위)
     */
    public long getExpirationInSeconds() {
        return expirationTime / 1000;
    }

    /**
     * Refresh Token 만료 시간(초) 반환
     *
     * @return Refresh Token 만료 시간 (초 단위)
     */
    public long getRefreshExpirationInSeconds() {
        return refreshExpirationTime / 1000;
    }

    /**
     * Refresh Token 만료 시간(밀리초) 반환
     *
     * @return Refresh Token 만료 시간 (밀리초 단위)
     */
    public long getRefreshExpirationInMillis() {
        return refreshExpirationTime;
    }
}
