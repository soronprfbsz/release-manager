package com.ts.rm.global.websocket.event;

import com.ts.rm.global.websocket.dto.WebSocketSessionMetadata;

/**
 * WebSocket 세션 이벤트 리스너 인터페이스
 * <p>
 * 비즈니스 로직에서 이 인터페이스를 구현하여
 * WebSocket 연결/해제 이벤트를 처리합니다.
 * </p>
 */
public interface WebSocketSessionEventListener {

    /**
     * WebSocket 연결 시 호출
     * <p>
     * 비즈니스 세션과 WebSocket 세션을 연결할 때 사용합니다.
     * </p>
     *
     * @param metadata WebSocket 세션 메타데이터
     */
    default void onSessionConnected(WebSocketSessionMetadata metadata) {
        // 기본 구현 없음 (선택적 구현)
    }

    /**
     * WebSocket 연결 해제 시 호출
     * <p>
     * 비즈니스 세션 정리, 리소스 해제 등을 수행합니다.
     * </p>
     *
     * @param metadata WebSocket 세션 메타데이터
     */
    void onSessionDisconnected(WebSocketSessionMetadata metadata);

    /**
     * WebSocket 구독 시 호출
     *
     * @param metadata WebSocket 세션 메타데이터
     */
    default void onSessionSubscribed(WebSocketSessionMetadata metadata) {
        // 기본 구현 없음 (선택적 구현)
    }

    /**
     * WebSocket 구독 취소 시 호출
     *
     * @param metadata WebSocket 세션 메타데이터
     */
    default void onSessionUnsubscribed(WebSocketSessionMetadata metadata) {
        // 기본 구현 없음 (선택적 구현)
    }

    /**
     * 이 리스너가 처리할 비즈니스 타입
     * <p>
     * 예: "SHELL", "CHAT", "GAME" 등
     * null을 반환하면 모든 타입의 이벤트를 처리합니다.
     * </p>
     *
     * @return 비즈니스 타입 (null 가능)
     */
    default String getBusinessType() {
        return null; // 모든 타입 처리
    }
}
