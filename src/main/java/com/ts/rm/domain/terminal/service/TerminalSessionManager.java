package com.ts.rm.domain.terminal.service;

import com.jcraft.jsch.Session;
import com.ts.rm.domain.terminal.adapter.SshAdapter;
import com.ts.rm.domain.terminal.dto.TerminalDto;
import com.ts.rm.domain.terminal.entity.Terminal;
import com.ts.rm.domain.terminal.enums.TerminalStatus;
import com.ts.rm.domain.terminal.repository.TerminalRepository;
import com.ts.rm.global.ssh.dto.SshExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 터미널 세션 관리 서비스
 * <p>
 * 메모리에서 SSH 터미널 세션을 관리하고, DB에 감사 기록을 저장합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TerminalSessionManager {

    private final TerminalRepository sessionRepository;
    private final SshAdapter sshAdapter;

    /**
     * 메모리에서 관리되는 활성 터미널 세션 맵
     * Key: shellSessionId (UUID), Value: ShellSessionContext
     */
    private final Map<String, ShellSessionContext> activeSessions = new ConcurrentHashMap<>();

    /**
     * 세션 기본 만료 시간 (60분)
     */
    private static final int DEFAULT_EXPIRY_MINUTES = 60;

    /**
     * 터미널 세션 생성
     *
     * @param request    SSH 연결 정보
     * @param ownerEmail 세션 소유자 이메일
     * @return 생성된 세션 정보
     */
    @Transactional
    public TerminalDto.ConnectResponse createSession(
            TerminalDto.ConnectRequest request,
            String ownerEmail) {

        // 세션 식별자 생성
        String shellSessionId = generateSessionIdentifier();

        // DB에 세션 기록 저장
        Terminal session = Terminal.builder()
                .terminalId(shellSessionId)
                .host(request.getHost())
                .port(request.getPort())
                .username(request.getUsername())
                .status(TerminalStatus.CONNECTING)
                .ownerEmail(ownerEmail)
                .lastActivityAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(DEFAULT_EXPIRY_MINUTES))
                .build();

        sessionRepository.save(session);

        // 메모리에 세션 컨텍스트 저장
        ShellSessionContext context = new ShellSessionContext(session, null, null);
        activeSessions.put(shellSessionId, context);

        log.info("터미널 세션 생성: shellSessionId={}, host={}@{}:{}",
                shellSessionId, request.getUsername(), request.getHost(), request.getPort());

        return TerminalDto.ConnectResponse.builder()
                .terminalId(shellSessionId)
                .status(TerminalStatus.CONNECTING)
                .host(request.getHost())
                .websocketUrl("/ws/terminal")
                .subscribeUrl("/topic/terminal/" + shellSessionId)
                .commandUrl("/app/terminal/" + shellSessionId + "/command")
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }

    /**
     * SSH 세션 및 터미널 실행 컨텍스트 연결
     *
     * @param shellSessionId   터미널 세션 식별자
     * @param sshSession       SSH 세션
     * @param executionContext SSH 실행 컨텍스트
     */
    public void attachShell(String shellSessionId,
                            Session sshSession,
                            SshExecutionContext executionContext) {
        ShellSessionContext context = activeSessions.get(shellSessionId);
        if (context != null) {
            context.setSshSession(sshSession);
            context.setExecutionContext(executionContext);
            updateSessionStatus(shellSessionId, TerminalStatus.CONNECTED);
            log.info("SSH 터미널 연결: shellSessionId={}", shellSessionId);
        }
    }

    /**
     * 세션 상태 업데이트
     *
     * @param shellSessionId 터미널 세션 식별자
     * @param status         새로운 상태
     */
    @Transactional
    public void updateSessionStatus(String shellSessionId, TerminalStatus status) {
        sessionRepository.findByTerminalId(shellSessionId)
                .ifPresent(session -> {
                    session.setStatus(status);
                    session.setLastActivityAt(LocalDateTime.now());

                    if (status == TerminalStatus.DISCONNECTED || status == TerminalStatus.ERROR) {
                        session.setDisconnectedAt(LocalDateTime.now());
                    }

                    sessionRepository.save(session);
                    log.debug("터미널 세션 상태 업데이트: shellSessionId={}, status={}", shellSessionId, status);
                });
    }

    /**
     * 마지막 활동 시간 업데이트
     * <p>
     * 모든 키 입력마다 호출되어 세션 활동을 추적합니다.
     * </p>
     *
     * @param shellSessionId 터미널 세션 식별자
     */
    @Transactional
    public void updateLastActivity(String shellSessionId) {
        sessionRepository.findByTerminalId(shellSessionId)
                .ifPresent(session -> {
                    session.setLastActivityAt(LocalDateTime.now());
                    sessionRepository.save(session);
                });
    }

    /**
     * 오류 메시지 저장
     *
     * @param shellSessionId 터미널 세션 식별자
     * @param errorMessage   오류 메시지
     */
    @Transactional
    public void updateErrorMessage(String shellSessionId, String errorMessage) {
        sessionRepository.findByTerminalId(shellSessionId)
                .ifPresent(session -> {
                    session.setErrorMessage(errorMessage);
                    session.setStatus(TerminalStatus.ERROR);
                    sessionRepository.save(session);
                    log.warn("터미널 세션 오류: shellSessionId={}, error={}", shellSessionId, errorMessage);
                });
    }

    /**
     * 세션 조회
     *
     * @param shellSessionId 터미널 세션 식별자
     * @return 세션 정보
     */
    public Optional<TerminalDto.ShellSessionInfo> getSession(String shellSessionId) {
        return sessionRepository.findByTerminalId(shellSessionId)
                .map(session -> TerminalDto.ShellSessionInfo.builder()
                        .terminalId(session.getTerminalId())
                        .status(session.getStatus())
                        .host(session.getHost())
                        .username(session.getUsername())
                        .ownerEmail(session.getOwnerEmail())
                        .createdAt(session.getCreatedAt())
                        .lastActivityAt(session.getLastActivityAt())
                        .expiresAt(session.getExpiresAt())
                        .build());
    }

    /**
     * SSH 세션 가져오기
     *
     * @param shellSessionId 터미널 세션 식별자
     * @return SSH 세션
     */
    public Optional<Session> getSshSession(String shellSessionId) {
        ShellSessionContext context = activeSessions.get(shellSessionId);
        return context != null ? Optional.ofNullable(context.getSshSession()) : Optional.empty();
    }

    /**
     * SSH 실행 컨텍스트 가져오기
     *
     * @param shellSessionId 터미널 세션 식별자
     * @return SSH 실행 컨텍스트
     */
    public Optional<SshExecutionContext> getExecutionContext(String shellSessionId) {
        ShellSessionContext context = activeSessions.get(shellSessionId);
        return context != null ? Optional.ofNullable(context.getExecutionContext()) : Optional.empty();
    }

    /**
     * 세션 종료 및 정리
     *
     * @param shellSessionId 터미널 세션 식별자
     */
    @Transactional
    public void closeSession(String shellSessionId) {
        ShellSessionContext context = activeSessions.remove(shellSessionId);

        if (context != null) {
            // 터미널 실행 컨텍스트 종료
            SshExecutionContext executionContext = context.getExecutionContext();
            if (executionContext != null) {
                // closeShell은 Orchestrator에서 호출
            }

            // SSH 세션 종료
            Session sshSession = context.getSshSession();
            if (sshSession != null) {
                sshAdapter.disconnect(sshSession);
            }

            log.info("터미널 세션 종료: shellSessionId={}", shellSessionId);
        }

        updateSessionStatus(shellSessionId, TerminalStatus.DISCONNECTED);
    }

    /**
     * 만료된 세션 정리 (매 10분마다 실행)
     */
    @Scheduled(fixedDelay = 600000) // 10분
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        activeSessions.entrySet().removeIf(entry -> {
            String sessionId = entry.getKey();
            ShellSessionContext context = entry.getValue();
            Terminal session = context.getSession();

            if (session.getExpiresAt().isBefore(now)) {
                log.info("만료된 터미널 세션 정리: shellSessionId={}", sessionId);

                // 터미널 실행 컨텍스트 종료
                if (context.getExecutionContext() != null) {
                    // closeShell은 Orchestrator에서 호출
                }

                Session sshSession = context.getSshSession();
                if (sshSession != null) {
                    sshAdapter.disconnect(sshSession);
                }

                // DB 상태 업데이트
                updateSessionStatus(sessionId, TerminalStatus.DISCONNECTED);
                updateErrorMessage(sessionId, "세션 만료");

                return true;
            }
            return false;
        });
    }

    /**
     * 세션 식별자 생성
     *
     * @return 세션 식별자 (terminal_{timestamp}_{uuid})
     */
    private String generateSessionIdentifier() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH_mm_ss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("terminal_%s_%s", timestamp, uuid);
    }

    /**
     * 터미널 세션 컨텍스트 (메모리에서 관리)
     */
    private static class ShellSessionContext {
        private final Terminal session;
        private Session sshSession;
        private SshExecutionContext executionContext;

        public ShellSessionContext(Terminal session,
                                   Session sshSession,
                                   SshExecutionContext executionContext) {
            this.session = session;
            this.sshSession = sshSession;
            this.executionContext = executionContext;
        }

        public Terminal getSession() {
            return session;
        }

        public Session getSshSession() {
            return sshSession;
        }

        public void setSshSession(Session sshSession) {
            this.sshSession = sshSession;
        }

        public SshExecutionContext getExecutionContext() {
            return executionContext;
        }

        public void setExecutionContext(SshExecutionContext executionContext) {
            this.executionContext = executionContext;
        }
    }
}
