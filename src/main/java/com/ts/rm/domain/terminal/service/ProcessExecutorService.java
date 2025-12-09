package com.ts.rm.domain.terminal.service;

import com.ts.rm.domain.terminal.dto.TerminalDto.OutputMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 셸 프로세스 실행 서비스
 * <p>
 * ProcessBuilder를 사용하여 Bash 스크립트를 실행하고 I/O 스트림을 관리합니다.
 * </p>
 */
@Slf4j
@Service
public class ProcessExecutorService {

    @Value("${app.release.base-path:/app/release}")
    private String releaseBasePath;

    /**
     * 순수 셸 실행 (OS별 자동 감지)
     *
     * @param workingDir 작업 디렉토리 (선택, null이면 release base path 사용)
     * @return 실행된 프로세스
     * @throws IOException 프로세스 실행 실패
     */
    public Process executeShell(String workingDir) throws IOException {
        File workDir = workingDir != null && !workingDir.isBlank()
                ? new File(workingDir)
                : new File(releaseBasePath);

        // OS 감지
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");

        log.info("셸 프로세스 실행: os={}, workDir={}", osName, workDir.getAbsolutePath());

        ProcessBuilder processBuilder;

        if (isWindows) {
            // Windows: cmd.exe 실행
            processBuilder = new ProcessBuilder("cmd.exe");
            processBuilder.directory(workDir);
            processBuilder.redirectErrorStream(false);
        } else {
            // Linux/Mac: bash 실행
            processBuilder = new ProcessBuilder("/bin/bash", "-i");
            processBuilder.directory(workDir);
            processBuilder.redirectErrorStream(false);

            // 환경 변수 설정 (프롬프트 표시)
            processBuilder.environment().put("PS1", "\\u@\\h:\\w$ ");
        }

        // 프로세스 시작
        return processBuilder.start();
    }

    /**
     * 셸 스크립트 실행 (OS별 자동 감지)
     *
     * @param scriptPath 스크립트 상대 경로 (예: patches/.../script.sh)
     * @param workingDir 작업 디렉토리 (선택)
     * @return 실행된 프로세스
     * @throws IOException 프로세스 실행 실패
     */
    public Process executeScript(String scriptPath, String workingDir) throws IOException {
        // 경로 검증 (Path Traversal 공격 방지)
        validatePath(scriptPath);

        // 절대 경로 생성
        File scriptFile = new File(releaseBasePath, scriptPath);
        if (!scriptFile.exists()) {
            throw new IOException("스크립트 파일이 존재하지 않습니다: " + scriptFile.getAbsolutePath());
        }

        // 작업 디렉토리 설정
        File workDir = workingDir != null && !workingDir.isBlank()
                ? new File(workingDir)
                : scriptFile.getParentFile();

        // OS 감지
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isWindows = osName.contains("win");

        log.info("스크립트 실행: os={}, script={}, workDir={}",
                osName, scriptFile.getAbsolutePath(), workDir.getAbsolutePath());

        ProcessBuilder processBuilder;

        if (isWindows) {
            // Windows: .bat/.cmd 파일은 cmd.exe로, .sh 파일은 Git Bash 경로로 실행 시도
            if (scriptPath.endsWith(".sh")) {
                // Git Bash가 설치되어 있다고 가정 (또는 WSL bash 경로)
                String[] bashPaths = {
                        "C:\\Program Files\\Git\\bin\\bash.exe",
                        "C:\\Program Files (x86)\\Git\\bin\\bash.exe",
                        "bash.exe" // PATH에 있는 경우
                };

                IOException lastException = null;
                for (String bashPath : bashPaths) {
                    try {
                        processBuilder = new ProcessBuilder(bashPath, scriptFile.getAbsolutePath());
                        processBuilder.directory(workDir);
                        processBuilder.redirectErrorStream(false);
                        return processBuilder.start();
                    } catch (IOException e) {
                        lastException = e;
                        log.debug("bash 실행 실패 ({}): {}", bashPath, e.getMessage());
                    }
                }
                throw new IOException("Windows에서 .sh 스크립트 실행 실패: Git Bash가 설치되어 있지 않습니다", lastException);
            } else {
                // .bat, .cmd 파일
                processBuilder = new ProcessBuilder("cmd.exe", "/c", scriptFile.getAbsolutePath());
                processBuilder.directory(workDir);
                processBuilder.redirectErrorStream(false);
            }
        } else {
            // Linux/Mac: bash로 실행
            if (!scriptFile.canExecute()) {
                log.warn("스크립트 실행 권한 없음, 권한 부여 시도: {}", scriptFile.getAbsolutePath());
                scriptFile.setExecutable(true);
            }

            processBuilder = new ProcessBuilder("/bin/bash", scriptFile.getAbsolutePath());
            processBuilder.directory(workDir);
            processBuilder.redirectErrorStream(false);
        }

        // 프로세스 시작
        return processBuilder.start();
    }

