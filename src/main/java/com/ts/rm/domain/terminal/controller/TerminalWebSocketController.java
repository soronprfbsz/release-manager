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

    private final TerminalService terminalService;
    private final WebSocketSessionRegistry sessionRegistry;

    /**
     * 명령어 전송
     * <p>
     * 클라이언트가 /app/terminal/{id}/command로 메시지 전송
     * 첫 메시지 수신 시 WebSocket 세션을 등록합니다.
     * </p>
     *
     * @param id             터미널 세션 ID
     * @param commandMessage 명령어 메시지
     * @param headerAccessor STOMP 헤더 접근자
     */
    @MessageMapping("/terminal/{id}/command")
    public void sendCommand(@DestinationVariable String id,
                            TerminalDto.CommandMessage commandMessage,
                            StompHeaderAccessor headerAccessor) {

        log.info("[{}] 명령어 수신: {}", id, commandMessage.getCommand());

        // WebSocket 세션 등록 (첫 메시지 시에만 등록됨)
        registerWebSocketSession(id, headerAccessor);

        // 명령어 실행
        terminalService.executeCommand(id, commandMessage.getCommand());
    }

    /**
     * 터미널 크기 변경 (PTY resize)
     * <p>
     * 클라이언트 터미널(xterm.js) 창 크기가 변경될 때 호출됩니다.
     * SSH PTY 크기를 동기화하여 줄바꿈이 올바르게 처리되도록 합니다.
     * </p>
     *
     * @param id            터미널 세션 ID
     * @param resizeMessage 크기 변경 메시지 (cols, rows)
     * @param headerAccessor STOMP 헤더 접근자
     */
    @MessageMapping("/terminal/{id}/resize")
    public void resize(@DestinationVariable String id,
                       TerminalDto.ResizeMessage resizeMessage,
                       StompHeaderAccessor headerAccessor) {

        log.debug("[{}] 크기 변경 요청: {}x{}", id, resizeMessage.getCols(), resizeMessage.getRows());

        // WebSocket 세션 등록 (첫 메시지 시에만 등록됨)
        registerWebSocketSession(id, headerAccessor);

        // PTY 크기 변경
        terminalService.resize(id, resizeMessage.getCols(), resizeMessage.getRows());
    }

    /**
     * WebSocket 세션 등록
     * <p>
     * 비즈니스 세션(terminalId)과 WebSocket 세션을 매핑합니다.
     * </p>
     *
     * @param terminalId     터미널 세션 ID
     * @param headerAccessor STOMP 헤더 접근자
     */
    private void registerWebSocketSession(String terminalId, StompHeaderAccessor headerAccessor) {
        String webSocketSessionId = headerAccessor.getSessionId();

        // 이미 등록되어 있으면 스킵
        if (sessionRegistry.getMetadata(webSocketSessionId).isPresent()) {
            return;
        }

        // 메타데이터 생성 및 등록
        WebSocketSessionMetadata metadata = WebSocketSessionMetadata.builder()
                .webSocketSessionId(webSocketSessionId)
                .businessSessionId(terminalId)
                .businessType("TERMINAL")
                .build();

        sessionRegistry.registerSession(metadata);

        log.info("WebSocket 세션 등록: wsSessionId={}, terminalId={}", webSocketSessionId, terminalId);
    }
}
