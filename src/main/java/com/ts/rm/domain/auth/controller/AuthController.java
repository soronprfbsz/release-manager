package com.ts.rm.domain.auth.controller;

import com.ts.rm.domain.auth.dto.AccessTokenResponse;
import com.ts.rm.domain.auth.dto.SignInRequest;
import com.ts.rm.domain.auth.dto.SignUpRequest;
import com.ts.rm.domain.auth.dto.SignUpResponse;
import com.ts.rm.domain.auth.dto.TokenResponse;
import com.ts.rm.domain.auth.service.AuthService;
import com.ts.rm.domain.refreshtoken.service.RefreshTokenService;
import com.ts.rm.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API 컨트롤러
 * - Access Token: Response Body로 반환
 * - Refresh Token: HttpOnly Cookie로 반환 (XSS 방어)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입, 로그인, 토큰 갱신 API")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean secureCookie;

    /**
     * 회원가입 API
     *
     * @param request 회원가입 요청 DTO
     * @return 회원가입 응답 DTO
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 계정을 생성합니다.")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("Sign up request received for email: {}", request.getEmail());
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 로그인 API
     * - Access Token: Response Body
     * - Refresh Token: HttpOnly Cookie
     *
     * @param request 로그인 요청 DTO
     * @param response HTTP 응답 (Cookie 설정용)
     * @return AccessTokenResponse (Access Token만 포함)
     */
    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. Access Token은 응답 본문에, Refresh Token은 HttpOnly Cookie로 전달됩니다.")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> signIn(
            @Valid @RequestBody SignInRequest request,
            HttpServletResponse response) {
        log.info("Sign in request received for email: {}", request.getEmail());

        TokenResponse tokenResponse = authService.signIn(request);

        // Refresh Token을 HttpOnly Cookie로 설정
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        // Access Token만 Response Body로 반환
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.from(tokenResponse);
        return ResponseEntity.ok(ApiResponse.success(accessTokenResponse));
    }

    /**
     * Access Token 갱신 API
     * - Refresh Token을 Cookie에서 읽어서 검증
     * - 새로운 Access Token: Response Body
     * - 새로운 Refresh Token: HttpOnly Cookie
     *
     * @param refreshToken Cookie에서 읽은 Refresh Token
     * @param response HTTP 응답 (Cookie 설정용)
     * @return 새로운 AccessTokenResponse
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Cookie의 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = true) String refreshToken,
            HttpServletResponse response) {
        log.info("Token refresh request received");

        TokenResponse tokenResponse = refreshTokenService.refreshAccessToken(refreshToken);

        // 새로운 Refresh Token을 HttpOnly Cookie로 설정
        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        // Access Token만 Response Body로 반환
        AccessTokenResponse accessTokenResponse = AccessTokenResponse.from(tokenResponse);
        return ResponseEntity.ok(ApiResponse.success(accessTokenResponse));
    }

    /**
     * 로그아웃 API
     * - Cookie에서 Refresh Token을 읽어서 무효화
     * - Refresh Token Cookie 삭제
     *
     * @param refreshToken Cookie에서 읽은 Refresh Token
     * @param response HTTP 응답 (Cookie 삭제용)
     * @return 로그아웃 완료 메시지
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Cookie의 Refresh Token을 무효화하고 로그아웃합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        log.info("Logout request received");

        if (refreshToken != null) {
            // Refresh Token DB에서 삭제
            authService.logout(refreshToken);
        }

        // Refresh Token Cookie 삭제
        deleteRefreshTokenCookie(response);

        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "로그아웃되었습니다.")));
    }

    /**
     * Refresh Token을 HttpOnly Cookie로 설정하는 헬퍼 메서드
     *
     * @param response HTTP 응답
     * @param refreshToken Refresh Token 값
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);  // XSS 공격 방어
        cookie.setSecure(secureCookie);  // HTTPS only (프로필별 설정)
        cookie.setPath("/api/auth");  // Cookie 사용 경로 제한
        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));  // 만료 시간 (초)
        // SameSite=Lax는 Spring Boot 2.6+ 기본값

        response.addCookie(cookie);
        log.debug("Refresh token cookie set with maxAge: {} seconds", cookie.getMaxAge());
    }

    /**
     * Refresh Token Cookie를 삭제하는 헬퍼 메서드
     *
     * @param response HTTP 응답
     */
    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);  // 즉시 만료

        response.addCookie(cookie);
        log.debug("Refresh token cookie deleted");
    }
}
