package com.ts.rm.global.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket 범용 메시지
 * <p>
 * 비즈니스 로직과 독립적인 범용 WebSocket 메시지 구조입니다.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    /**
     * 메시지 타입
     */
    private String type;

    /**
     * 메시지 페이로드 (데이터)
     */
    private Object payload;

    /**
     * 추가 메타데이터
     */
    private Map<String, Object> metadata;

    /**
     * 타임스탬프
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 정적 팩토리 메서드: 타입과 페이로드로 메시지 생성
     */
    public static WebSocketMessage of(String type, Object payload) {
        return WebSocketMessage.builder()
                .type(type)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 정적 팩토리 메서드: 타입, 페이로드, 메타데이터로 메시지 생성
     */
    public static WebSocketMessage of(String type, Object payload, Map<String, Object> metadata) {
        return WebSocketMessage.builder()
                .type(type)
                .payload(payload)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
