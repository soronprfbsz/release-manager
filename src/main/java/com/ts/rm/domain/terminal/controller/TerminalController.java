package com.ts.rm.domain.terminal.controller;

import com.ts.rm.domain.terminal.dto.TerminalDto.SessionInfoResponse;
import com.ts.rm.domain.terminal.dto.TerminalDto.SessionStartRequest;
import com.ts.rm.domain.terminal.dto.TerminalDto.SessionStartResponse;
import com.ts.rm.domain.terminal.entity.TerminalSession;
import com.ts.rm.domain.terminal.entity.TerminalType;
import com.ts.rm.domain.terminal.service.TerminalSessionManager;
import com.ts.rm.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 스크립트 실행 터미널 REST API 컨트롤러
 * <p>
 * .sh 스크립트 파일 실행을 통한 대화형 터미널 기능을 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/terminal/scripts")
@RequiredArgsConstructor
public class TerminalController implements TerminalControllerDocs {

    private final TerminalSessionManager sessionManager;

    /**
     * 스크립트 실행 세션 시작
     */
    @Override
    @PostMapping
    public ApiResponse<SessionStartResponse> startSession(
            @Valid @RequestBody SessionStartRequest request
    ) {
        // Authentication은 Spring Security가 자동 주입
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        try {
            String userEmail = authentication.getName();
            String sessionId = sessionManager.startScriptSession(
                    userEmail,
                    request.getScriptPath(),
                    request.getWorkingDirectory()
            );

            SessionStartResponse response = SessionStartResponse.builder()
                    .sessionId(sessionId)
                    .type(TerminalType.SCRIPT)
                    .websocketUrl("/ws/terminal")
                    .subscribeUrl("/topic/terminal/" + sessionId)
                    .publishUrl("/app/terminal/" + sessionId + "/input")
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();

            log.info("스크립트 세션 시작 API 호출: user={}, sessionId={}", userEmail, sessionId);
            return ApiResponse.success(response);
        } catch (IOException e) {
            log.error("스크립트 세션 시작 실패: {}", e.getMessage());
            throw new RuntimeException("스크립트 실행 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 스크립트 실행 세션 종료
     */
    @Override
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> terminateSession(
            @PathVariable String sessionId
    ) {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String userEmail = authentication.getName();
        sessionManager.terminateSession(sessionId, userEmail);

        log.info("터미널 세션 종료 API 호출: user={}, sessionId={}", userEmail, sessionId);
        return ApiResponse.success(null);
    }

    /**
     * 활성 스크립트 세션 목록 조회
     */
    @Override
    @GetMapping
    public ApiResponse<List<SessionInfoResponse>> listSessions() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String userEmail = authentication.getName();
        List<TerminalSession> sessions = sessionManager.getUserSessions(userEmail);

        List<SessionInfoResponse> responses = sessions.stream()
                .filter(session -> session.getType() == TerminalType.SCRIPT)
                .map(session -> SessionInfoResponse.builder()
                        .sessionId(session.getSessionId())
                        .type(session.getType())
                        .scriptPath(session.getScriptPath())
                        .createdAt(session.getCreatedAt())
                        .isAlive(session.isAlive())
                        .ownerEmail(session.getOwnerEmail())
                        .build())
                .toList();

        log.debug("활성 세션 목록 조회: user={}, count={}", userEmail, responses.size());
        return ApiResponse.success(responses);
    }
}
