package com.ts.rm.global.ssh.client;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.ssh.dto.SshConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SSH 클라이언트
 * <p>
 * JSch 라이브러리를 사용하여 SSH 연결을 관리하는 범용 클라이언트입니다.
 * 비즈니스 로직과 독립적으로 재사용 가능합니다.
 * </p>
 */
@Slf4j
@Component
public class SshClient {

    /**
     * SSH 연결 생성
     *
     * @param connectionInfo SSH 연결 정보
     * @return SSH 세션
     * @throws BusinessException SSH 연결 실패
     */
    public Session connect(SshConnectionInfo connectionInfo) {
        validateConnectionInfo(connectionInfo);

        try {
            JSch jsch = new JSch();

            // SSH 세션 생성
            Session session = jsch.getSession(
                    connectionInfo.getUsername(),
                    connectionInfo.getHost(),
                    connectionInfo.getPort()
            );

            // 비밀번호 설정
            session.setPassword(connectionInfo.getPassword());

            // SSH 설정
            configureSession(session, connectionInfo);

            // 연결
            log.info("SSH 연결 시도: {}@{}:{}",
                    connectionInfo.getUsername(),
                    connectionInfo.getHost(),
                    connectionInfo.getPort());

            session.connect();

            log.info("SSH 연결 성공: {}@{}:{}",
                    connectionInfo.getUsername(),
                    connectionInfo.getHost(),
                    connectionInfo.getPort());

            return session;

        } catch (JSchException e) {
            String errorMessage = String.format("SSH 연결 실패: %s@%s:%d - %s",
                    connectionInfo.getUsername(),
                    connectionInfo.getHost(),
                    connectionInfo.getPort(),
                    e.getMessage());

            log.error(errorMessage);

            // 인증 실패 vs 연결 실패 구분
            if (e.getMessage() != null && e.getMessage().contains("Auth fail")) {
                throw new BusinessException(ErrorCode.SSH_AUTHENTICATION_FAILED, errorMessage);
            }
            throw new BusinessException(ErrorCode.SSH_CONNECTION_FAILED, errorMessage);
        }
    }

    /**
     * SSH 연결 해제
     *
     * @param session SSH 세션
     */
    public void disconnect(Session session) {
        if (session != null && session.isConnected()) {
            String connectionInfo = String.format("%s@%s",
                    session.getUserName(),
                    session.getHost());

            session.disconnect();
            log.info("SSH 연결 종료: {}", connectionInfo);
        }
    }

    /**
     * SSH 연결 상태 확인
     *
     * @param session SSH 세션
     * @return 연결 여부
     */
    public boolean isConnected(Session session) {
        return session != null && session.isConnected();
    }

    /**
     * SSH 세션 설정
     *
     * @param session        SSH 세션
     * @param connectionInfo 연결 정보
     */
    private void configureSession(Session session, SshConnectionInfo connectionInfo) {
        try {
            // 호스트 키 검증 설정
            String hostKeyChecking = connectionInfo.getStrictHostKeyChecking() ? "yes" : "no";
            session.setConfig("StrictHostKeyChecking", hostKeyChecking);

            // 타임아웃 설정
            session.setTimeout(connectionInfo.getTimeout());

            // 추가 설정
            session.setConfig("PreferredAuthentications", "password");
        } catch (JSchException e) {
            log.warn("SSH 세션 설정 중 오류 (일부 설정 적용 안됨): {}", e.getMessage());
        }
    }

    /**
     * 연결 정보 유효성 검증
     *
     * @param connectionInfo 연결 정보
     * @throws IllegalArgumentException 유효하지 않은 연결 정보
     */
    private void validateConnectionInfo(SshConnectionInfo connectionInfo) {
        if (connectionInfo == null) {
            throw new IllegalArgumentException("SSH 연결 정보는 필수입니다");
        }
        if (connectionInfo.getHost() == null || connectionInfo.getHost().isBlank()) {
            throw new IllegalArgumentException("SSH 호스트는 필수입니다");
        }
        if (connectionInfo.getPort() == null || connectionInfo.getPort() < 1 || connectionInfo.getPort() > 65535) {
            throw new IllegalArgumentException("SSH 포트는 1-65535 범위여야 합니다");
        }
        if (connectionInfo.getUsername() == null || connectionInfo.getUsername().isBlank()) {
            throw new IllegalArgumentException("SSH 사용자명은 필수입니다");
        }
        if (connectionInfo.getPassword() == null || connectionInfo.getPassword().isBlank()) {
            throw new IllegalArgumentException("SSH 비밀번호는 필수입니다");
        }
    }
}
