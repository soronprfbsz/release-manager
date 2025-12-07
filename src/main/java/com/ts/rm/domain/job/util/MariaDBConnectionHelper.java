package com.ts.rm.domain.job.util;

import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MariaDB 연결 및 명령어 실행 공통 유틸리티
 *
 * <p>MariaDB 백업/복원 서비스에서 공통으로 사용하는 MariaDB 연결 테스트 및 명령어 실행 로직을 제공합니다.
 *
 * <p>사용 예시:
 * <pre>
 * // 연결 테스트
 * MariaDBConnectionHelper.testConnection("localhost", 3306, "user", "password");
 *
 * // 프로세스 실행 및 출력 읽기
 * ProcessBuilder pb = new ProcessBuilder(...);
 * Process process = pb.start();
 * String output = MariaDBConnectionHelper.readProcessOutput(process);
 * </pre>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MariaDBConnectionHelper {

    private static final String MARIADB_COMMAND = "mariadb";
    private static final String SSL_DISABLE_OPTION = "--ssl=false";

    /**
     * MariaDB 연결 테스트
     *
     * @param host     호스트
     * @param port     포트
     * @param username 사용자명
     * @param password 비밀번호
     * @throws BusinessException 연결 실패 시
     */
    public static void testConnection(String host, int port, String username, String password) {
        List<String> command = buildMariaDBCommand(host, port, username, password);
        command.add("-e");
        command.add("SELECT 1");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String error = readProcessOutput(process);
                log.error("MariaDB 연결 테스트 실패 - host: {}:{}, user: {}, error: {}",
                        host, port, username, error);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "MariaDB 연결에 실패했습니다: " + error);
            }

            log.debug("MariaDB 연결 테스트 성공 - host: {}:{}, user: {}", host, port, username);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "MariaDB 연결 테스트 중 I/O 오류가 발생했습니다: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "MariaDB 연결 테스트가 중단되었습니다.");
        }
    }

    /**
     * MariaDB 기본 명령어 빌드
     *
     * @param host     호스트
     * @param port     포트
     * @param username 사용자명
     * @param password 비밀번호
     * @return 명령어 리스트
     */
    public static List<String> buildMariaDBCommand(String host, int port, String username,
            String password) {
        List<String> command = new ArrayList<>();
        command.add(MARIADB_COMMAND);
        command.add("-h");
        command.add(host);
        command.add("-P");
        command.add(String.valueOf(port));
        command.add("-u");
        command.add(username);
        command.add("-p" + password);
        command.add(SSL_DISABLE_OPTION);
        return command;
    }

    /**
     * MariaDB Dump 명령어 빌드
     *
     * @param host     호스트
     * @param port     포트
     * @param username 사용자명
     * @param password 비밀번호
     * @param database 데이터베이스명
     * @return 명령어 리스트
     */
    public static List<String> buildMariaDBDumpCommand(String host, int port, String username,
            String password, String database) {
        List<String> command = new ArrayList<>();
        command.add("mariadb-dump");
        command.add("-h");
        command.add(host);
        command.add("-P");
        command.add(String.valueOf(port));
        command.add("-u");
        command.add(username);
        command.add("-p" + password);
        command.add(SSL_DISABLE_OPTION);
        command.add("--single-transaction");
        command.add("--routines");
        command.add("--triggers");
        command.add("--events");
        command.add("--skip-add-locks");
        command.add("--databases");
        command.add(database);
        return command;
    }

    /**
     * 프로세스 출력 읽기 (표준 출력 + 표준 에러)
     *
     * @param process 실행 중인 프로세스
     * @return 프로세스 출력 문자열
     * @throws IOException I/O 오류 발생 시
     */
    public static String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();

        // 표준 출력 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // 표준 에러 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        return output.toString().trim();
    }

    /**
     * 프로세스 표준 출력만 읽기
     *
     * @param process 실행 중인 프로세스
     * @return 표준 출력 문자열
     * @throws IOException I/O 오류 발생 시
     */
    public static String readProcessStdout(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }

    /**
     * 프로세스 표준 에러만 읽기
     *
     * @param process 실행 중인 프로세스
     * @return 표준 에러 문자열
     * @throws IOException I/O 오류 발생 시
     */
    public static String readProcessStderr(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString().trim();
    }
}
