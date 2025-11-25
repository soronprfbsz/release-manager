package com.ts.rm.domain.refreshtoken.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.auth.dto.TokenResponse;
import com.ts.rm.domain.refreshtoken.entity.RefreshToken;

/**
 * Refresh Token 서비스 인터페이스
 */
public interface RefreshTokenService {

    /**
     * Refresh Token 생성 및 저장
     *
     * @param account 계정 엔티티
     * @return 생성된 RefreshToken 엔티티
     */
    RefreshToken createRefreshToken(Account account);

    /**
     * Refresh Token으로 Access Token 재발급
     *
     * @param refreshToken Refresh Token 문자열
     * @return 새로운 TokenResponse (accessToken + refreshToken)
     */
    TokenResponse refreshAccessToken(String refreshToken);

    /**
     * Refresh Token 삭제 (로그아웃)
     *
     * @param refreshToken Refresh Token 문자열
     */
    void deleteRefreshToken(String refreshToken);

    /**
     * 계정 ID로 모든 Refresh Token 삭제
     *
     * @param accountId 계정 ID
     */
    void deleteAllByAccountId(Long accountId);

    /**
     * 만료된 Refresh Token 일괄 삭제
     *
     * @return 삭제된 레코드 수
     */
    int deleteExpiredTokens();
}
