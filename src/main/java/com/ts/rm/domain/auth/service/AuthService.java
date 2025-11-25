package com.ts.rm.domain.auth.service;

import com.ts.rm.domain.auth.dto.SignInRequest;
import com.ts.rm.domain.auth.dto.SignUpRequest;
import com.ts.rm.domain.auth.dto.SignUpResponse;
import com.ts.rm.domain.auth.dto.TokenResponse;

/**
 * 인증 서비스 인터페이스
 */
public interface AuthService {

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     */
    SignUpResponse signUp(SignUpRequest request);

    /**
     * 로그인
     *
     * @param request 로그인 요청 DTO
     * @return TokenResponse (Access Token + Refresh Token)
     */
    TokenResponse signIn(SignInRequest request);

    /**
     * 로그아웃
     *
     * @param refreshToken Refresh Token 문자열
     */
    void logout(String refreshToken);
}
