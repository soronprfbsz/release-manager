package com.ts.rm.domain.terminal.controller;

import com.ts.rm.domain.terminal.dto.TerminalDto.SessionInfoResponse;
import com.ts.rm.domain.terminal.dto.TerminalDto.SessionStartResponse;
import com.ts.rm.domain.terminal.entity.TerminalSession;
import com.ts.rm.domain.terminal.entity.TerminalType;
import com.ts.rm.domain.terminal.service.TerminalSessionManager;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 순수 셸 터미널 REST API 컨트롤러
 * <p>
 * /bin/bash 직접 실행을 통한 대화형 터미널 기능을 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/terminal/shell")
@RequiredArgsConstructor
@Tag(name = "Shell Terminal", description = "순수 셸 터미널 API")
public class ShellTerminalController {

    private final TerminalSessionManager sessionManager;

    /**
     * 순수 셸 터미널 세션 시작
     *
     * @param userDetails 인증된 사용자 정보
     * @param workingDirectory 작업 디렉토리 (선택)
     * @return 세션 시작 응답 (sessionId, websocket URL 등)
     */
    @PostMapping
    @Operation(
            summary = "셸 터미널 세션 시작",
            description = "/bin/bash를 실행하여 대화형 셸 세션을 시작합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "세션 시작 성공",
                    content = @Content(schema = @Schema(implementation = SessionStartResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "세션 시작 실패 (최대 세션 초과 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "프로세스 실행 실패"
            )
    })
    public ResponseEntity<?> startShellSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String workingDirectory
    ) {
        try {
            String userEmail = userDetails.getUsername();
            String sessionId = sessionManager.startShellSession(userEmail, workingDirectory);

            SessionStartResponse response = SessionStartResponse.builder()
                    .sessionId(sessionId)
                    .type(TerminalType.SHELL)
                    .websocketUrl("/ws/terminal")
                    .subscribeUrl("/topic/terminal/" + sessionId)
                    .publishUrl("/app/terminal/" + sessionId + "/input")
                    .expiresAt(sessionManager.getUserSessions(userEmail).stream()
                            .filter(s -> s.getSessionId().equals(sessionId))
                            .findFirst()
                            .map(TerminalSession::getExpiresAt)
                            .orElse(null))
                    .build();

            log.info("셸 터미널 세션 시작 성공: sessionId={}, user={}", sessionId, userEmail);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalStateException e) {
            log.warn("셸 터미널 세션 시작 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("MAX_SESSIONS_EXCEEDED", e.getMessage()));

        } catch (IOException e) {
            log.error("셸 프로세스 실행 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("PROCESS_EXECUTION_FAILED", "셸 프로세스 실행에 실패했습니다"));
        }
    }

    /**
     * 셸 터미널 세션 종료
     *
     * @param userDetails 인증된 사용자 정보
     * @param sessionId 종료할 세션 ID
     * @return 성공/실패 응답
     */
    @DeleteMapping("/{sessionId}")
    @Operation(
            summary = "셸 터미널 세션 종료",
            description = "실행 중인 셸 터미널 세션을 종료하고 프로세스를 정리합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "세션 종료 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "세션을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "세션 접근 권한 없음"
            )
    })
    public ResponseEntity<?> terminateShellSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sessionId
    ) {
        try {
            String userEmail = userDetails.getUsername();
            sessionManager.terminateSession(sessionId, userEmail);

            log.info("셸 터미널 세션 종료 성공: sessionId={}, user={}", sessionId, userEmail);
            return ResponseEntity.ok(ApiResponse.success(null));

        } catch (IllegalArgumentException e) {
            log.warn("세션 종료 실패 - 세션 없음: sessionId={}", sessionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("SESSION_NOT_FOUND", e.getMessage()));

        } catch (SecurityException e) {
            log.warn("세션 종료 실패 - 권한 없음: sessionId={}, user={}",
                    sessionId, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("ACCESS_DENIED", e.getMessage()));
        }
    }

    /**
     * 사용자의 활성 셸 세션 목록 조회
     *
     * @param userDetails 인증된 사용자 정보
     * @return 활성 셸 세션 목록
     */
    @GetMapping
    @Operation(
            summary = "활성 셸 세션 목록 조회",
            description = "현재 사용자가 실행 중인 모든 셸 터미널 세션 목록을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SessionInfoResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<SessionInfoResponse>>> listShellSessions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();

        List<SessionInfoResponse> sessions = sessionManager.getUserSessions(userEmail).stream()
                .filter(session -> session.getType() == TerminalType.SHELL)
                .map(session -> SessionInfoResponse.builder()
                        .sessionId(session.getSessionId())
                        .type(session.getType())
                        .scriptPath(session.getScriptPath())
                        .createdAt(session.getCreatedAt())
                        .isAlive(session.isAlive())
                        .ownerEmail(session.getOwnerEmail())
                        .build())
                .toList();

        log.info("셸 세션 목록 조회: user={}, count={}", userEmail, sessions.size());
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }
}
