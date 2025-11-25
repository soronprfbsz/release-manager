package com.ts.rm.domain.auth.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.domain.auth.dto.SignInRequest;
import com.ts.rm.domain.auth.dto.SignUpRequest;
import com.ts.rm.domain.auth.dto.SignUpResponse;
import com.ts.rm.domain.auth.dto.TokenResponse;
import com.ts.rm.domain.refreshtoken.entity.RefreshToken;
import com.ts.rm.domain.refreshtoken.service.RefreshTokenService;
import com.ts.rm.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    private static final String ACCOUNT_ROLE_USER = "ACCOUNT_ROLE_USER";
    private static final String ACCOUNT_STATUS_ACTIVE = "ACCOUNT_STATUS_ACTIVE";

    @Override
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 1. 이메일 중복 체크
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 2. 계정 생성
        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountName(request.getAccountName())
                .role(ACCOUNT_ROLE_USER)
                .status(ACCOUNT_STATUS_ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("New account created: {}", savedAccount.getEmail());

        // 3. 응답 생성
        return SignUpResponse.builder()
                .accountId(savedAccount.getAccountId())
                .email(savedAccount.getEmail())
                .accountName(savedAccount.getAccountName())
                .role(savedAccount.getRole())
                .createdAt(savedAccount.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public TokenResponse signIn(SignInRequest request) {
        // 1. 계정 조회
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. Access Token 생성
        String accessToken = jwtTokenProvider.generateToken(account.getEmail(), account.getRole());

        // 4. Refresh Token 생성 및 저장
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(account);

        log.info("User signed in: {}", account.getEmail());

        // 5. 응답 생성
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationInSeconds())
                .refreshExpiresIn(jwtTokenProvider.getRefreshExpirationInSeconds())
                .accountInfo(TokenResponse.AccountInfo.builder()
                        .accountId(account.getAccountId())
                        .email(account.getEmail())
                        .accountName(account.getAccountName())
                        .role(account.getRole())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
        log.info("User logged out");
    }
}