    /**
     * 출력 스트림을 비동기로 읽어서 콜백 함수로 전달
     * <p>
     * 문자 단위로 읽어서 개행 없는 프롬프트도 즉시 전달합니다.
     * </p>
     *
     * @param inputStream 입력 스트림 (stdout 또는 stderr)
     * @param callback 출력 메시지 콜백
     * @param isError stderr 여부
     */
    public void readOutputAsync(InputStream inputStream, Consumer<OutputMessage> callback, boolean isError) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                char[] buffer = new char[1024];
                int charsRead;

                while ((charsRead = reader.read(buffer, 0, buffer.length)) != -1) {
                    String data = new String(buffer, 0, charsRead);

                    OutputMessage message = OutputMessage.builder()
                            .type(isError ? "error" : "output")
                            .data(data)
                            .timestamp(LocalDateTime.now())
                            .build();

                    callback.accept(message);
                }
            } catch (IOException e) {
                log.error("출력 스트림 읽기 오류: {}", e.getMessage());
            }
        }, isError ? "stderr-reader" : "stdout-reader");

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 프로세스 종료 대기 및 콜백 호출
     *
     * @param process 프로세스
     * @param callback 종료 메시지 콜백
     */
    public void waitForProcessAsync(Process process, Consumer<OutputMessage> callback) {
        Thread thread = new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                log.info("프로세스 종료: exitCode={}", exitCode);

                OutputMessage message = OutputMessage.builder()
                        .type("exit")
                        .data("프로세스가 종료되었습니다.")
                        .exitCode(exitCode)
                        .timestamp(LocalDateTime.now())
                        .build();

                callback.accept(message);
            } catch (InterruptedException e) {
                log.error("프로세스 대기 중단: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }, "process-waiter");

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 프로세스 stdin에 입력 전송
     *
     * @param outputStream 프로세스 stdin
     * @param input 입력 데이터
     * @throws IOException 전송 실패
     */
    public void sendInput(OutputStream outputStream, String input) throws IOException {
        if (outputStream != null) {
            outputStream.write(input.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            log.debug("입력 전송 완료: {} bytes", input.length());
        }
    }

    /**
     * 프로세스 종료
     *
     * @param process 종료할 프로세스
     * @param force 강제 종료 여부
     */
    public void terminateProcess(Process process, boolean force) {
        if (process != null && process.isAlive()) {
            if (force) {
                process.destroyForcibly();
                log.info("프로세스 강제 종료 완료");
            } else {
                process.destroy();
                log.info("프로세스 정상 종료 요청 완료");
            }
        }
    }

    /**
     * 경로 검증 (Path Traversal 공격 방지)
     *
     * @param path 검증할 경로
     * @throws IllegalArgumentException 유효하지 않은 경로
     */
    private void validatePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("경로가 비어있습니다");
        }

        // ../ 패턴 차단
        if (path.contains("../") || path.contains("..\\")) {
            throw new IllegalArgumentException("유효하지 않은 경로입니다 (../ 포함)");
        }

        // 절대 경로 차단
        if (path.startsWith("/") || path.matches("^[A-Za-z]:.*")) {
            throw new IllegalArgumentException("절대 경로는 사용할 수 없습니다");
        }
    }
}
