package com.ts.rm.domain.terminal.controller;

import com.ts.rm.domain.terminal.dto.TerminalDto.SessionInfoResponse;
import com.ts.rm.domain.terminal.dto.TerminalDto.SessionStartRequest;
import com.ts.rm.domain.terminal.dto.TerminalDto.SessionStartResponse;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * TerminalController Swagger 문서화 인터페이스 (스크립트 실행)
 */
@Tag(name = "Script Terminal", description = "스크립트 실행 터미널 세션 관리 API")
@SwaggerResponse
public interface TerminalControllerDocs {

    @Operation(
            summary = "스크립트 실행 세션 시작",
            description = ".sh 스크립트 파일 실행을 위한 터미널 세션을 생성합니다.\\n\\n"
                    + "**동작 흐름**:\\n"
                    + "1. 세션 ID 생성\\n"
                    + "2. 지정된 스크립트로 Bash 프로세스 실행\\n"
                    + "3. WebSocket 구독 정보 반환\\n\\n"
                    + "**제한사항**:\\n"
                    + "- 사용자당 최대 3개 세션\\n"
                    + "- 세션 타임아웃: 1시간\\n"
                    + "- 실행 가능한 스크립트: release 디렉토리 내 .sh 파일만",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SessionStartApiResponse.class),
                            examples = @ExampleObject(
                                    name = "터미널 세션 시작 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "sessionId": "term_20251209_130012_abc123",
                                                "type": "SCRIPT",
                                                "websocketUrl": "/ws/terminal",
                                                "subscribeUrl": "/topic/terminal/term_20251209_130012_abc123",
                                                "publishUrl": "/app/terminal/term_20251209_130012_abc123/input",
                                                "expiresAt": "2025-12-09T14:00:12"
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    ApiResponse<SessionStartResponse> startSession(
            @Valid @RequestBody SessionStartRequest request
    );

    @Operation(
            summary = "스크립트 실행 세션 종료",
            description = "실행 중인 스크립트 터미널 세션을 종료합니다.\\n\\n"
                    + "**동작**:\\n"
                    + "- 실행 중인 스크립트 프로세스 종료 (SIGTERM)\\n"
                    + "- 세션 정보 제거\\n"
                    + "- WebSocket 연결 종료",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"status\": \"success\", \"data\": null}"
                            )
                    )
            )
    )
    ApiResponse<Void> terminateSession(
            @Parameter(description = "세션 ID", required = true, example = "term_20251209_130012_abc123")
            @PathVariable String sessionId
    );

    @Operation(
            summary = "활성 스크립트 세션 목록 조회",
            description = "현재 로그인한 사용자의 활성 스크립트 실행 세션 목록을 조회합니다.",
            responses = @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SessionListApiResponse.class),
                            examples = @ExampleObject(
                                    name = "활성 세션 목록 조회 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": [
                                                {
                                                  "sessionId": "term_20251209_130012_abc123",
                                                  "type": "SCRIPT",
                                                  "scriptPath": "patches/infraeye2/202512091300_1.0.0_1.1.2/mariadb_patch.sh",
                                                  "createdAt": "2025-12-09T13:00:12",
                                                  "isAlive": true,
                                                  "ownerEmail": "jhlee@tscientific"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    ApiResponse<List<SessionInfoResponse>> listSessions();

    /**
     * Swagger 스키마용 wrapper 클래스 - 세션 시작 응답
     */
    @Schema(description = "터미널 세션 시작 API 응답")
    class SessionStartApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "세션 시작 정보")
        public SessionStartResponse data;
    }

    /**
     * Swagger 스키마용 wrapper 클래스 - 세션 목록 응답
     */
    @Schema(description = "터미널 세션 목록 API 응답")
    class SessionListApiResponse {
        @Schema(description = "응답 상태", example = "success")
        public String status;

        @Schema(description = "세션 목록")
        public List<SessionInfoResponse> data;
    }
}
