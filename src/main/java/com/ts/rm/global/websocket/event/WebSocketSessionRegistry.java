package com.ts.rm.global.websocket.event;

import com.ts.rm.global.websocket.dto.WebSocketSessionMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 세션 레지스트리
 * <p>
 * WebSocket 세션 ID와 비즈니스 세션 메타데이터를 매핑 관리합니다.
 * 비즈니스 로직과 독립적으로 재사용 가능합니다.
 * </p>
 */
@Slf4j
@Component
public class WebSocketSessionRegistry {

    /**
     * WebSocket 세션 ID → 메타데이터 매핑
     */
    private final Map<String, WebSocketSessionMetadata> sessionMetadataMap = new ConcurrentHashMap<>();

    /**
     * 비즈니스 세션 ID → WebSocket 세션 ID 역방향 매핑
     */
    private final Map<String, String> businessSessionToWebSocketMap = new ConcurrentHashMap<>();

    /**
     * WebSocket 세션 등록
     *
     * @param metadata 세션 메타데이터
     */
    public void registerSession(WebSocketSessionMetadata metadata) {
        sessionMetadataMap.put(metadata.getWebSocketSessionId(), metadata);
        businessSessionToWebSocketMap.put(metadata.getBusinessSessionId(), metadata.getWebSocketSessionId());

        log.debug("WebSocket 세션 등록: wsSessionId={}, businessSessionId={}, type={}",
                metadata.getWebSocketSessionId(),
                metadata.getBusinessSessionId(),
                metadata.getBusinessType());
    }

    /**
     * WebSocket 세션 제거
     *
     * @param webSocketSessionId WebSocket 세션 ID
     * @return 제거된 메타데이터 (없으면 empty)
     */
    public Optional<WebSocketSessionMetadata> unregisterSession(String webSocketSessionId) {
        WebSocketSessionMetadata metadata = sessionMetadataMap.remove(webSocketSessionId);

        if (metadata != null) {
            businessSessionToWebSocketMap.remove(metadata.getBusinessSessionId());

            log.debug("WebSocket 세션 제거: wsSessionId={}, businessSessionId={}, type={}",
                    webSocketSessionId,
                    metadata.getBusinessSessionId(),
                    metadata.getBusinessType());

            return Optional.of(metadata);
        }

        return Optional.empty();
    }

    /**
     * WebSocket 세션 ID로 메타데이터 조회
     *
     * @param webSocketSessionId WebSocket 세션 ID
     * @return 메타데이터 (없으면 empty)
     */
    public Optional<WebSocketSessionMetadata> getMetadata(String webSocketSessionId) {
        return Optional.ofNullable(sessionMetadataMap.get(webSocketSessionId));
    }

    /**
     * 비즈니스 세션 ID로 메타데이터 조회
     *
     * @param businessSessionId 비즈니스 세션 ID
     * @return 메타데이터 (없으면 empty)
     */
    public Optional<WebSocketSessionMetadata> getMetadataByBusinessSessionId(String businessSessionId) {
        String webSocketSessionId = businessSessionToWebSocketMap.get(businessSessionId);
        return webSocketSessionId != null ? getMetadata(webSocketSessionId) : Optional.empty();
    }

    /**
     * 비즈니스 세션 ID로 WebSocket 세션 ID 조회
     *
     * @param businessSessionId 비즈니스 세션 ID
     * @return WebSocket 세션 ID (없으면 empty)
     */
    public Optional<String> getWebSocketSessionId(String businessSessionId) {
        return Optional.ofNullable(businessSessionToWebSocketMap.get(businessSessionId));
    }

    /**
     * 마지막 활동 시각 갱신
     *
     * @param webSocketSessionId WebSocket 세션 ID
     */
    public void updateLastActivity(String webSocketSessionId) {
        getMetadata(webSocketSessionId).ifPresent(WebSocketSessionMetadata::updateLastActivity);
    }

    /**
     * 전체 세션 수
     *
     * @return 활성 WebSocket 세션 수
     */
    public int getSessionCount() {
        return sessionMetadataMap.size();
    }

    /**
     * 비즈니스 타입별 세션 수
     *
     * @param businessType 비즈니스 타입
     * @return 세션 수
     */
    public long getSessionCountByType(String businessType) {
        return sessionMetadataMap.values().stream()
                .filter(metadata -> businessType.equals(metadata.getBusinessType()))
                .count();
    }

    /**
     * 모든 세션 정리 (테스트용)
     */
    public void clear() {
        sessionMetadataMap.clear();
        businessSessionToWebSocketMap.clear();
        log.info("모든 WebSocket 세션 레지스트리 정리 완료");
    }
}
