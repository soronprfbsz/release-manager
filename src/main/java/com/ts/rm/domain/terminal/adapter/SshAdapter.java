package com.ts.rm.domain.terminal.adapter;

import com.jcraft.jsch.Session;
import com.ts.rm.domain.terminal.dto.SshConnectionDto;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.ssh.client.SshClient;
import com.ts.rm.global.ssh.dto.SshConnectionInfo;
import com.ts.rm.global.ssh.dto.SshExecutionContext;
import com.ts.rm.global.ssh.executor.SftpExecutor;
import com.ts.rm.global.ssh.executor.SshShellExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * SSH 어댑터
 * <p>
 * Global SSH 모듈을 Domain Shell 모듈에 연결하는 어댑터입니다.
 * Global 예외를 Domain 예외로 변환합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SshAdapter {

    private final SshClient sshClient;
    private final SshShellExecutor interactiveExecutor;
    private final SftpExecutor sftpExecutor;

    /**
     * SSH 연결
     *
     * @param connectionDto Domain SSH 연결 정보
     * @return SSH 세션
     * @throws BusinessException SSH 연결 실패
     */
    public Session connect(SshConnectionDto connectionDto) {
        // Domain DTO → Global DTO 변환
        SshConnectionInfo connectionInfo = convertToConnectionInfo(connectionDto);

        // Global SSH 클라이언트 사용 (예외는 Global에서 BusinessException으로 발생)
        return sshClient.connect(connectionInfo);
    }

    /**
     * SSH 연결 해제
     *
     * @param session SSH 세션
     */
    public void disconnect(Session session) {
        sshClient.disconnect(session);
    }

    /**
     * SSH 연결 상태 확인
     *
     * @param session SSH 세션
     * @return 연결 여부
     */
    public boolean isConnected(Session session) {
        return sshClient.isConnected(session);
    }

    /**
     * 대화형 터미널 열기
     *
     * @param session        SSH 세션
     * @param outputConsumer 출력 처리 콜백
     * @return SSH 실행 컨텍스트
     * @throws BusinessException 터미널 열기 실패
     */
    public SshExecutionContext openShell(Session session, Consumer<String> outputConsumer) {
        // Global SSH 실행기 사용 (예외는 Global에서 BusinessException으로 발생)
        return interactiveExecutor.openShell(session, outputConsumer);
    }

    /**
     * 입력 전송 (키 입력)
     *
     * @param context SSH 실행 컨텍스트
     * @param input   입력 데이터
     * @throws BusinessException 입력 전송 실패
     */
    public void writeInput(SshExecutionContext context, String input) {
        // Global SSH 실행기 사용 (예외는 Global에서 BusinessException으로 발생)
        interactiveExecutor.writeInput(context, input);
    }

    /**
     * 터미널 닫기
     *
     * @param context SSH 실행 컨텍스트
     */
    public void closeShell(SshExecutionContext context) {
        interactiveExecutor.closeShell(context);
    }

    /**
     * 터미널 연결 상태 확인
     *
     * @param context SSH 실행 컨텍스트
     * @return 연결 여부
     */
    public boolean isShellConnected(SshExecutionContext context) {
        return context != null && context.isConnected();
    }

    /**
     * 파일 업로드 (MultipartFile)
     *
     * @param session    SSH 세션
     * @param file       업로드할 파일
     * @param remotePath 원격 경로 (디렉토리)
     * @throws BusinessException 파일 업로드 실패
     */
    public void uploadFile(Session session, MultipartFile file, String remotePath) {
        try (InputStream inputStream = file.getInputStream()) {
            String fileName = file.getOriginalFilename();
            sftpExecutor.uploadFile(session, inputStream, fileName, remotePath);
        } catch (IOException e) {
            String errorMessage = String.format("파일 입력 스트림 읽기 실패: %s (%s)",
                    file.getOriginalFilename(), e.getMessage());
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_IO_ERROR, errorMessage);
        }
    }

    /**
     * 로컬 파일 업로드
     *
     * @param session    SSH 세션
     * @param localPath  로컬 파일 경로
     * @param remotePath 원격 경로 (디렉토리)
     * @throws BusinessException 파일 업로드 실패
     */
    public void uploadLocalFile(Session session, Path localPath, String remotePath) {
        sftpExecutor.uploadLocalFile(session, localPath, remotePath);
    }

    /**
     * Domain DTO → Global DTO 변환
     *
     * @param dto Domain SSH 연결 정보
     * @return Global SSH 연결 정보
     */
    private SshConnectionInfo convertToConnectionInfo(SshConnectionDto dto) {
        return SshConnectionInfo.builder()
                .host(dto.getHost())
                .port(dto.getPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .timeout(30000)
                .strictHostKeyChecking(false)
                .build();
    }
}
