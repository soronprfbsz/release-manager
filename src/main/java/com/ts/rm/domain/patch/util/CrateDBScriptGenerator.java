package com.ts.rm.domain.patch.util;

import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CrateDB 패치 스크립트 생성 구현체
 *
 * <p>CrateDB SQL 파일 실행 스크립트를 생성합니다.
 * VERSION_HISTORY와 관계없이 SQL 파일만 실행합니다.
 */
@Slf4j
@Component("crateDBScriptGenerator")
public class CrateDBScriptGenerator extends AbstractScriptGenerator {

    @Override
    protected String getTemplatePath() {
        return "release/script/CRATEDB/cratedb_patch_template.sh";
    }

    @Override
    protected String getDatabaseType() {
        return "CrateDB";
    }

    @Override
    public String getScriptFileName() {
        return "cratedb_patch.sh";
    }

    /**
     * CrateDB 패치 스크립트 생성
     *
     * @param fromVersion      From 버전
     * @param toVersion        To 버전
     * @param versions         버전 리스트
     * @param cratedbFiles     CrateDB SQL 파일 리스트
     * @param outputDirPath    출력 디렉토리 경로
     * @param defaultPatchedBy 패치 담당자 기본값 (사용하지 않음)
     */
    @Override
    public void generatePatchScript(
            String fromVersion,
            String toVersion,
            List<ReleaseVersion> versions,
            List<ReleaseFile> cratedbFiles,
            String outputDirPath,
            String defaultPatchedBy) {

        // 템플릿 로드
        String template = loadTemplate();

        // 변수 치환
        String script = template
                .replace("{{GENERATED_DATE}}", getCurrentDateTime())
                .replace("{{FROM_VERSION}}", fromVersion)
                .replace("{{TO_VERSION}}", toVersion)
                .replace("{{VERSION_COUNT}}", String.valueOf(versions.size()))
                .replace("{{VERSION_METADATA}}", buildVersionMetadata(versions))
                .replace("{{SQL_EXECUTION_COMMANDS}}", buildCrateDBSqlExecutionCommands(cratedbFiles, versions));

        // 스크립트 저장
        saveScript(script, outputDirPath);
    }

    /**
     * CrateDB SQL 실행 명령어 생성
     *
     * <p>CrateDB는 VERSION_HISTORY와 관계없이 SQL 파일만 실행합니다.
     * 버전 순서를 보장하기 위해 versions 리스트를 순회합니다.
     *
     * @param files    CrateDB SQL 파일 리스트
     * @param versions 버전 리스트 (순서 보장)
     * @return SQL 실행 명령어
     */
    private String buildCrateDBSqlExecutionCommands(List<ReleaseFile> files, List<ReleaseVersion> versions) {
        // 버전별로 그룹화
        var filesByVersion = files.stream()
                .collect(Collectors.groupingBy(f -> f.getReleaseVersion().getVersion()));

        StringBuilder commands = new StringBuilder();

        // 버전 리스트 순서대로 실행 명령 생성 (순서 보장)
        for (ReleaseVersion version : versions) {
            String versionStr = version.getVersion();
            List<ReleaseFile> versionFiles = filesByVersion.get(versionStr);

            // 해당 버전에 CrateDB 파일이 없으면 스킵
            if (versionFiles == null || versionFiles.isEmpty()) {
                continue;
            }

            commands.append(String.format("log_step \"버전 %s 패치 적용 중...\"\n", versionStr));
            commands.append(String.format("cd \"%s\"\n", versionStr));

            // 실행 순서대로 정렬된 파일 실행
            versionFiles.stream()
                    .sorted((a, b) -> Integer.compare(a.getExecutionOrder(), b.getExecutionOrder()))
                    .forEach(file -> {
                        commands.append(String.format("log_info \"실행: %s\"\n", file.getFileName()));
                        commands.append(String.format("execute_sql \"%s\"\n", file.getFileName()));
                    });

            commands.append("cd ..\n");
            commands.append(String.format("log_success \"버전 %s 패치 완료!\"\n\n", versionStr));
        }

        return commands.toString();
    }
}
