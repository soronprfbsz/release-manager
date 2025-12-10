package com.ts.rm.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * <p>
 * STOMP over WebSocket을 사용하여 실시간 양방향 통신을 지원합니다.
 * 대화형 셸 터미널의 실시간 입출력을 위해 사용됩니다.
 * </p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정
     * <p>
     * - /topic: 브로드캐스트 메시지 (구독자 모두에게 전송)
     * - /app: 애플리케이션 목적지 prefix
     * </p>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple In-Memory 브로커 사용 (프로덕션에서는 RabbitMQ, Redis 등 고려)
        config.enableSimpleBroker("/topic");

        // 클라이언트에서 서버로 메시지 전송 시 prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 등록
     * <p>
     * - /ws/shell: 대화형 셸 WebSocket 연결 엔드포인트
     * - SockJS fallback 지원
     * </p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/shell")
                .setAllowedOriginPatterns("*") // CORS 설정 (프로덕션에서는 특정 도메인만 허용)
                .withSockJS(); // SockJS fallback 지원 (WebSocket 미지원 브라우저 대응)
    }
}
