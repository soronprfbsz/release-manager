package com.ts.rm.domain.terminal.dto;

import com.ts.rm.domain.terminal.entity.TerminalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 터미널 도메인 DTO 클래스
 */
public class TerminalDto {

    /**
     * 터미널 세션 시작 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 세션 시작 요청")
    public static class SessionStartRequest {

        @NotBlank(message = "스크립트 경로는 필수입니다")
        @Schema(
                description = "실행할 스크립트 상대 경로 (release 디렉토리 기준)",
                example = "patches/infraeye2/202512091300_1.0.0_1.1.2/mariadb_patch.sh",
                required = true
        )
        private String scriptPath;

        @Schema(
                description = "작업 디렉토리 (선택, 미지정 시 스크립트 디렉토리)",
                example = "/app/release/patches/infraeye2/202512091300_1.0.0_1.1.2"
        )
        private String workingDirectory;
    }

    /**
     * 터미널 세션 시작 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 세션 시작 응답")
    public static class SessionStartResponse {

        @Schema(description = "세션 ID", example = "term_20251209_130012_abc123")
        private String sessionId;

        @Schema(description = "세션 타입", example = "SHELL")
        private TerminalType type;

        @Schema(description = "WebSocket 연결 URL", example = "/ws/terminal")
        private String websocketUrl;

        @Schema(description = "구독 URL (출력 수신)", example = "/topic/terminal/term_20251209_130012_abc123")
        private String subscribeUrl;

        @Schema(description = "발행 URL (입력 전송)", example = "/app/terminal/term_20251209_130012_abc123/input")
        private String publishUrl;

        @Schema(description = "세션 만료 시각", example = "2025-12-09T14:00:12")
        private LocalDateTime expiresAt;
    }

    /**
     * 터미널 세션 정보 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 세션 정보")
    public static class SessionInfoResponse {

        @Schema(description = "세션 ID", example = "term_20251209_130012_abc123")
        private String sessionId;

        @Schema(description = "세션 타입", example = "SHELL")
        private TerminalType type;

        @Schema(description = "스크립트 경로", example = "mariadb_patch.sh")
        private String scriptPath;

        @Schema(description = "생성 시각", example = "2025-12-09T13:00:12")
        private LocalDateTime createdAt;

        @Schema(description = "프로세스 실행 여부", example = "true")
        private boolean isAlive;

        @Schema(description = "세션 소유자 이메일", example = "jhlee@tscientific")
        private String ownerEmail;
    }

    /**
     * 터미널 입력 메시지 (클라이언트 → 서버)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 입력 메시지")
    public static class InputMessage {

        @Schema(
                description = "메시지 타입",
                allowableValues = {"input", "signal"},
                example = "input"
        )
        private String type; // "input" | "signal"

        @Schema(
                description = "입력 데이터 (type=input: 사용자 입력, type=signal: SIGINT/SIGTERM/SIGKILL)",
                example = "jhlee@tscientific\n"
        )
        private String data;

        @Schema(description = "타임스탬프", example = "2025-12-09T13:00:16")
        private LocalDateTime timestamp;
    }

    /**
     * 터미널 출력 메시지 (서버 → 클라이언트)
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "터미널 출력 메시지")
    public static class OutputMessage {

        @Schema(
                description = "메시지 타입",
                allowableValues = {"output", "error", "exit"},
                example = "output"
        )
        private String type; // "output" | "error" | "exit"

        @Schema(
                description = "출력 데이터 (ANSI escape 코드 포함 가능)",
                example = "실행 중...\n"
        )
        private String data;

        @Schema(description = "타임스탬프", example = "2025-12-09T13:00:15")
        private LocalDateTime timestamp;

        @Schema(description = "종료 코드 (type=exit 시에만)", example = "0")
        private Integer exitCode;
    }
}
