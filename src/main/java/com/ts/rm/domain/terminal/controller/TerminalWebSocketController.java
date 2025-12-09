package com.ts.rm.domain.terminal.controller;

import com.ts.rm.domain.terminal.dto.TerminalDto.InputMessage;
import com.ts.rm.domain.terminal.service.TerminalSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * 터미널 WebSocket 메시지 핸들러
 * <p>
 * STOMP 메시지를 처리하여 터미널 입력을 프로세스로 전달합니다.
 * </p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class TerminalWebSocketController {

    private final TerminalSessionManager sessionManager;

    /**
     * 터미널 입력 처리
     * <p>
     * 클라이언트가 전송한 입력(명령어, 시그널)을 해당 세션의 프로세스로 전달합니다.
     * </p>
     *
     * @param sessionId 세션 ID
     * @param message 입력 메시지 (type: "input" | "signal", data: 입력 내용)
     * @param authentication Spring Security 인증 정보
     */
    @MessageMapping("/terminal/{sessionId}/input")
    public void handleInput(
            @DestinationVariable String sessionId,
            InputMessage message,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        log.info("터미널 입력 수신: sessionId={}, user={}, type={}, length={}",
                sessionId, userEmail, message.getType(), message.getData().length());

        try {
            if ("input".equals(message.getType())) {
                // 일반 입력 (명령어)
                sessionManager.sendInput(sessionId, userEmail, message.getData());
            } else if ("signal".equals(message.getType())) {
                // 시그널 (SIGINT, SIGTERM, SIGKILL)
                sessionManager.sendSignal(sessionId, userEmail, message.getData());
            } else {
                log.warn("알 수 없는 메시지 타입: {}", message.getType());
            }
        } catch (IllegalArgumentException e) {
            log.error("세션을 찾을 수 없음: sessionId={}, error={}", sessionId, e.getMessage());
        } catch (SecurityException e) {
            log.error("세션 접근 권한 없음: sessionId={}, user={}", sessionId, userEmail);
        } catch (Exception e) {
            log.error("입력 처리 실패: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }
}
