package com.ts.rm.domain.patch.util;

import com.ts.rm.domain.release.entity.ReleaseFile;
import com.ts.rm.domain.release.entity.ReleaseVersion;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 패치 실행 스크립트 생성 유틸리티
 *
 * <p>누적 패치 생성시 MariaDB, CrateDB용 실행 스크립트를 자동 생성
 */
@Slf4j
@Component
public class ScriptGenerator {

    private final String baseReleasePath;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss");

    public ScriptGenerator(
            @Value("${app.release.base-path:src/main/resources/release}") String basePath) {
        this.baseReleasePath = basePath;
    }

    /**
     * MariaDB 패치 스크립트 생성
     *
     * @param fromVersion   From 버전
     * @param toVersion     To 버전
     * @param versions      버전 리스트
     * @param mariadbFiles  MariaDB SQL 파일 리스트
     * @param outputDirPath 출력 디렉토리 경로
     */
    public void generateMariaDBPatchScript(
            String fromVersion,
            String toVersion,
            List<ReleaseVersion> versions,
            List<ReleaseFile> mariadbFiles,
            String outputDirPath) {

        try {
            // 템플릿 로드
            ClassPathResource templateResource = new ClassPathResource(
                    "templates/mariadb_patch_template.sh");
            String template = Files.readString(Path.of(templateResource.getURI()));

            // 변수 치환
            String script = template
                    .replace("{{GENERATED_DATE}}", LocalDateTime.now().format(DATE_FORMATTER))
                    .replace("{{FROM_VERSION}}", fromVersion)
                    .replace("{{TO_VERSION}}", toVersion)
                    .replace("{{VERSION_COUNT}}", String.valueOf(versions.size()))
                    .replace("{{VERSION_METADATA}}", buildVersionMetadata(versions))
                    .replace("{{SQL_EXECUTION_COMMANDS}}", buildSqlExecutionCommands(mariadbFiles))
                    .replace("{{VERSION_HISTORY_INSERTS}}",
                            buildVersionHistoryInserts(versions));

            // 스크립트 파일 저장
            Path scriptPath = Paths.get(baseReleasePath, outputDirPath, "mariadb_patch.sh");
            Files.createDirectories(scriptPath.getParent());
            Files.writeString(scriptPath, script);

            // 실행 권한 부여 (Linux/Mac에서만 작동)
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                scriptPath.toFile().setExecutable(true);
            }

            log.info("MariaDB 패치 스크립트 생성 완료: {}", scriptPath);

        } catch (IOException e) {
            log.error("MariaDB 패치 스크립트 생성 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "스크립트 생성 실패: " + e.getMessage());
        }
    }

