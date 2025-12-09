package com.ts.rm.domain.terminal.service;

import com.ts.rm.domain.terminal.dto.TerminalDto.OutputMessage;
import com.ts.rm.domain.terminal.entity.TerminalSession;
import com.ts.rm.domain.terminal.entity.TerminalType;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 터미널 세션 관리 서비스
 * <p>
 * 터미널 세션의 생성, 조회, 종료 및 입출력 처리를 담당합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TerminalSessionManager {

    private final ProcessExecutorService processExecutor;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, TerminalSession> sessions = new ConcurrentHashMap<>();

    @Value("${app.terminal.session-timeout-minutes:60}")
    private int sessionTimeoutMinutes;

    @Value("${app.terminal.max-sessions-per-user:3}")
    private int maxSessionsPerUser;

    /**
     * 순수 셸 세션 시작
     *
     * @param userEmail 사용자 이메일
     * @param workingDirectory 작업 디렉토리 (선택)
     * @return 생성된 세션 ID
     * @throws IOException 프로세스 실행 실패
     */
    public String startShellSession(String userEmail, String workingDirectory) throws IOException {
        // 사용자별 세션 개수 제한
        long userSessionCount = sessions.values().stream()
                .filter(s -> userEmail.equals(s.getOwnerEmail()))
                .count();

        if (userSessionCount >= maxSessionsPerUser) {
            throw new IllegalStateException(
                    String.format("사용자당 최대 %d개 세션까지 생성 가능합니다", maxSessionsPerUser)
            );
        }

        // 세션 ID 생성
        String sessionId = generateSessionId();

        // bash 셸 실행
        Process process = processExecutor.executeShell(workingDirectory);

        // 세션 생성
        LocalDateTime now = LocalDateTime.now();
        TerminalSession session = TerminalSession.builder()
                .sessionId(sessionId)
                .type(TerminalType.SHELL)
                .ownerEmail(userEmail)
                .scriptPath("/bin/bash")
                .workingDirectory(workingDirectory)
                .process(process)
                .stdout(process.getInputStream())
                .stderr(process.getErrorStream())
                .stdin(process.getOutputStream())
                .createdAt(now)
                .lastActivityAt(now)
                .expiresAt(now.plusMinutes(sessionTimeoutMinutes))
                .build();

        sessions.put(sessionId, session);

        // 출력 스트림 읽기 시작 (비동기)
        setupOutputStreams(sessionId, session);

        // 초기 프롬프트 전송 (Windows cmd.exe는 자동으로 프롬프트를 출력하지 않음)
        sendInitialPrompt(sessionId, workingDirectory);

        log.info("셸 세션 시작: sessionId={}, user={}", sessionId, userEmail);

        return sessionId;
    }

    /**
     * 스크립트 실행 세션 시작
     *
     * @param userEmail 사용자 이메일
     * @param scriptPath 스크립트 경로
     * @param workingDirectory 작업 디렉토리
     * @return 생성된 세션 ID
     * @throws IOException 프로세스 실행 실패
     */
    public String startScriptSession(String userEmail, String scriptPath, String workingDirectory) throws IOException {
        // 사용자별 세션 개수 제한
        long userSessionCount = sessions.values().stream()
                .filter(s -> userEmail.equals(s.getOwnerEmail()))
                .count();

        if (userSessionCount >= maxSessionsPerUser) {
            throw new IllegalStateException(
                    String.format("사용자당 최대 %d개 세션까지 생성 가능합니다", maxSessionsPerUser)
            );
        }

        // 세션 ID 생성
        String sessionId = generateSessionId();

        // 프로세스 실행
        Process process = processExecutor.executeScript(scriptPath, workingDirectory);

        // 세션 생성
        LocalDateTime now = LocalDateTime.now();
        TerminalSession session = TerminalSession.builder()
                .sessionId(sessionId)
                .type(TerminalType.SCRIPT)
                .ownerEmail(userEmail)
                .scriptPath(scriptPath)
                .workingDirectory(workingDirectory)
                .process(process)
                .stdout(process.getInputStream())
                .stderr(process.getErrorStream())
                .stdin(process.getOutputStream())
                .createdAt(now)
                .lastActivityAt(now)
                .expiresAt(now.plusMinutes(sessionTimeoutMinutes))
                .build();

        sessions.put(sessionId, session);

        // 출력 스트림 읽기 시작 (비동기)
        setupOutputStreams(sessionId, session);

        log.info("스크립트 세션 시작: sessionId={}, user={}, script={}",
                sessionId, userEmail, scriptPath);

        return sessionId;
    }

    /**
     * 출력 스트림 설정 (stdout, stderr, exit)
     */
    private void setupOutputStreams(String sessionId, TerminalSession session) {
        // stdout 읽기
        processExecutor.readOutputAsync(
                session.getStdout(),
                message -> sendToClient(sessionId, message),
                false
        );

        // stderr 읽기
        processExecutor.readOutputAsync(
                session.getStderr(),
                message -> sendToClient(sessionId, message),
                true
        );

        // 프로세스 종료 대기
        processExecutor.waitForProcessAsync(
                session.getProcess(),
                message -> {
                    sendToClient(sessionId, message);
                    // 프로세스 종료 시 세션 제거 (5초 후)
                    scheduleSessionCleanup(sessionId, 5000);
                }
        );
    }

    /**
     * 클라이언트로 메시지 전송
     */
    private void sendToClient(String sessionId, OutputMessage message) {
        try {
            messagingTemplate.convertAndSend("/topic/terminal/" + sessionId, message);
            log.trace("메시지 전송: sessionId={}, type={}", sessionId, message.getType());
        } catch (Exception e) {
            log.error("메시지 전송 실패: sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    /**
     * 세션 정리 예약
     */
    private void scheduleSessionCleanup(String sessionId, long delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                terminateSession(sessionId, null);
                log.info("세션 자동 정리 완료: sessionId={}", sessionId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 터미널 입력을 프로세스로 전송
     *
     * @param sessionId 세션 ID
     * @param userEmail 사용자 이메일
     * @param input 입력 데이터
     */
    public void sendInput(String sessionId, String userEmail, String input) {
        TerminalSession session = getSessionWithPermission(sessionId, userEmail);

        try {
            // bash stdin으로 전송
            processExecutor.sendInput(session.getStdin(), input);
            session.updateLastActivity();

            log.debug("입력 전송: sessionId={}, length={}", sessionId, input.length());
        } catch (IOException e) {
            log.error("입력 전송 실패: sessionId={}, error={}", sessionId, e.getMessage());
            throw new RuntimeException("입력 전송 실패", e);
        }
    }

    /**
     * 시그널 전송 (SIGINT, SIGTERM 등)
     *
     * @param sessionId 세션 ID
     * @param userEmail 사용자 이메일
     * @param signal 시그널 타입
     */
    public void sendSignal(String sessionId, String userEmail, String signal) {
        TerminalSession session = getSessionWithPermission(sessionId, userEmail);

        boolean force = "SIGKILL".equals(signal);
        processExecutor.terminateProcess(session.getProcess(), force);
        session.updateLastActivity();

        log.info("시그널 전송: sessionId={}, signal={}", sessionId, signal);
    }

    /**
     * 세션 조회 (권한 검증 포함)
     *
     * @param sessionId 세션 ID
     * @param userEmail 사용자 이메일
     * @return 터미널 세션
     */
    private TerminalSession getSessionWithPermission(String sessionId, String userEmail) {
        TerminalSession session = sessions.get(sessionId);

        if (session == null) {
            throw new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId);
        }

        if (!userEmail.equals(session.getOwnerEmail())) {
            throw new SecurityException("세션 접근 권한이 없습니다");
        }

        return session;
    }

    /**
     * 세션 종료
     *
     * @param sessionId 세션 ID
     * @param userEmail 사용자 이메일 (null이면 권한 검증 생략)
     */
    public void terminateSession(String sessionId, String userEmail) {
        TerminalSession session = userEmail != null
                ? getSessionWithPermission(sessionId, userEmail)
                : sessions.get(sessionId);

        if (session != null) {
            processExecutor.terminateProcess(session.getProcess(), false);
            sessions.remove(sessionId);
            log.info("세션 종료: sessionId={}", sessionId);
        }
    }

    /**
     * 사용자의 활성 세션 목록 조회
     *
     * @param userEmail 사용자 이메일
     * @return 세션 목록
     */
    public List<TerminalSession> getUserSessions(String userEmail) {
        return sessions.values().stream()
                .filter(s -> userEmail.equals(s.getOwnerEmail()))
                .toList();
    }

    /**
     * 초기 프롬프트 전송
     * <p>
     * Windows cmd.exe는 대화형 모드에서 자동으로 프롬프트를 출력하지 않으므로
     * 수동으로 초기 환영 메시지와 프롬프트를 전송합니다.
     * </p>
     */
    private void sendInitialPrompt(String sessionId, String workingDirectory) {
        try {
            // OS 감지
            String osName = System.getProperty("os.name").toLowerCase();
            boolean isWindows = osName.contains("win");

            String initialMessage;
            if (isWindows) {
                // Windows cmd.exe 초기 메시지
                String workDir = workingDirectory != null && !workingDirectory.isBlank()
                        ? workingDirectory
                        : System.getProperty("user.dir");

                initialMessage = String.format(
                        "Microsoft Windows [Version %s]\r\n(c) Release Manager Terminal.\r\n\r\n%s>",
                        System.getProperty("os.version", ""),
                        workDir
                );
            } else {
                // Linux/Mac bash 초기 메시지
                String username = System.getProperty("user.name", "user");
                String hostname = "localhost";
                try {
                    hostname = java.net.InetAddress.getLocalHost().getHostName();
                } catch (Exception e) {
                    // 호스트명을 가져올 수 없으면 기본값 사용
                }
                initialMessage = String.format("%s@%s:~$ ", username, hostname);
            }

            // 초기 프롬프트 전송
            OutputMessage welcomeMessage = OutputMessage.builder()
                    .type("output")
                    .data(initialMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            sendToClient(sessionId, welcomeMessage);
            log.debug("초기 프롬프트 전송 완료: sessionId={}", sessionId);

        } catch (Exception e) {
            log.warn("초기 프롬프트 전송 실패: sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    /**
     * 세션 ID 생성
     */
    private String generateSessionId() {
        return "term_" + LocalDateTime.now().toString().replace(":", "").replace(".", "_")
                + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 주기적으로 만료된 세션 정리 (5분마다)
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        int cleanedCount = 0;

        for (Map.Entry<String, TerminalSession> entry : sessions.entrySet()) {
            TerminalSession session = entry.getValue();
            if (session.isExpired() || !session.isAlive()) {
                terminateSession(entry.getKey(), null);
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            log.info("만료된 세션 정리 완료: {} 개", cleanedCount);
        }
    }

    /**
     * 애플리케이션 종료 시 모든 세션 정리
     */
    @PreDestroy
    public void cleanup() {
        log.info("모든 세션 종료 시작: {} 개", sessions.size());
        sessions.keySet().forEach(sessionId -> terminateSession(sessionId, null));
        log.info("모든 세션 종료 완료");
    }
}
