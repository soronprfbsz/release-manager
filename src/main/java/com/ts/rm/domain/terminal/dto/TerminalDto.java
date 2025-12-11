package com.ts.rm.domain.terminal.dto;

import com.ts.rm.domain.terminal.enums.TerminalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 터미널 DTO 모음
 */
public class TerminalDto {

    /**
     * 터미널 연결 요청
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 연결 요청")
    public static class ConnectRequest {
        @Schema(description = "호스트 주소", example = "192.168.1.100")
        @NotBlank(message = "호스트는 필수입니다")
        private String host;

        @Schema(description = "SSH 포트", example = "22")
        @NotNull(message = "포트는 필수입니다")
        @Min(value = 1, message = "포트는 1 이상이어야 합니다")
        @Max(value = 65535, message = "포트는 65535 이하여야 합니다")
        private Integer port;

        @Schema(description = "사용자명", example = "deploy")
        @NotBlank(message = "사용자명은 필수입니다")
        private String username;

        @Schema(description = "비밀번호")
        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    }

    /**
     * 터미널 연결 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 연결 응답")
    public static class ConnectResponse {
        @Schema(description = "터미널 ID", example = "terminal_2025-12-09T22_00_00_abc123")
        private String terminalId;

        @Schema(description = "터미널 상태", example = "CONNECTED")
        private TerminalStatus status;

        @Schema(description = "호스트 주소", example = "192.168.1.100")
        private String host;

        @Schema(description = "WebSocket 연결 URL", example = "/ws/terminal")
        private String websocketUrl;

        @Schema(description = "WebSocket 구독 URL", example = "/topic/terminal/terminal_2025-12-09T22_00_00_abc123")
        private String subscribeUrl;

        @Schema(description = "명령어 전송 URL", example = "/app/terminal/terminal_2025-12-09T22_00_00_abc123/command")
        private String commandUrl;

        @Schema(description = "생성 시각")
        private LocalDateTime createdAt;

        @Schema(description = "만료 시각")
        private LocalDateTime expiresAt;
    }

    /**
     * 명령어 메시지 (WebSocket - 클라이언트 → 서버)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "명령어 메시지")
    public static class CommandMessage {
        @Schema(description = "실행할 명령어", example = "ls -la")
        private String command;
    }

    /**
     * 출력 메시지 (WebSocket - 서버 → 클라이언트)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "출력 메시지")
    public static class OutputMessage {
        @Schema(description = "메시지 타입", example = "OUTPUT", allowableValues = {"STATUS", "OUTPUT", "ERROR"})
        private String type;

        @Schema(description = "터미널 상태 (type=STATUS인 경우)")
        private TerminalStatus status;

        @Schema(description = "출력 데이터 (type=OUTPUT/ERROR인 경우)")
        private String data;

        @Schema(description = "메시지 내용")
        private String message;

        @Schema(description = "타임스탬프")
        private LocalDateTime timestamp;
    }

    /**
     * 터미널 세션 정보 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 세션 정보")
    public static class ShellSessionInfo {
        @Schema(description = "터미널 ID")
        private String terminalId;

        @Schema(description = "터미널 상태")
        private TerminalStatus status;

        @Schema(description = "호스트 주소")
        private String host;

        @Schema(description = "사용자명")
        private String username;

        @Schema(description = "소유자 이메일")
        private String ownerEmail;

        @Schema(description = "생성 시각")
        private LocalDateTime createdAt;

        @Schema(description = "마지막 활동 시각")
        private LocalDateTime lastActivityAt;

        @Schema(description = "만료 시각")
        private LocalDateTime expiresAt;
    }
}
