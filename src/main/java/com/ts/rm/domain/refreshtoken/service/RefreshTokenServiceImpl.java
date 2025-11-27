package com.ts.rm.domain.refreshtoken.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.auth.dto.TokenResponse;
import com.ts.rm.domain.refreshtoken.entity.RefreshToken;
import com.ts.rm.domain.refreshtoken.repository.RefreshTokenRepository;
import com.ts.rm.global.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Refresh Token 서비스 구현체 (Redis 기반)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public RefreshToken createRefreshToken(Account account) {
        // 1. 기존 Refresh Token 삭제 (한 계정당 하나의 Refresh Token만 유지)
        refreshTokenRepository.deleteByAccountId(account.getAccountId());

        // 2. 새 Refresh Token 생성
        String tokenValue = jwtTokenProvider.generateRefreshToken(account.getEmail());
        long ttlInSeconds = jwtTokenProvider.getRefreshExpirationInSeconds();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .accountName(account.getAccountName())
                .role(account.getRole())
                .createdAt(LocalDateTime.now())
                .ttl(ttlInSeconds)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh Token created for account: {} (TTL: {}s)", account.getEmail(), ttlInSeconds);

        return savedToken;
    }

    @Override
    public TokenResponse refreshAccessToken(String refreshTokenValue) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshTokenValue)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. Redis에서 Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findById(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("존재하지 않는 리프레시 토큰입니다."));

        // 3. 계정 정보 (Redis에서 추출)
        String email = refreshToken.getEmail();
        String accountName = refreshToken.getAccountName();
        String role = refreshToken.getRole();
        Long accountId = refreshToken.getAccountId();

        // 4. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateToken(email, role);

        // 5. Refresh Token Rotation: 기존 토큰 삭제 및 새 토큰 발급
        refreshTokenRepository.deleteById(refreshTokenValue);

        // Account 객체 재생성 (Redis에서는 Account 연관관계 없음)
        Account account = Account.builder()
                .email(email)
                .accountName(accountName)
                .role(role)
                .build();
        account.setAccountId(accountId);

        RefreshToken newRefreshToken = createRefreshToken(account);

        log.info("Token refreshed for account: {}", email);

        // 6. 응답 생성
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .refreshExpiresIn(jwtTokenProvider.getRefreshExpirationInSeconds())
                .accountInfo(TokenResponse.AccountInfo.builder()
                        .accountId(accountId)
                        .email(email)
                        .accountName(accountName)
                        .role(role)
                        .build())
                .build();
    }

    @Override
    public void deleteRefreshToken(String refreshTokenValue) {
        if (refreshTokenRepository.existsById(refreshTokenValue)) {
            refreshTokenRepository.deleteById(refreshTokenValue);
            String maskedToken = refreshTokenValue.length() > 20
                    ? refreshTokenValue.substring(0, 20) + "..."
                    : refreshTokenValue.substring(0, Math.min(refreshTokenValue.length(), 10)) + "***";
            log.info("Refresh Token deleted: {}", maskedToken);
        }
    }

    @Override
    public void deleteAllByAccountId(Long accountId) {
        refreshTokenRepository.deleteByAccountId(accountId);
        log.info("All Refresh Tokens deleted for account ID: {}", accountId);
    }

    @Override
    public int deleteExpiredTokens() {
        // Redis TTL이 자동으로 만료 처리하므로 별도 구현 불필요
        log.debug("Redis TTL handles expired token cleanup automatically");
        return 0;
    }
}
