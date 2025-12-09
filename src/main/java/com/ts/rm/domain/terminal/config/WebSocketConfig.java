package com.ts.rm.domain.terminal.config;

import com.ts.rm.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * <p>
 * STOMP over WebSocket 프로토콜을 사용하여 실시간 터미널 기능을 제공합니다.
 * </p>
 *
 * <h3>엔드포인트 구조</h3>
 * <ul>
 *   <li>연결: {@code ws://localhost:8081/ws/terminal}</li>
 *   <li>구독 (출력 수신): {@code /topic/terminal/{sessionId}}</li>
 *   <li>발행 (입력 전송): {@code /app/terminal/{sessionId}/input}</li>
 * </ul>
 *
 * <h3>보안</h3>
 * <ul>
 *   <li>JWT 토큰 기반 인증 (CONNECT 프레임의 Authorization 헤더)</li>
 *   <li>세션별 권한 검증 (세션 소유자만 접근 가능)</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * STOMP 엔드포인트 등록
     * <p>
     * 클라이언트는 {@code /ws/terminal} 엔드포인트로 WebSocket 연결을 맺습니다.
     * SockJS 폴백을 활성화하여 WebSocket을 지원하지 않는 브라우저에서도 동작하도록 합니다.
     * </p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/terminal")
                .setAllowedOriginPatterns("*") // 프로덕션에서는 명시적 도메인으로 제한 필요
                .withSockJS();
    }

    /**
     * 메시지 브로커 설정
     * <p>
     * Simple Broker를 사용하여 인메모리 메시지 브로킹을 제공합니다.
     * (RabbitMQ 등 외부 브로커는 불필요)
     * </p>
     *
     * <ul>
     *   <li>{@code /topic}: 구독 주제 (서버 → 클라이언트)</li>
     *   <li>{@code /app}: 애플리케이션 목적지 (클라이언트 → 서버)</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // 출력 스트림 구독용
        registry.setApplicationDestinationPrefixes("/app"); // 입력 전송용
    }

    /**
     * 인바운드 채널 인터셉터 등록
     * <p>
     * WebSocket CONNECT 프레임에서 JWT 토큰을 검증하고 인증 정보를 설정합니다.
     * </p>
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        try {
                            if (jwtTokenProvider.validateToken(token)) {
                                String email = jwtTokenProvider.getEmail(token);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );

                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                accessor.setUser(authentication);

                                log.info("WebSocket 인증 성공: {}", email);
                            } else {
                                log.warn("WebSocket 인증 실패: 유효하지 않은 토큰");
                                throw new IllegalArgumentException("Invalid JWT token");
                            }
                        } catch (Exception e) {
                            log.error("WebSocket 인증 오류: {}", e.getMessage());
                            throw new IllegalArgumentException("Authentication failed", e);
                        }
                    } else {
                        log.warn("WebSocket 연결 거부: Authorization 헤더 없음");
                        throw new IllegalArgumentException("Missing Authorization header");
                    }
                }

                return message;
            }
        });
    }
}
