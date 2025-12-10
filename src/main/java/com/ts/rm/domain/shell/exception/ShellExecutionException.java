package com.ts.rm.domain.shell.exception;

/**
 * 셸 실행 예외
 */
public class ShellExecutionException extends RuntimeException {
    public ShellExecutionException(String message) {
        super(message);
    }

    public ShellExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
