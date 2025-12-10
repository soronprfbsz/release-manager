package com.ts.rm.global.websocket.messaging;

import com.ts.rm.global.websocket.dto.WebSocketDestination;
import com.ts.rm.global.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * WebSocket 메시지 전송기
 * <p>
 * STOMP over WebSocket을 통해 메시지를 전송하는 범용 컴포넌트입니다.
 * 비즈니스 로직과 독립적으로 재사용 가능합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageSender {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 메시지 전송
     *
     * @param destination 목적지 정보
     * @param message     WebSocket 메시지
     */
    public void send(WebSocketDestination destination, WebSocketMessage message) {
        validateDestination(destination);
        validateMessage(message);

        String fullPath = destination.getFullPath();

        log.debug("WebSocket 메시지 전송: destination={}, type={}", fullPath, message.getType());

        messagingTemplate.convertAndSend(fullPath, message);
    }

    /**
     * 메시지 전송 (간편 메서드)
     *
     * @param destination 목적지 정보
     * @param type        메시지 타입
     * @param payload     페이로드
     */
    public void send(WebSocketDestination destination, String type, Object payload) {
        WebSocketMessage message = WebSocketMessage.of(type, payload);
        send(destination, message);
    }

    /**
     * 토픽으로 메시지 전송
     *
     * @param topicPath 토픽 경로
     * @param message   WebSocket 메시지
     */
    public void sendToTopic(String topicPath, WebSocketMessage message) {
        WebSocketDestination destination = WebSocketDestination.topic(topicPath);
        send(destination, message);
    }

    /**
     * 토픽으로 메시지 전송 (간편 메서드)
     *
     * @param topicPath 토픽 경로
     * @param type      메시지 타입
     * @param payload   페이로드
     */
    public void sendToTopic(String topicPath, String type, Object payload) {
        WebSocketDestination destination = WebSocketDestination.topic(topicPath);
        WebSocketMessage message = WebSocketMessage.of(type, payload);
        send(destination, message);
    }

    /**
     * 사용자에게 메시지 전송
     *
     * @param username    사용자명
     * @param destination 목적지 경로
     * @param message     WebSocket 메시지
     */
    public void sendToUser(String username, String destination, WebSocketMessage message) {
        validateMessage(message);

        log.debug("WebSocket 사용자 메시지 전송: user={}, destination={}, type={}",
                username, destination, message.getType());

        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    /**
     * 사용자에게 메시지 전송 (간편 메서드)
     *
     * @param username    사용자명
     * @param destination 목적지 경로
     * @param type        메시지 타입
     * @param payload     페이로드
     */
    public void sendToUser(String username, String destination, String type, Object payload) {
        WebSocketMessage message = WebSocketMessage.of(type, payload);
        sendToUser(username, destination, message);
    }

    /**
     * 목적지 유효성 검증
     *
     * @param destination 목적지 정보
     */
    private void validateDestination(WebSocketDestination destination) {
        if (destination == null) {
            throw new IllegalArgumentException("WebSocket 목적지는 필수입니다");
        }
        if (destination.getPath() == null || destination.getPath().isBlank()) {
            throw new IllegalArgumentException("WebSocket 목적지 경로는 필수입니다");
        }
    }

    /**
     * 메시지 유효성 검증
     *
     * @param message WebSocket 메시지
     */
    private void validateMessage(WebSocketMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("WebSocket 메시지는 필수입니다");
        }
        if (message.getType() == null || message.getType().isBlank()) {
            throw new IllegalArgumentException("WebSocket 메시지 타입은 필수입니다");
        }
    }
}
