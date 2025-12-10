package com.ts.rm.global.websocket.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * WebSocket 세션 메타데이터
 * <p>
 * WebSocket 세션에 연결된 비즈니스 세션 정보를 저장합니다.
 * 범용적으로 사용 가능하도록 Map 기반으로 설계되었습니다.
 * </p>
 */
@Getter
@Builder
public class WebSocketSessionMetadata {

    /**
     * WebSocket 세션 ID (Spring STOMP 세션 ID)
     */
    private String webSocketSessionId;

    /**
     * 비즈니스 세션 식별자
     * <p>
     * 예: shellSessionId, chatRoomId, gameRoomId 등
     * </p>
     */
    private String businessSessionId;

    /**
     * 비즈니스 타입
     * <p>
     * 예: "SHELL", "CHAT", "GAME" 등
     * 동일한 WebSocket 엔드포인트를 여러 용도로 사용할 경우 구분자로 활용
     * </p>
     */
    private String businessType;

    /**
     * 추가 속성
     * <p>
     * 비즈니스별로 필요한 추가 정보를 저장합니다.
     * 예: userId, roomId, permissions 등
     * </p>
     */
    private Map<String, Object> attributes;

    /**
     * 생성 시각
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 마지막 활동 시각
     */
    @Builder.Default
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    /**
     * 마지막 활동 시각 갱신
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * 속성 추가
     *
     * @param key   속성 키
     * @param value 속성 값
     */
    public void setAttribute(String key, Object value) {
        if (attributes != null) {
            attributes.put(key, value);
        }
    }

    /**
     * 속성 조회
     *
     * @param key 속성 키
     * @return 속성 값
     */
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }
}
