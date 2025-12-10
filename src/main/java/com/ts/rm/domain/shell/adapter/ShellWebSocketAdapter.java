package com.ts.rm.domain.shell.adapter;

import com.ts.rm.domain.shell.dto.InteractiveShellDto;
import com.ts.rm.domain.shell.enums.ShellStatus;
import com.ts.rm.global.websocket.dto.WebSocketDestination;
import com.ts.rm.global.websocket.dto.WebSocketMessage;
import com.ts.rm.global.websocket.messaging.WebSocketMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 셸 WebSocket 어댑터
 * <p>
 * Global WebSocket 모듈을 Domain Shell 모듈에 연결하는 어댑터입니다.
 * Domain 메시지를 Global 메시지로 변환하여 전송합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShellWebSocketAdapter {

    private final WebSocketMessageSender messageSender;

    /**
     * 상태 메시지 전송
     *
     * @param shellSessionId 셸 세션 ID
     * @param status         셸 상태
     * @param message        메시지 내용
     */
    public void sendStatusMessage(String shellSessionId, ShellStatus status, String message) {
        // Domain DTO 생성
        InteractiveShellDto.OutputMessage outputMessage = InteractiveShellDto.OutputMessage.builder()
                .type("STATUS")
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // Global WebSocket 사용
        sendToSession(shellSessionId, outputMessage);
    }

    /**
     * 출력 메시지 전송
     *
     * @param shellSessionId 셸 세션 ID
     * @param output         출력 데이터
     */
    public void sendOutputMessage(String shellSessionId, String output) {
        // Domain DTO 생성
        InteractiveShellDto.OutputMessage outputMessage = InteractiveShellDto.OutputMessage.builder()
                .type("OUTPUT")
                .data(output)
                .timestamp(LocalDateTime.now())
                .build();

        // Global WebSocket 사용
        sendToSession(shellSessionId, outputMessage);
    }

    /**
     * 에러 메시지 전송
     *
     * @param shellSessionId 셸 세션 ID
     * @param error          에러 내용
     */
    public void sendErrorMessage(String shellSessionId, String error) {
        // Domain DTO 생성
        InteractiveShellDto.OutputMessage outputMessage = InteractiveShellDto.OutputMessage.builder()
                .type("ERROR")
                .data(error)
                .timestamp(LocalDateTime.now())
                .build();

        // Global WebSocket 사용
        sendToSession(shellSessionId, outputMessage);
    }

    /**
     * 세션별 메시지 전송
     *
     * @param shellSessionId 셸 세션 ID
     * @param outputMessage  Domain 출력 메시지
     */
    private void sendToSession(String shellSessionId, InteractiveShellDto.OutputMessage outputMessage) {
        // Domain 메시지 → Global 메시지 변환
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .type(outputMessage.getType())
                .payload(outputMessage)
                .metadata(buildMetadata(outputMessage))
                .timestamp(outputMessage.getTimestamp())
                .build();

        // 목적지 생성
        String topicPath = "shell/" + shellSessionId;
        WebSocketDestination destination = WebSocketDestination.topic(topicPath);

        // 메시지 전송
        messageSender.send(destination, wsMessage);

        log.debug("셸 메시지 전송: sessionId={}, type={}", shellSessionId, outputMessage.getType());
    }

    /**
     * 메타데이터 생성
     *
     * @param outputMessage Domain 출력 메시지
     * @return 메타데이터
     */
    private Map<String, Object> buildMetadata(InteractiveShellDto.OutputMessage outputMessage) {
        return Map.of(
                "messageType", outputMessage.getType(),
                "hasStatus", outputMessage.getStatus() != null,
                "hasData", outputMessage.getData() != null
        );
    }
}
