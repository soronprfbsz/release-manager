package com.ts.rm.global.websocket.event;

import com.ts.rm.global.websocket.dto.WebSocketSessionMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.List;

/**
 * WebSocket 이벤트 핸들러
 * <p>
 * Spring WebSocket 이벤트를 수신하여 등록된 리스너들에게 전파합니다.
 * 비즈니스 로직과 독립적으로 재사용 가능합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventHandler {

    private final WebSocketSessionRegistry sessionRegistry;
    private final List<WebSocketSessionEventListener> eventListeners;

    /**
     * WebSocket 연결 이벤트
     *
     * @param event 연결 이벤트
     */
    @EventListener
    public void handleWebSocketConnectEvent(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String webSocketSessionId = accessor.getSessionId();

        log.debug("WebSocket 연결: sessionId={}", webSocketSessionId);

        // 메타데이터가 등록되어 있으면 리스너에게 알림
        sessionRegistry.getMetadata(webSocketSessionId).ifPresent(metadata -> {
            notifyListeners(listener -> listener.onSessionConnected(metadata), metadata);
        });
    }

    /**
     * WebSocket 연결 해제 이벤트
     * <p>
     * 브라우저 강제 종료, 네트워크 끊김 등 모든 연결 해제를 감지합니다.
     * </p>
     *
     * @param event 연결 해제 이벤트
     */
    @EventListener
    public void handleWebSocketDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String webSocketSessionId = accessor.getSessionId();

        log.info("WebSocket 연결 해제: sessionId={}, closeStatus={}",
                webSocketSessionId, event.getCloseStatus());

        // 세션 메타데이터 조회 및 제거
        sessionRegistry.unregisterSession(webSocketSessionId).ifPresent(metadata -> {
            log.warn("비즈니스 세션 자동 정리: businessSessionId={}, type={}",
                    metadata.getBusinessSessionId(), metadata.getBusinessType());

            // 모든 리스너에게 연결 해제 알림
            notifyListeners(listener -> listener.onSessionDisconnected(metadata), metadata);
        });
    }

    /**
     * WebSocket 구독 이벤트
     *
     * @param event 구독 이벤트
     */
    @EventListener
    public void handleWebSocketSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String webSocketSessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        log.debug("WebSocket 구독: sessionId={}, destination={}", webSocketSessionId, destination);

        sessionRegistry.getMetadata(webSocketSessionId).ifPresent(metadata -> {
            sessionRegistry.updateLastActivity(webSocketSessionId);
            notifyListeners(listener -> listener.onSessionSubscribed(metadata), metadata);
        });
    }

    /**
     * WebSocket 구독 취소 이벤트
     *
     * @param event 구독 취소 이벤트
     */
    @EventListener
    public void handleWebSocketUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String webSocketSessionId = accessor.getSessionId();

        log.debug("WebSocket 구독 취소: sessionId={}", webSocketSessionId);

        sessionRegistry.getMetadata(webSocketSessionId).ifPresent(metadata -> {
            notifyListeners(listener -> listener.onSessionUnsubscribed(metadata), metadata);
        });
    }

    /**
     * 리스너들에게 이벤트 전파
     *
     * @param notifier 이벤트 전달 함수
     * @param metadata 세션 메타데이터
     */
    private void notifyListeners(ListenerNotifier notifier, WebSocketSessionMetadata metadata) {
        for (WebSocketSessionEventListener listener : eventListeners) {
            // 리스너의 비즈니스 타입 필터링
            String listenerType = listener.getBusinessType();
            if (listenerType == null || listenerType.equals(metadata.getBusinessType())) {
                try {
                    notifier.notify(listener);
                } catch (Exception e) {
                    log.error("WebSocket 이벤트 리스너 실행 중 오류: listener={}, businessSessionId={}",
                            listener.getClass().getSimpleName(),
                            metadata.getBusinessSessionId(), e);
                }
            }
        }
    }

    /**
     * 리스너 알림 함수형 인터페이스
     */
    @FunctionalInterface
    private interface ListenerNotifier {
        void notify(WebSocketSessionEventListener listener);
    }
}
