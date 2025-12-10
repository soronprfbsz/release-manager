package com.ts.rm.domain.shell.event;

import com.ts.rm.domain.shell.service.InteractiveShellOrchestrator;
import com.ts.rm.global.websocket.dto.WebSocketSessionMetadata;
import com.ts.rm.global.websocket.event.WebSocketSessionEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 셸 WebSocket 세션 이벤트 리스너
 * <p>
 * WebSocket 연결 해제 시 SSH 세션을 자동으로 정리합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShellWebSocketSessionListener implements WebSocketSessionEventListener {

    private final InteractiveShellOrchestrator shellOrchestrator;

    /**
     * WebSocket 연결 해제 시 SSH 세션 자동 종료
     *
     * @param metadata WebSocket 세션 메타데이터
     */
    @Override
    public void onSessionDisconnected(WebSocketSessionMetadata metadata) {
        String shellSessionId = metadata.getBusinessSessionId();

        log.warn("WebSocket 연결 해제 감지 - SSH 세션 자동 정리 시작: shellSessionId={}", shellSessionId);

        try {
            // SSH 세션 종료 (셸 닫기, SSH 연결 해제, DB 상태 업데이트)
            shellOrchestrator.disconnect(shellSessionId);

            log.info("SSH 세션 자동 정리 완료: shellSessionId={}", shellSessionId);

        } catch (Exception e) {
            log.error("SSH 세션 자동 정리 실패: shellSessionId={}", shellSessionId, e);
        }
    }

    /**
     * WebSocket 연결 시 (선택적 구현)
     *
     * @param metadata WebSocket 세션 메타데이터
     */
    @Override
    public void onSessionConnected(WebSocketSessionMetadata metadata) {
        String shellSessionId = metadata.getBusinessSessionId();
        log.info("WebSocket 연결 확인: shellSessionId={}", shellSessionId);
    }

    /**
     * 이 리스너가 처리할 비즈니스 타입
     *
     * @return "SHELL" - 셸 세션만 처리
     */
    @Override
    public String getBusinessType() {
        return "SHELL";
    }
}
