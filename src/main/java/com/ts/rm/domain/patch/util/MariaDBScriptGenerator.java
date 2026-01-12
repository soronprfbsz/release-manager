package com.ts.rm.domain.patch.util;

import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MariaDB 패치 스크립트 생성 구현체
 *
 * <p>MariaDB SQL 파일 실행 및 VERSION_HISTORY 테이블 관리 스크립트를 생성합니다.
 */
@Slf4j
@Component("mariaDBScriptGenerator")
public class MariaDBScriptGenerator extends AbstractScriptGenerator {

    /**
     * VERSION_HISTORY INSERT가 필요한 프로젝트 ID 목록
     *
     * <p>CM_DB.VERSION_HISTORY 테이블은 infraeye1, infraeye2 프로젝트에서만 사용됩니다.
     */
    private static final List<String> VERSION_HISTORY_PROJECT_IDS = List.of("infraeye1", "infraeye2");

    @Override
    protected String getTemplatePath() {
        return "release-manager/templates/MARIADB/mariadb_patch_template.sh";
    }

    @Override
    protected String getDatabaseType() {
        return "MariaDB";
    }

    @Override
    public String getScriptFileName() {
        return "mariadb_patch.sh";
    }

    /**
     * MariaDB 패치 스크립트 생성
     *
     * <p>MariaDB SQL 파일이 없더라도 VERSION_HISTORY INSERT를 위해 항상 스크립트가 생성됩니다.
     * <p>단, VERSION_HISTORY INSERT는 infraeye1, infraeye2 프로젝트에서만 실행됩니다.
     *
     * @param projectId        프로젝트 ID
     * @param fromVersion      From 버전
     * @param toVersion        To 버전
     * @param versions         버전 리스트
     * @param mariadbFiles     MariaDB SQL 파일 리스트 (빈 리스트 가능)
     * @param outputDirPath    출력 디렉토리 경로
     * @param defaultPatchedBy 패치 담당자 기본값 (nullable, 프론트엔드에서 입력받은 값)
     */
    @Override
    public void generatePatchScript(
            String projectId,
            String fromVersion,
            String toVersion,
            List<ReleaseVersion> versions,
            List<ReleaseFile> mariadbFiles,
            String outputDirPath,
            String defaultPatchedBy) {

        // VERSION_HISTORY INSERT가 필요한 프로젝트인지 확인
        boolean includeVersionHistory = VERSION_HISTORY_PROJECT_IDS.contains(projectId);

        // 템플릿 로드
        String template = loadTemplate();

        // SQL 실행 명령어 생성
        String sqlCommands;
        if (mariadbFiles.isEmpty()) {
            if (includeVersionHistory) {
                sqlCommands = buildVersionHistoryOnlyCommands(versions);
            } else {
                // VERSION_HISTORY도 없고 SQL 파일도 없으면 빈 명령어
                sqlCommands = "log_info \"이 패치에는 MariaDB SQL 파일이 없습니다.\"\n";
            }
        } else {
            sqlCommands = buildSqlExecutionCommands(mariadbFiles, versions, includeVersionHistory);
        }

        // 패치 담당자 기본값 처리 (null 또는 빈 문자열이면 빈 문자열)
        String patchedByDefault = (defaultPatchedBy != null && !defaultPatchedBy.isBlank())
                ? defaultPatchedBy.trim()
                : "";

        // 변수 치환
        String script = template
                .replace("{{GENERATED_DATE}}", getCurrentDateTime())
                .replace("{{FROM_VERSION}}", fromVersion)
                .replace("{{TO_VERSION}}", toVersion)
                .replace("{{VERSION_COUNT}}", String.valueOf(versions.size()))
                .replace("{{VERSION_METADATA}}", buildVersionMetadata(versions))
                .replace("{{SQL_EXECUTION_COMMANDS}}", sqlCommands)
                .replace("{{DEFAULT_PATCHED_BY}}", patchedByDefault);

        // 스크립트 저장
        saveScript(script, outputDirPath);
    }

    /**
     * VERSION_HISTORY INSERT만 생성 (MariaDB SQL 파일이 없는 버전용)
     *
     * <p>WEB, ENGINE 등 DB 변경 없이 빌드 파일만 있는 버전에서도
     * VERSION_HISTORY에 버전 이력을 기록합니다.
     *
     * @param versions 버전 리스트
     * @return VERSION_HISTORY INSERT 명령어
     */
    private String buildVersionHistoryOnlyCommands(List<ReleaseVersion> versions) {
        StringBuilder commands = new StringBuilder();

        commands.append("log_info \"이 패치에는 MariaDB SQL 파일이 없습니다.\"\n");
        commands.append("log_info \"VERSION_HISTORY에 버전 이력만 기록합니다.\"\n\n");

        for (ReleaseVersion version : versions) {
            commands.append(String.format("log_step \"버전 %s 이력 기록 중...\"\n", version.getVersion()));
            commands.append(buildVersionHistoryInsertCommand(version));
            commands.append(String.format("log_success \"버전 %s 이력 기록 완료!\"\n\n", version.getVersion()));
        }

        return commands.toString();
    }