    /**
     * CrateDB 패치 스크립트 생성
     *
     * @param fromVersion   From 버전
     * @param toVersion     To 버전
     * @param versions      버전 리스트
     * @param cratedbFiles  CrateDB SQL 파일 리스트
     * @param outputDirPath 출력 디렉토리 경로
     */
    public void generateCrateDBPatchScript(
            String fromVersion,
            String toVersion,
            List<ReleaseVersion> versions,
            List<ReleaseFile> cratedbFiles,
            String outputDirPath) {

        try {
            // 템플릿 로드
            ClassPathResource templateResource = new ClassPathResource(
                    "templates/cratedb_patch_template.sh");
            String template = Files.readString(Path.of(templateResource.getURI()));

            // 변수 치환
            String script = template
                    .replace("{{GENERATED_DATE}}", LocalDateTime.now().format(DATE_FORMATTER))
                    .replace("{{FROM_VERSION}}", fromVersion)
                    .replace("{{TO_VERSION}}", toVersion)
                    .replace("{{VERSION_COUNT}}", String.valueOf(versions.size()))
                    .replace("{{VERSION_METADATA}}", buildVersionMetadata(versions))
                    .replace("{{SQL_EXECUTION_COMMANDS}}", buildSqlExecutionCommands(cratedbFiles));

            // 스크립트 파일 저장
            Path scriptPath = Paths.get(baseReleasePath, outputDirPath, "cratedb_patch.sh");
            Files.createDirectories(scriptPath.getParent());
            Files.writeString(scriptPath, script);

            // 실행 권한 부여 (Linux/Mac에서만 작동)
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                scriptPath.toFile().setExecutable(true);
            }

            log.info("CrateDB 패치 스크립트 생성 완료: {}", scriptPath);

        } catch (IOException e) {
            log.error("CrateDB 패치 스크립트 생성 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "스크립트 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 버전 메타데이터 배열 생성
     *
     * <pre>
     * "1.1.0:2025-11-05:jhlee:초기 버전"
     * "1.1.1:2025-11-10:jhlee:버그 수정"
     * </pre>
     */
    private String buildVersionMetadata(List<ReleaseVersion> versions) {
        return versions.stream()
                .map(v -> String.format("    \"%s:%s:%s:%s\"",
                        v.getVersion(),
                        v.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        v.getCreatedBy(),
                        v.getComment() != null ? v.getComment().replace("\"", "\\\"") : ""))
                .collect(Collectors.joining("\n"));
    }

    /**
     * SQL 실행 명령어 생성
     *
     * <pre>
     * for version_dir in 1.1.0 1.1.1 1.1.2; do
     *     log_step "버전 $version_dir 패치 적용 중..."
     *     cd "$version_dir"
     *     for sql_file in *.sql; do
     *         log_info "실행: $sql_file"
     *         execute_sql "$sql_file"
     *     done
     *     cd ..
     * done
     * </pre>
     */
    private String buildSqlExecutionCommands(List<ReleaseFile> files) {
        // 버전별로 그룹화
        var filesByVersion = files.stream()
                .collect(Collectors.groupingBy(f -> f.getReleaseVersion().getVersion()));

        StringBuilder commands = new StringBuilder();

        // 버전별로 실행 명령 생성
        filesByVersion.forEach((version, versionFiles) -> {
            commands.append(String.format("log_step \"버전 %s 패치 적용 중...\"\n", version));
            commands.append(String.format("cd \"%s\"\n", version));

            // 실행 순서대로 정렬된 파일 실행
            versionFiles.stream()
                    .sorted((a, b) -> Integer.compare(a.getExecutionOrder(), b.getExecutionOrder()))
                    .forEach(file -> {
                        commands.append(String.format("log_info \"실행: %s\"\n", file.getFileName()));
                        commands.append(String.format("execute_sql \"%s\"\n", file.getFileName()));
                    });

            commands.append("cd ..\n");
            commands.append(String.format("log_success \"버전 %s 패치 완료!\"\n\n", version));
        });

        return commands.toString();
    }

    /**
     * VERSION_HISTORY INSERT 문 생성
     *
     * <pre>
     * INSERT INTO version_history (version_id, standard_version, custom_version, ...)
     * VALUES ('1.1.0', '1.1.0', NULL, ...)
     * ON DUPLICATE KEY UPDATE system_applied_at = NOW();
     * </pre>
     */
    private String buildVersionHistoryInserts(List<ReleaseVersion> versions) {
        StringBuilder inserts = new StringBuilder();

        for (ReleaseVersion version : versions) {
            String insertSql = String.format(
                    "INSERT INTO version_history (version_id, standard_version, custom_version, "
                            +
                            "version_created_at, version_created_by, system_applied_by, system_applied_at, comment) "
                            +
                            "VALUES ('%s', '%s', %s, '%s', '%s', 'patch_script', NOW(), %s) "
                            +
                            "ON DUPLICATE KEY UPDATE system_applied_at = NOW();",
                    version.getVersion(),
                    version.getVersion(),
                    version.getCustomVersion() != null ? "'" + version.getCustomVersion() + "'"
                            : "NULL",
                    version.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    version.getCreatedBy(),
                    version.getComment() != null ? "'" + version.getComment().replace("'", "\\'") + "'"
                            : "NULL"
            );

            inserts.append(String.format("execute_sql_string \"%s\"\n", insertSql));
            inserts.append(String.format("log_info \"VERSION_HISTORY 업데이트: %s\"\n",
                    version.getVersion()));
        }

        return inserts.toString();
    }
}
