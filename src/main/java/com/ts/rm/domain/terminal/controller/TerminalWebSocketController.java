package com.ts.rm.domain.terminal.controller;

import com.ts.rm.domain.terminal.dto.TerminalDto;
import com.ts.rm.domain.terminal.service.TerminalService;
import com.ts.rm.global.websocket.dto.WebSocketSessionMetadata;
import com.ts.rm.global.websocket.event.WebSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * 터미널 WebSocket Controller
 * <p>
 * 클라이언트에서 WebSocket을 통해 명령어를 전송할 수 있습니다.
 * </p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class TerminalWebSocketController {

    private final TerminalService shellOrchestrator;
    private final WebSocketSessionRegistry sessionRegistry;

    /**
     * 명령어 전송
     * <p>
     * 클라이언트가 /app/terminal/{shellSessionId}/command로 메시지 전송
     * 첫 메시지 수신 시 WebSocket 세션을 등록합니다.
     * </p>
     *
     * @param shellSessionId 터미널 세션 ID
     * @param commandMessage 명령어 메시지
     * @param headerAccessor STOMP 헤더 접근자
     */
    @MessageMapping("/terminal/{shellSessionId}/command")
    public void sendCommand(@DestinationVariable String shellSessionId,
                            TerminalDto.CommandMessage commandMessage,
                            StompHeaderAccessor headerAccessor) {

        log.info("[{}] 명령어 수신: {}", shellSessionId, commandMessage.getCommand());

        // WebSocket 세션 등록 (첫 메시지 시에만 등록됨)
        registerWebSocketSession(shellSessionId, headerAccessor);

        // 명령어 실행
        shellOrchestrator.executeCommand(shellSessionId, commandMessage.getCommand());
    }

    /**
     * WebSocket 세션 등록
     * <p>
     * 비즈니스 세션(shellSessionId)과 WebSocket 세션을 매핑합니다.
     * </p>
     *
     * @param shellSessionId 터미널 세션 ID
     * @param headerAccessor STOMP 헤더 접근자
     */
    private void registerWebSocketSession(String shellSessionId, StompHeaderAccessor headerAccessor) {
        String webSocketSessionId = headerAccessor.getSessionId();

        // 이미 등록되어 있으면 스킵
        if (sessionRegistry.getMetadata(webSocketSessionId).isPresent()) {
            return;
        }

        // 메타데이터 생성 및 등록
        WebSocketSessionMetadata metadata = WebSocketSessionMetadata.builder()
                .webSocketSessionId(webSocketSessionId)
                .businessSessionId(shellSessionId)
                .businessType("TERMINAL")
                .build();

        sessionRegistry.registerSession(metadata);

        log.info("WebSocket 세션 등록: wsSessionId={}, shellSessionId={}", webSocketSessionId, shellSessionId);
    }
}