    /**
     * SQL 실행 명령어 생성 (MariaDB SQL 파일이 있는 경우)
     *
     * <p>각 버전 SQL 실행 완료 후 VERSION_HISTORY 테이블에 버전 정보를 INSERT합니다.
     * MariaDB SQL 파일이 없는 버전도 VERSION_HISTORY INSERT는 수행됩니다.
     * <p>단, VERSION_HISTORY INSERT는 infraeye1, infraeye2 프로젝트에서만 실행됩니다.
     *
     * @param files                 MariaDB SQL 파일 리스트
     * @param versions              전체 버전 리스트
     * @param includeVersionHistory VERSION_HISTORY INSERT 포함 여부
     * @return SQL 실행 명령어
     */
    private String buildSqlExecutionCommands(List<ReleaseFile> files, List<ReleaseVersion> versions,
            boolean includeVersionHistory) {
        // 버전별로 그룹화
        var filesByVersion = files.stream()
                .collect(Collectors.groupingBy(f -> f.getReleaseVersion().getVersion()));

        StringBuilder commands = new StringBuilder();

        // 모든 버전에 대해 처리 (SQL 파일 유무와 관계없이)
        for (ReleaseVersion version : versions) {
            String versionStr = version.getVersion();
            List<ReleaseFile> versionFiles = filesByVersion.get(versionStr);

            commands.append(String.format("log_step \"버전 %s 패치 적용 중...\"\n", versionStr));

            if (versionFiles != null && !versionFiles.isEmpty()) {
                // SQL 파일이 있는 경우: 디렉토리 이동 후 SQL 실행
                commands.append(String.format("cd \"%s\"\n", versionStr));

                // 실행 순서대로 정렬된 파일 실행
                versionFiles.stream()
                        .sorted((a, b) -> Integer.compare(a.getExecutionOrder(), b.getExecutionOrder()))
                        .forEach(file -> {
                            commands.append(String.format("log_info \"실행: %s\"\n", file.getFileName()));
                            commands.append(String.format("execute_sql \"%s\"\n", file.getFileName()));
                        });

                commands.append("cd ..\n");
            } else {
                // SQL 파일이 없는 경우
                commands.append("log_info \"이 버전에는 MariaDB SQL 파일이 없습니다.\"\n");
            }

            // VERSION_HISTORY INSERT는 infraeye1, infraeye2 프로젝트에서만 실행
            if (includeVersionHistory) {
                commands.append(buildVersionHistoryInsertCommand(version));
            }

            commands.append(String.format("log_success \"버전 %s 패치 완료!\"\n\n", versionStr));
        }

        return commands.toString();
    }

    /**
     * 핫픽스 패치 스크립트 생성
     *
     * <p>핫픽스는 단일 버전에 대한 패치이므로 From-To 범위가 아닌 단일 버전 스크립트를 생성합니다.
     * 기존 템플릿을 재사용하되, 핫픽스에 맞게 변수를 치환합니다.
     * <p>단, VERSION_HISTORY INSERT는 infraeye1, infraeye2 프로젝트에서만 실행됩니다.
     *
     * @param projectId        프로젝트 ID
     * @param hotfixVersion    핫픽스 버전 엔티티
     * @param mariadbFiles     MariaDB SQL 파일 리스트
     * @param outputDirPath    출력 디렉토리 경로
     * @param defaultPatchedBy 패치 담당자 기본값 (nullable)
     */
    @Override
    public void generateHotfixScript(
            String projectId,
            ReleaseVersion hotfixVersion,
            List<ReleaseFile> mariadbFiles,
            String outputDirPath,
            String defaultPatchedBy) {

        // VERSION_HISTORY INSERT가 필요한 프로젝트인지 확인
        boolean includeVersionHistory = VERSION_HISTORY_PROJECT_IDS.contains(projectId);

        // 템플릿 로드
        String template = loadTemplate();

        // 핫픽스 버전 정보
        String hotfixBaseVersion = hotfixVersion.getVersion();  // 예: 1.1.0
        String fullVersion = hotfixVersion.getFullVersion();     // 예: 1.1.0.1

        // SQL 실행 명령어 생성 (핫픽스용)
        String sqlCommands;
        if (mariadbFiles.isEmpty()) {
            if (includeVersionHistory) {
                sqlCommands = buildHotfixVersionHistoryOnlyCommands(hotfixVersion);
            } else {
                // VERSION_HISTORY도 없고 SQL 파일도 없으면 빈 명령어
                sqlCommands = "log_info \"이 핫픽스에는 MariaDB SQL 파일이 없습니다.\"\n";
            }
        } else {
            sqlCommands = buildHotfixSqlExecutionCommands(hotfixVersion, mariadbFiles, includeVersionHistory);
        }

        // 패치 담당자 기본값 처리
        String patchedByDefault = (defaultPatchedBy != null && !defaultPatchedBy.isBlank())
                ? defaultPatchedBy.trim()
                : "";

        // 변수 치환 (핫픽스용)
        String script = template
                .replace("{{GENERATED_DATE}}", getCurrentDateTime())
                .replace("{{FROM_VERSION}}", hotfixBaseVersion)
                .replace("{{TO_VERSION}}", fullVersion)
                .replace("{{VERSION_COUNT}}", "1")
                .replace("{{VERSION_METADATA}}", buildHotfixVersionMetadata(hotfixVersion))
                .replace("{{SQL_EXECUTION_COMMANDS}}", sqlCommands)
                .replace("{{DEFAULT_PATCHED_BY}}", patchedByDefault);

        // 스크립트 저장
        saveScript(script, outputDirPath);

        log.info("핫픽스 MariaDB 패치 스크립트 생성 완료: {} ({})", fullVersion, outputDirPath);
    }

