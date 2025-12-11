package com.ts.rm.domain.terminal.service;

import com.jcraft.jsch.Session;
import com.ts.rm.domain.terminal.adapter.SshAdapter;
import com.ts.rm.domain.terminal.adapter.TerminalWebSocketAdapter;
import com.ts.rm.domain.terminal.dto.TerminalDto;
import com.ts.rm.domain.terminal.dto.SshConnectionDto;
import com.ts.rm.domain.terminal.enums.TerminalStatus;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.ssh.dto.SshExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 터미널 서비스
 * <p>
 * SSH 연결 → 터미널 열기 → 명령어 실행 → 출력 스트리밍의 전체 흐름을 조율합니다.
 * Infrastructure 모듈(SSH, WebSocket)을 Adapter를 통해 사용합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TerminalService {

    private final SshAdapter sshAdapter;
    private final TerminalWebSocketAdapter webSocketAdapter;
    private final TerminalSessionManager sessionManager;

    /**
     * 터미널 연결 시작
     *
     * @param request    연결 요청 정보
     * @param ownerEmail 세션 소유자 이메일
     * @return 세션 정보
     */
    public TerminalDto.ConnectResponse connect(
            TerminalDto.ConnectRequest request,
            String ownerEmail) {

        // 세션 생성
        TerminalDto.ConnectResponse response = sessionManager.createSession(request, ownerEmail);
        String shellSessionId = response.getTerminalId();

        // 비동기 연결
        connectAsync(shellSessionId, request);

        return response;
    }

    /**
     * 비동기 SSH 연결 및 터미널 열기
     *
     * @param shellSessionId 터미널 세션 ID
     * @param request        연결 요청 정보
     */
    private void connectAsync(String shellSessionId, TerminalDto.ConnectRequest request) {
        Thread connectionThread = new Thread(() -> {
            Session sshSession = null;
            SshExecutionContext executionContext = null;

            try {
                // 1. SSH 연결
                log.info("[{}] SSH 연결 시작", shellSessionId);
                webSocketAdapter.sendStatusMessage(shellSessionId, TerminalStatus.CONNECTING, "SSH 연결 중...");

                SshConnectionDto connectionInfo = SshConnectionDto.builder()
                        .host(request.getHost())
                        .port(request.getPort())
                        .username(request.getUsername())
                        .password(request.getPassword())
                        .build();

                sshSession = sshAdapter.connect(connectionInfo);

                // 2. 대화형 터미널 열기
                log.info("[{}] 대화형 터미널 열기", shellSessionId);
                executionContext = sshAdapter.openShell(sshSession, output -> {
                    // 실시간 출력 스트리밍 (WebSocket Adapter 사용)
                    webSocketAdapter.sendOutputMessage(shellSessionId, output);
                });

                // 3. 세션 연결 완료
                sessionManager.attachShell(shellSessionId, sshSession, executionContext);
                webSocketAdapter.sendStatusMessage(shellSessionId, TerminalStatus.CONNECTED, "SSH 연결 성공");

                log.info("[{}] 터미널 연결 성공", shellSessionId);

            } catch (BusinessException e) {
                log.error("[{}] 터미널 연결 실패: {}", shellSessionId, e.getErrorCode(), e);
                handleConnectionError(shellSessionId, e.getMessage());

                // SSH 세션이 열렸을 수 있으니 닫아줌
                if (sshSession != null) {
                    sshAdapter.disconnect(sshSession);
                }

            } catch (Exception e) {
                log.error("[{}] 예상치 못한 오류", shellSessionId, e);
                handleConnectionError(shellSessionId, "예상치 못한 오류: " + e.getMessage());

                if (sshSession != null) {
                    sshAdapter.disconnect(sshSession);
                }
            }
        });

        connectionThread.setName("ShellConnection-" + shellSessionId);
        connectionThread.start();
    }

    /**
     * 명령어 실행
     *
     * @param shellSessionId 터미널 세션 ID
     * @param command        실행할 명령어 (또는 키 입력)
     */
    public void executeCommand(String shellSessionId, String command) {
        try {
            // 터미널 컨텍스트 가져오기
            SshExecutionContext executionContext = sessionManager.getExecutionContext(shellSessionId)
                    .orElseThrow(() -> new IllegalArgumentException("터미널 세션을 찾을 수 없습니다: " + shellSessionId));

            // 터미널 연결 확인
            if (!sshAdapter.isShellConnected(executionContext)) {
                throw new BusinessException(ErrorCode.TERMINAL_NOT_CONNECTED);
            }

            // 명령어 실행 시에만 활동 시간 업데이트 (Enter 키 입력 시)
            // DB 부하 감소: 한글자 입력마다가 아닌 실제 명령어 실행 시에만 업데이트
            if (command.contains("\r") || command.contains("\n")) {
                sessionManager.updateLastActivity(shellSessionId);
            }

            // 명령어 전송 (SSH Adapter 사용)
            log.debug("[{}] 입력 전송: {}", shellSessionId,
                    command.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"));
            sshAdapter.writeInput(executionContext, command);

        } catch (Exception e) {
            log.error("[{}] 명령어 실행 실패: {}", shellSessionId, command, e);
            webSocketAdapter.sendErrorMessage(shellSessionId, "명령어 실행 실패: " + e.getMessage());
        }
    }

    /**
     * 터미널 연결 종료
     *
     * @param shellSessionId 터미널 세션 ID
     */
    public void disconnect(String shellSessionId) {
        log.info("[{}] 터미널 연결 종료 요청", shellSessionId);

        try {
            // 터미널 컨텍스트 가져오기
            sessionManager.getExecutionContext(shellSessionId).ifPresent(executionContext -> {
                sshAdapter.closeShell(executionContext);
            });

            // 세션 종료
            sessionManager.closeSession(shellSessionId);

            webSocketAdapter.sendStatusMessage(shellSessionId, TerminalStatus.DISCONNECTED, "터미널 연결이 종료되었습니다");

        } catch (Exception e) {
            log.error("[{}] 터미널 종료 중 오류", shellSessionId, e);
        }
    }

    /**
     * 세션 정보 조회
     *
     * @param shellSessionId 터미널 세션 ID
     * @return 세션 정보
     */
    public TerminalDto.ShellSessionInfo getSessionInfo(String shellSessionId) {
        return sessionManager.getSession(shellSessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + shellSessionId));
    }

    /**
     * 연결 오류 처리
     *
     * @param shellSessionId 터미널 세션 ID
     * @param errorMessage   오류 메시지
     */
    private void handleConnectionError(String shellSessionId, String errorMessage) {
        sessionManager.updateErrorMessage(shellSessionId, errorMessage);
        webSocketAdapter.sendErrorMessage(shellSessionId, errorMessage);
        webSocketAdapter.sendStatusMessage(shellSessionId, TerminalStatus.ERROR, errorMessage);
    }
}
