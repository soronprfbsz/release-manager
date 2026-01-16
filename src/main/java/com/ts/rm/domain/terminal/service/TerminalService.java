package com.ts.rm.domain.terminal.service;

import com.jcraft.jsch.Session;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.domain.patch.repository.PatchRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

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
    private final PatchRepository patchRepository;

    @Value("${app.release.base-path:data/release-manager}")
    private String releaseBasePath;

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
     * 터미널 크기 변경 (PTY resize)
     * <p>
     * 클라이언트 터미널(xterm.js) 창 크기가 변경될 때 SSH PTY 크기를 동기화합니다.
     * SSH 연결이 완료되기 전에 호출되면 무시됩니다 (연결 후 재요청됨).
     * </p>
     *
     * @param shellSessionId 터미널 세션 ID
     * @param cols           컬럼 수
     * @param rows           행 수
     */
    public void resize(String shellSessionId, int cols, int rows) {
        try {
            // 터미널 컨텍스트 가져오기 (연결 완료 전이면 없을 수 있음)
            var executionContextOpt = sessionManager.getExecutionContext(shellSessionId);

            if (executionContextOpt.isEmpty()) {
                // SSH 연결이 아직 완료되지 않음 - 연결 후 클라이언트가 다시 요청할 것이므로 무시
                log.debug("[{}] PTY 크기 변경 무시 (SSH 연결 중): {}x{}", shellSessionId, cols, rows);
                return;
            }

            SshExecutionContext executionContext = executionContextOpt.get();

            // 터미널 연결 확인
            if (!sshAdapter.isShellConnected(executionContext)) {
                log.debug("[{}] PTY 크기 변경 무시 (터미널 미연결): {}x{}", shellSessionId, cols, rows);
                return;
            }

            // PTY 크기 변경 (SSH Adapter 사용)
            sshAdapter.resizePty(executionContext, cols, rows);

            log.debug("[{}] PTY 크기 변경: {}x{}", shellSessionId, cols, rows);

        } catch (Exception e) {
            log.error("[{}] PTY 크기 변경 실패: {}x{}", shellSessionId, cols, rows, e);
            webSocketAdapter.sendErrorMessage(shellSessionId, "터미널 크기 변경 실패: " + e.getMessage());
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
     * 파일 업로드 (클라이언트 → 원격 호스트)
     *
     * @param terminalId 터미널 ID
     * @param file       업로드할 파일
     * @param remotePath 원격 경로 (디렉토리)
     * @return 파일 전송 응답
     * @throws BusinessException 파일 업로드 실패 시
     */
    public TerminalDto.FileTransferResponse uploadFile(
            String terminalId,
            MultipartFile file,
            String remotePath) {

        log.info("[{}] 파일 업로드 요청: {} → {}", terminalId, file.getOriginalFilename(), remotePath);

        // SSH 세션 가져오기
        Session sshSession = sessionManager.getSshSession(terminalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TERMINAL_NOT_CONNECTED,
                        "터미널 세션이 연결되어 있지 않습니다"));

        // 세션 연결 확인
        if (!sshAdapter.isConnected(sshSession)) {
            throw new BusinessException(ErrorCode.TERMINAL_NOT_CONNECTED,
                    "SSH 세션이 연결되어 있지 않습니다");
        }

        // 파일 업로드
        sshAdapter.uploadFile(sshSession, file, remotePath);

        log.info("[{}] 파일 업로드 완료: {} → {}", terminalId, file.getOriginalFilename(), remotePath);

        return TerminalDto.FileTransferResponse.builder()
                .fileName(file.getOriginalFilename())
                .remotePath(remotePath)
                .message("파일이 성공적으로 전송되었습니다")
                .transferredAt(LocalDateTime.now())
                .build();
    }

    /**
     * 패치 파일 배포 (서버 → 원격 호스트)
     *
     * @param terminalId 터미널 ID
     * @param patchId    패치 ID
     * @param remotePath 원격 경로 (디렉토리)
     * @return 파일 전송 응답
     * @throws BusinessException 패치 파일 배포 실패 시
     */
    public TerminalDto.FileTransferResponse deployPatch(
            String terminalId,
            Long patchId,
            String remotePath) {

        log.info("[{}] 패치 파일 배포 요청: patchId={} → {}", terminalId, patchId, remotePath);

        // SSH 세션 가져오기
        Session sshSession = sessionManager.getSshSession(terminalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TERMINAL_NOT_CONNECTED,
                        "터미널 세션이 연결되어 있지 않습니다"));

        // 세션 연결 확인
        if (!sshAdapter.isConnected(sshSession)) {
            throw new BusinessException(ErrorCode.TERMINAL_NOT_CONNECTED,
                    "SSH 세션이 연결되어 있지 않습니다");
        }

        // 패치 조회
        Patch patch = patchRepository.findById(patchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PATCH_FILE_NOT_FOUND,
                        "패치를 찾을 수 없습니다: " + patchId));

        // 패치 디렉토리 경로 (서버 내부)
        // outputPath는 상대 경로로 저장되므로 releaseBasePath와 결합하여 절대 경로 생성
        Path localPatchPath = Paths.get(releaseBasePath, patch.getOutputPath());

        // 패치 디렉토리 업로드 (재귀적으로 모든 파일 전송)
        sshAdapter.uploadLocalFile(sshSession, localPatchPath, remotePath);

        String patchName = localPatchPath.getFileName().toString();
        log.info("[{}] 패치 디렉토리 배포 완료: {} → {}", terminalId, patchName, remotePath);

        return TerminalDto.FileTransferResponse.builder()
                .fileName(patchName)
                .remotePath(remotePath)
                .message("패치 디렉토리가 성공적으로 배포되었습니다 (모든 파일 포함)")
                .transferredAt(LocalDateTime.now())
                .build();
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
