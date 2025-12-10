package com.ts.rm.global.ssh.dto;

import com.jcraft.jsch.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * SSH 실행 컨텍스트
 * <p>
 * SSH 명령 실행 또는 대화형 셸의 실행 컨텍스트를 담습니다.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
public class SshExecutionContext {
    /**
     * SSH 채널 (ChannelShell, ChannelExec 등)
     */
    private Channel channel;

    /**
     * 출력 스트림 (서버로 데이터 전송)
     */
    private OutputStream outputStream;

    /**
     * 입력 스트림 (서버로부터 데이터 수신)
     */
    private InputStream inputStream;

    /**
     * 에러 스트림 (서버로부터 에러 수신)
     */
    private InputStream errorStream;

    /**
     * 출력 읽기 스레드 (대화형 셸의 경우)
     */
    private Thread outputReaderThread;

    /**
     * 실행 상태
     */
    private volatile boolean running;

    /**
     * 실행 중지
     */
    public void stop() {
        this.running = false;
    }

    /**
     * 연결 상태 확인
     */
    public boolean isConnected() {
        return channel != null && channel.isConnected() && running;
    }
}
