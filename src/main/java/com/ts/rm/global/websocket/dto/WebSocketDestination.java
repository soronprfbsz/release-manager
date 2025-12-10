package com.ts.rm.global.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 목적지 정보
 * <p>
 * STOMP 브로커의 토픽 또는 큐 목적지를 표현합니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketDestination {
    /**
     * 목적지 타입
     */
    private DestinationType type;

    /**
     * 목적지 경로
     */
    private String path;

    /**
     * 목적지 타입
     */
    public enum DestinationType {
        /**
         * 브로드캐스트 토픽 (/topic)
         */
        TOPIC,

        /**
         * 개인 큐 (/queue)
         */
        QUEUE,

        /**
         * 사용자 특정 목적지 (/user)
         */
        USER
    }

    /**
     * 전체 목적지 경로 반환
     */
    public String getFullPath() {
        return switch (type) {
            case TOPIC -> "/topic/" + path;
            case QUEUE -> "/queue/" + path;
            case USER -> "/user/" + path;
        };
    }

    /**
     * 정적 팩토리 메서드: 토픽 목적지 생성
     */
    public static WebSocketDestination topic(String path) {
        return WebSocketDestination.builder()
                .type(DestinationType.TOPIC)
                .path(path)
                .build();
    }

    /**
     * 정적 팩토리 메서드: 큐 목적지 생성
     */
    public static WebSocketDestination queue(String path) {
        return WebSocketDestination.builder()
                .type(DestinationType.QUEUE)
                .path(path)
                .build();
    }

    /**
     * 정적 팩토리 메서드: 사용자 목적지 생성
     */
    public static WebSocketDestination user(String path) {
        return WebSocketDestination.builder()
                .type(DestinationType.USER)
                .path(path)
                .build();
    }
}