    /**
     * 핫픽스 버전 메타데이터 생성
     */
    private String buildHotfixVersionMetadata(ReleaseVersion hotfixVersion) {
        return String.format("    \"%s:%s:%s:%s\"",
                hotfixVersion.getFullVersion(),
                hotfixVersion.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                hotfixVersion.getCreatedByName(),
                hotfixVersion.getComment() != null ? hotfixVersion.getComment().replace("\"", "\\\"") : "핫픽스");
    }

    /**
     * 핫픽스 VERSION_HISTORY INSERT만 생성 (SQL 파일 없는 경우)
     */
    private String buildHotfixVersionHistoryOnlyCommands(ReleaseVersion hotfixVersion) {
        StringBuilder commands = new StringBuilder();

        commands.append("log_info \"이 핫픽스에는 MariaDB SQL 파일이 없습니다.\"\n");
        commands.append("log_info \"VERSION_HISTORY에 핫픽스 이력만 기록합니다.\"\n\n");

        commands.append(String.format("log_step \"핫픽스 %s 이력 기록 중...\"\n", hotfixVersion.getFullVersion()));
        commands.append(buildHotfixVersionHistoryInsertCommand(hotfixVersion));
        commands.append(String.format("log_success \"핫픽스 %s 이력 기록 완료!\"\n\n", hotfixVersion.getFullVersion()));

        return commands.toString();
    }

    /**
     * 핫픽스 SQL 실행 명령어 생성
     *
     * @param hotfixVersion         핫픽스 버전 엔티티
     * @param files                 MariaDB SQL 파일 리스트
     * @param includeVersionHistory VERSION_HISTORY INSERT 포함 여부
     * @return SQL 실행 명령어
     */
    private String buildHotfixSqlExecutionCommands(ReleaseVersion hotfixVersion, List<ReleaseFile> files,
            boolean includeVersionHistory) {
        StringBuilder commands = new StringBuilder();

        String fullVersion = hotfixVersion.getFullVersion();

        commands.append(String.format("log_step \"핫픽스 %s 패치 적용 중...\"\n", fullVersion));

        // SQL 파일 실행 (핫픽스는 단일 디렉토리에 있으므로 디렉토리 이동 없이 실행)
        files.stream()
                .sorted((a, b) -> Integer.compare(a.getExecutionOrder(), b.getExecutionOrder()))
                .forEach(file -> {
                    commands.append(String.format("log_info \"실행: %s\"\n", file.getFileName()));
                    commands.append(String.format("execute_sql \"%s\"\n", file.getFileName()));
                });

        // VERSION_HISTORY INSERT는 infraeye1, infraeye2 프로젝트에서만 실행
        if (includeVersionHistory) {
            commands.append(buildHotfixVersionHistoryInsertCommand(hotfixVersion));
        }

        commands.append(String.format("log_success \"핫픽스 %s 패치 완료!\"\n\n", fullVersion));

        return commands.toString();
    }

