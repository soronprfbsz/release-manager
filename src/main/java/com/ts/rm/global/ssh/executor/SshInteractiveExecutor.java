package com.ts.rm.global.ssh.executor;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.ssh.dto.SshExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * SSH 대화형 실행기
 * <p>
 * ChannelShell을 사용하여 대화형 SSH 터미널을 실행합니다.
 * 비즈니스 로직과 독립적으로 재사용 가능합니다.
 * </p>
 */
@Slf4j
@Component
public class SshInteractiveExecutor {

    /**
     * 기본 PTY 설정
     */
    private static final String DEFAULT_PTY_TYPE = "xterm";
    private static final int DEFAULT_COLS = 100;
    private static final int DEFAULT_ROWS = 30;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    /**
     * 대화형 셸 열기
     *
     * @param session        SSH 세션
     * @param outputConsumer 출력 처리 콜백 (실시간 출력 전달)
     * @return SSH 실행 컨텍스트
     * @throws BusinessException 셸 열기 실패
     */
    public SshExecutionContext openShell(Session session, Consumer<String> outputConsumer) {
        validateSession(session);

        try {
            // ChannelShell 열기
            ChannelShell channel = (ChannelShell) session.openChannel("shell");

            // PTY 설정 (Pseudo Terminal)
            configurePty(channel);

            // 환경 변수 설정 (UTF-8 인코딩 지원)
            configureEnvironment(channel);

            // 입출력 스트림 설정
            OutputStream outputStream = channel.getOutputStream();
            InputStream inputStream = channel.getInputStream();

            log.info("대화형 셸 열기 시작 (PTY: {}, Size: {}x{})",
                    DEFAULT_PTY_TYPE, DEFAULT_COLS, DEFAULT_ROWS);

            channel.connect();

            log.info("대화형 셸 연결 성공");

            // 비동기 출력 읽기 스레드 시작
            Thread outputReaderThread = startOutputReader(inputStream, outputConsumer);

            SshExecutionContext context = SshExecutionContext.builder()
                    .channel(channel)
                    .outputStream(outputStream)
                    .inputStream(inputStream)
                    .outputReaderThread(outputReaderThread)
                    .running(true)
                    .build();

            // 초기 프롬프트 출력 대기
            waitForInitialPrompt();

            return context;

        } catch (JSchException e) {
            String errorMessage = "셸 채널 열기 실패: " + e.getMessage();
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_CHANNEL_OPEN_FAILED, errorMessage);
        } catch (IOException e) {
            String errorMessage = "셸 입출력 스트림 설정 실패: " + e.getMessage();
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_IO_ERROR, errorMessage);
        }
    }

    /**
     * 입력 전송 (키 입력)
     * <p>
     * 클라이언트에서 전송한 키 입력을 그대로 SSH 서버로 전달합니다.
     * </p>
     *
     * @param context SSH 실행 컨텍스트
     * @param input   입력 데이터 (단일 문자 또는 특수 키)
     * @throws BusinessException 입력 전송 실패
     */
    public void writeInput(SshExecutionContext context, String input) {
        validateExecutionContext(context);

        try {
            OutputStream out = context.getOutputStream();

            // 입력 데이터를 그대로 전송 (개행 추가 안함)
            out.write(input.getBytes(StandardCharsets.UTF_8));
            out.flush();

            log.debug("입력 전송: {} (length: {})",
                    input.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n"),
                    input.length());

        } catch (IOException e) {
            String errorMessage = "입력 전송 실패: " + e.getMessage();
            log.error(errorMessage);
            throw new BusinessException(ErrorCode.SSH_IO_ERROR, errorMessage);
        }
    }

    /**
     * 셸 닫기
     *
     * @param context SSH 실행 컨텍스트
     */
    public void closeShell(SshExecutionContext context) {
        if (context == null) {
            return;
        }

        log.info("대화형 셸 닫기");

        // 출력 읽기 중지
        context.stop();

        // 채널 종료
        if (context.getChannel() != null && context.getChannel().isConnected()) {
            context.getChannel().disconnect();
        }

        // 출력 읽기 스레드 종료 대기
        Thread readerThread = context.getOutputReaderThread();
        if (readerThread != null && readerThread.isAlive()) {
            try {
                readerThread.join(2000); // 최대 2초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("출력 읽기 스레드 종료 대기 중 인터럽트: {}", e.getMessage());
            }
        }

        log.info("대화형 셸 닫기 완료");
    }

    /**
     * PTY 설정
     *
     * @param channel ChannelShell
     */
    private void configurePty(ChannelShell channel) {
        channel.setPtyType(DEFAULT_PTY_TYPE);
        channel.setPtySize(DEFAULT_COLS, DEFAULT_ROWS, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 환경 변수 설정
     *
     * @param channel ChannelShell
     */
    private void configureEnvironment(ChannelShell channel) {
        channel.setEnv("TERM", DEFAULT_PTY_TYPE);
        channel.setEnv("LANG", "en_US.UTF-8");
        channel.setEnv("LC_ALL", "en_US.UTF-8");
    }

    /**
     * 출력 읽기 스레드 시작
     *
     * @param inputStream    입력 스트림
     * @param outputConsumer 출력 처리 콜백
     * @return 출력 읽기 스레드
     */
    private Thread startOutputReader(InputStream inputStream, Consumer<String> outputConsumer) {
        Thread thread = new Thread(() -> readOutputAsync(inputStream, outputConsumer));
        thread.setName("SshOutputReader");
        thread.start();
        return thread;
    }

    /**
     * 비동기 출력 읽기
     *
     * @param inputStream    입력 스트림
     * @param outputConsumer 출력 처리 콜백
     */
    private void readOutputAsync(InputStream inputStream, Consumer<String> outputConsumer) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            char[] buffer = new char[1024];
            int bytesRead;

            while ((bytesRead = reader.read(buffer)) != -1) {
                String output = new String(buffer, 0, bytesRead);

                // 출력 전달
                if (outputConsumer != null && !output.isEmpty()) {
                    outputConsumer.accept(output);
                }
            }

        } catch (IOException e) {
            if (e.getMessage() != null && !e.getMessage().contains("Stream closed")) {
                log.error("셸 출력 읽기 오류: {}", e.getMessage());
            }
        }

        log.debug("셸 출력 읽기 종료");
    }

    /**
     * 초기 프롬프트 출력 대기
     */
    private void waitForInitialPrompt() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * SSH 세션 유효성 검증
     *
     * @param session SSH 세션
     */
    private void validateSession(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("SSH 세션은 필수입니다");
        }
        if (!session.isConnected()) {
            throw new IllegalArgumentException("SSH 세션이 연결되어 있지 않습니다");
        }
    }

    /**
     * SSH 실행 컨텍스트 유효성 검증
     *
     * @param context SSH 실행 컨텍스트
     */
    private void validateExecutionContext(SshExecutionContext context) {
        if (context == null) {
            throw new IllegalArgumentException("SSH 실행 컨텍스트는 필수입니다");
        }
        if (!context.isConnected()) {
            throw new BusinessException(ErrorCode.SHELL_NOT_CONNECTED);
        }
    }
}
