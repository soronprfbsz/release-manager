package com.ts.rm.global.config;

import com.ts.rm.global.filter.JwtAuthenticationFilter;
import com.ts.rm.global.security.jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용으로 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 활성화
                .cors(cors -> cors.configure(http))

                // Session 사용 안 함
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 실패 시 401 Unauthorized 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // 인증/인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/codes/POSITION").permitAll()  // 회원가입 폼에서 직급 코드 조회
                        .requestMatchers("/swagger-ui/**", "/swagger", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()  // Spring Boot 기본 에러 처리 엔드포인트
                        // WebSocket 엔드포인트 - SockJS handshake 허용 (STOMP CONNECT에서 JWT 검증)
                        .requestMatchers("/ws/**").permitAll()  // 모든 WebSocket 엔드포인트 (/ws/remote-execution, /ws/terminal 등)
                        // 파일 API - 브라우저 네이티브 다운로드 방식 사용으로 인증 제외
                        .requestMatchers("/api/files/**").permitAll()  // 공통 파일 API (다운로드, 컨텐츠 조회)
                        .requestMatchers("/api/releases/versions/*/download").permitAll()  // 릴리즈 버전 전체 다운로드 (ZIP)
                        .requestMatchers("/api/patches/*/download").permitAll()  // 패치 다운로드 (ZIP)
                        .requestMatchers("/api/projects/*/onboardings/files/zip-download").permitAll() // 온보딩 전체 다운로드 (ZIP)
                        .requestMatchers("/api/projects/*/installs/files/zip-download").permitAll() // 인스톨 전체 다운로드 (ZIP)
                        .requestMatchers("/api/resources/*/files/zip-download").permitAll() // 리소스 전체 다운로드 (ZIP)
                        .requestMatchers("/api/jobs/backup-files/*/download").permitAll() // 백업 파일 다운로드
                        .requestMatchers("/api/jobs/backup-files/*/logs/download").permitAll() // 백업 로그 다운로드
                        .requestMatchers("/api/publishing/*/serve/**").permitAll() // 퍼블리싱 파일 서빙 (브라우저 열기)
                        .requestMatchers("/api/publishing/*/download").permitAll() // 퍼블리싱 전체 다운로드 (ZIP)
                        .requestMatchers(HttpMethod.GET, "/api/board/images/**").permitAll() // 게시판 이미지 조회 (업로드/삭제는 인증 필요)
                        // 스케줄러 내부 호출용 maintenance API (X-Schedule-Job 헤더로 검증)
                        .requestMatchers("/api/maintenance/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
