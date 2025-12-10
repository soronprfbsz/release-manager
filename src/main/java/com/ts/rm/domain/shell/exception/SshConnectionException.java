package com.ts.rm.domain.shell.exception;

/**
 * SSH 연결 예외
 */
public class SshConnectionException extends RuntimeException {
    public SshConnectionException(String message) {
        super(message);
    }

    public SshConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
