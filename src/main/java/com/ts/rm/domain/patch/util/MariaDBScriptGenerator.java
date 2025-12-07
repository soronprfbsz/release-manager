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

    @Override
    protected String getTemplatePath() {
        return "release/script/MARIADB/mariadb_patch_template.sh";
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
     *
     * @param fromVersion      From 버전
     * @param toVersion        To 버전
     * @param versions         버전 리스트
     * @param mariadbFiles     MariaDB SQL 파일 리스트 (빈 리스트 가능)
     * @param outputDirPath    출력 디렉토리 경로
     * @param defaultPatchedBy 패치 담당자 기본값 (nullable, 프론트엔드에서 입력받은 값)
     */
    @Override
    public void generatePatchScript(
            String fromVersion,
            String toVersion,
            List<ReleaseVersion> versions,
            List<ReleaseFile> mariadbFiles,
            String outputDirPath,
            String defaultPatchedBy) {

        // 템플릿 로드
        String template = loadTemplate();

        // SQL 실행 명령어 생성 (파일이 있으면 SQL 실행 + VERSION_HISTORY, 없으면 VERSION_HISTORY만)
        String sqlCommands = mariadbFiles.isEmpty()
                ? buildVersionHistoryOnlyCommands(versions)
                : buildSqlExecutionCommands(mariadbFiles, versions);

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
     *
     * @param files    MariaDB SQL 파일 리스트
     * @param versions 전체 버전 리스트
     * @return SQL 실행 명령어
     */
    private String buildSqlExecutionCommands(List<ReleaseFile> files, List<ReleaseVersion> versions) {
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

            // VERSION_HISTORY INSERT는 항상 실행
            commands.append(buildVersionHistoryInsertCommand(version));

            commands.append(String.format("log_success \"버전 %s 패치 완료!\"\n\n", versionStr));
        }

        return commands.toString();
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
        command.append("'").append(escapeForSql(releaseVersion.getCreatedBy())).append("', ");
        command.append("'$APPLIED_BY', ");  // 스크립트 실행 시 입력받은 값 사용
        command.append(commentValue);
        command.append(") ON DUPLICATE KEY UPDATE ");
        command.append("SYSTEM_APPLIED_AT = NOW(), ");
        command.append("SYSTEM_APPLIED_BY = '$APPLIED_BY';\"\n");

        return command.toString();
    }
}
