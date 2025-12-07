package com.ts.rm.domain.job.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * MariaDBConnectionHelper 테스트
 */
class MariaDBConnectionHelperTest {

    @Test
    @DisplayName("MariaDB 기본 명령어 빌드")
    void buildMariaDBCommand() {
        // given
        String host = "localhost";
        int port = 3306;
        String username = "testuser";
        String password = "testpass";

        // when
        List<String> command = MariaDBConnectionHelper.buildMariaDBCommand(
                host, port, username, password);

        // then
        assertThat(command).containsExactly(
                "mariadb",
                "-h", "localhost",
                "-P", "3306",
                "-u", "testuser",
                "-ptestpass",
                "--ssl=false"
        );
    }

    @Test
    @DisplayName("MariaDB Dump 명령어 빌드")
    void buildMariaDBDumpCommand() {
        // given
        String host = "db.example.com";
        int port = 13306;
        String username = "admin";
        String password = "secure123";
        String database = "mydb";

        // when
        List<String> command = MariaDBConnectionHelper.buildMariaDBDumpCommand(
                host, port, username, password, database);

        // then
        assertThat(command).containsExactly(
                "mariadb-dump",
                "-h", "db.example.com",
                "-P", "13306",
                "-u", "admin",
                "-psecure123",
                "--ssl=false",
                "--single-transaction",
                "--routines",
                "--triggers",
                "--events",
                "--skip-add-locks",
                "--databases",
                "mydb"
        );
    }

    @Test
    @DisplayName("MariaDB 명령어 빌드 - 다양한 포트")
    void buildMariaDBCommand_variousPorts() {
        // given
        String host = "127.0.0.1";
        int port = 33060;
        String username = "root";
        String password = "rootpass";

        // when
        List<String> command = MariaDBConnectionHelper.buildMariaDBCommand(
                host, port, username, password);

        // then
        assertThat(command)
                .contains("-P", "33060")
                .contains("-h", "127.0.0.1");
    }

    @Test
    @DisplayName("MariaDB 명령어 빌드 - 특수문자 포함 비밀번호")
    void buildMariaDBCommand_specialCharPassword() {
        // given
        String host = "localhost";
        int port = 3306;
        String username = "user";
        String password = "p@ss!w0rd#123";

        // when
        List<String> command = MariaDBConnectionHelper.buildMariaDBCommand(
                host, port, username, password);

        // then
        assertThat(command).contains("-pp@ss!w0rd#123");
    }

    @Test
    @DisplayName("MariaDB Dump 명령어 - 필수 옵션 포함 확인")
    void buildMariaDBDumpCommand_requiredOptions() {
        // given
        String host = "localhost";
        int port = 3306;
        String username = "user";
        String password = "pass";
        String database = "testdb";

        // when
        List<String> command = MariaDBConnectionHelper.buildMariaDBDumpCommand(
                host, port, username, password, database);

        // then
        assertThat(command)
                .contains("--single-transaction")
                .contains("--routines")
                .contains("--triggers")
                .contains("--events")
                .contains("--skip-add-locks")
                .contains("--databases");
    }

    @Test
    @DisplayName("MariaDB Dump 명령어 - SSL 비활성화 확인")
    void buildMariaDBDumpCommand_sslDisabled() {
        // given
        String host = "localhost";
        int port = 3306;
        String username = "user";
        String password = "pass";
        String database = "testdb";

        // when
        List<String> command = MariaDBConnectionHelper.buildMariaDBDumpCommand(
                host, port, username, password, database);

        // then
        assertThat(command).contains("--ssl=false");
    }

    @Test
    @DisplayName("MariaDB Dump 명령어 - 데이터베이스명 정확성")
    void buildMariaDBDumpCommand_databaseName() {
        // given
        String database = "production_db";

        // when
        List<String> command = MariaDBConnectionHelper.buildMariaDBDumpCommand(
                "localhost", 3306, "user", "pass", database);

        // then
        // --databases 옵션 다음에 데이터베이스명이 와야 함
        int databasesIndex = command.indexOf("--databases");
        assertThat(command.get(databasesIndex + 1)).isEqualTo("production_db");
    }
}