    /**
     * 핫픽스 VERSION_HISTORY INSERT SQL 실행 명령어 생성
     *
     * <p>핫픽스의 경우 fullVersion (예: 1.1.0.1)을 VERSION_ID로 사용합니다.
     */
    private String buildHotfixVersionHistoryInsertCommand(ReleaseVersion hotfixVersion) {
        StringBuilder command = new StringBuilder();

        // VERSION_ID: 핫픽스는 fullVersion 사용 (예: 1.1.0.1)
        String versionId = hotfixVersion.getFullVersion();

        // CUSTOM_VERSION 처리: 핫픽스는 null
        String customVersionValue = hotfixVersion.getCustomVersion() != null
                ? "'" + escapeForSql(hotfixVersion.getCustomVersion()) + "'"
                : "NULL";

        // COMMENT 처리
        String commentValue = hotfixVersion.getComment() != null
                ? "'" + escapeForSql(hotfixVersion.getComment()) + "'"
                : "'핫픽스'";

        // VERSION_CREATED_AT 포맷
        String createdAtValue = hotfixVersion.getCreatedAt() != null
                ? "'" + hotfixVersion.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'"
                : "NOW()";

        command.append("log_info \"VERSION_HISTORY에 핫픽스 정보 기록 중...\"\n");
        command.append("execute_sql_string \"INSERT INTO CM_DB.VERSION_HISTORY ");
        command.append("(VERSION_ID, STANDARD_VERSION, CUSTOM_VERSION, VERSION_CREATED_AT, VERSION_CREATED_BY, SYSTEM_APPLIED_BY, COMMENT) ");
        command.append("VALUES (");
        command.append("'").append(escapeForSql(versionId)).append("', ");
        command.append("'").append(escapeForSql(hotfixVersion.getVersion())).append("', ");  // 베이스 버전
        command.append(customVersionValue).append(", ");
        command.append(createdAtValue).append(", ");
        command.append("'").append(escapeForSql(hotfixVersion.getCreatedByName())).append("', ");
        command.append("'$APPLIED_BY', ");
        command.append(commentValue);
        command.append(") ON DUPLICATE KEY UPDATE ");
        command.append("SYSTEM_APPLIED_AT = NOW(), ");
        command.append("SYSTEM_APPLIED_BY = '$APPLIED_BY';\"\n");

        return command.toString();
    }

    /**
     * VERSION_HISTORY INSERT SQL 실행 명령어 생성
     *
     * <p>현장에 적용된 버전 관리를 위해 CM_DB.VERSION_HISTORY 테이블에 버전 정보를 삽입합니다.
     *
     * @param releaseVersion 릴리즈 버전 정보
     * @return INSERT SQL 실행 명령어
     */
    private String buildVersionHistoryInsertCommand(ReleaseVersion releaseVersion) {
        StringBuilder command = new StringBuilder();

        // VERSION_ID 생성: 표준본은 version만, 커스텀본은 version.customVersion
        String versionId = releaseVersion.getCustomVersion() != null
                ? releaseVersion.getVersion() + "." + releaseVersion.getCustomVersion()
                : releaseVersion.getVersion();

        // CUSTOM_VERSION 처리: null이면 NULL, 값이 있으면 따옴표로 감싸기
        String customVersionValue = releaseVersion.getCustomVersion() != null
                ? "'" + escapeForSql(releaseVersion.getCustomVersion()) + "'"
                : "NULL";

        // COMMENT 처리: null이면 NULL, 값이 있으면 따옴표로 감싸기
        String commentValue = releaseVersion.getComment() != null
                ? "'" + escapeForSql(releaseVersion.getComment()) + "'"
                : "NULL";

        // VERSION_CREATED_AT 포맷
        String createdAtValue = releaseVersion.getCreatedAt() != null
                ? "'" + releaseVersion.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'"
                : "NOW()";

        command.append("log_info \"VERSION_HISTORY에 버전 정보 기록 중...\"\n");
        command.append("execute_sql_string \"INSERT INTO CM_DB.VERSION_HISTORY ");
        command.append("(VERSION_ID, STANDARD_VERSION, CUSTOM_VERSION, VERSION_CREATED_AT, VERSION_CREATED_BY, SYSTEM_APPLIED_BY, COMMENT) ");
        command.append("VALUES (");
        command.append("'").append(escapeForSql(versionId)).append("', ");
        command.append("'").append(escapeForSql(releaseVersion.getVersion())).append("', ");
        command.append(customVersionValue).append(", ");
        command.append(createdAtValue).append(", ");
        command.append("'").append(escapeForSql(releaseVersion.getCreatedByName())).append("', ");
        command.append("'$APPLIED_BY', ");  // 스크립트 실행 시 입력받은 값 사용
        command.append(commentValue);
        command.append(") ON DUPLICATE KEY UPDATE ");
        command.append("SYSTEM_APPLIED_AT = NOW(), ");
        command.append("SYSTEM_APPLIED_BY = '$APPLIED_BY';\"\n");

        return command.toString();
    }
}
