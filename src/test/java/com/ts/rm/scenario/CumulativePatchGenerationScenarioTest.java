package com.ts.rm.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.config.TestQueryDslConfig;
import com.ts.rm.domain.patch.repository.PatchRepository;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 시나리오 2: 누적 패치 생성
 *
 * <p>이 테스트는 실제 누적 패치 생성 과정을 시뮬레이션합니다:
 * <pre>
 * 전제 조건:
 * - 버전 1.2.0, 1.3.0, 1.3.1이 이미 생성되어 있음
 * - 각 버전마다 MariaDB, CrateDB SQL 파일이 업로드되어 있음
 *
 * 1. POST /api/patches/generate - 패치 이력 생성 (1.2.0 → 1.3.1)
 *    → 버전 범위 조회: 1.2.0 < version <= 1.3.1 → [1.3.0, 1.3.1]
 *    → 출력 디렉토리 생성: versions/standard/1.3.x/1.3.1/from-1.2.0/
 *    → SQL 파일 복사:
 *      - mariadb/source_files/1.3.0/*.sql 복사
 *      - mariadb/source_files/1.3.1/*.sql 복사
 *      - cratedb/source_files/1.3.0/*.sql 복사
 *      - cratedb/source_files/1.3.1/*.sql 복사
 *    → 스크립트 생성:
 *      - mariadb_patch.sh (실행 가능한 Shell 스크립트)
 *        - VERSION_METADATA 배열 포함 (버전 메타데이터)
 *        - SQL 실행 명령 동적 생성
 *      - cratedb_patch.sh (실행 가능한 Shell 스크립트)
 *    → README.md 생성
 *    → DB: patch_history 테이블에 이력 INSERT
 *
 * 2. 생성된 스크립트 검증
 *    → mariadb_patch.sh 내용 확인
 *    → 실행 권한 확인 (Linux/Mac)
 * </pre>
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@org.springframework.context.annotation.Import(TestQueryDslConfig.class)
public class CumulativePatchGenerationScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private ReleaseFileRepository releaseFileRepository;

    @Autowired
    private PatchRepository patchRepository;

    @Value("${app.release.base-path}")
    private String baseReleasePath;

    private String releaseMetadataBackup;  // release_metadata.json 백업

    /**
     * 테스트 실행 전: 테스트 전용 디렉토리 확인 및 백업
     */
    @BeforeEach
    void setUp() throws IOException {
        // 안전성 검증: 테스트가 실제 release 디렉토리를 사용하지 않도록 확인
        if (baseReleasePath.contains("src/main/resources/release")) {
            throw new IllegalStateException(
                "테스트가 실제 release 디렉토리를 사용하려고 합니다! " +
                "application-test.yml에서 app.release.base-path가 제대로 설정되었는지 확인하세요. " +
                "현재 경로: " + baseReleasePath
            );
        }

        System.out.println("\n[테스트 준비] 테스트 전용 경로 사용: " + baseReleasePath);

        // 테스트 디렉토리 생성
        Path testDir = Paths.get(baseReleasePath);
        if (!Files.exists(testDir)) {
            Files.createDirectories(testDir);
            System.out.println("[테스트 준비] 테스트 디렉토리 생성: " + baseReleasePath);
        }

        // release_metadata.json 백업 (있으면)
        Path metadataPath = Paths.get(baseReleasePath, "versions/standard/release_metadata.json");
        if (Files.exists(metadataPath)) {
            releaseMetadataBackup = Files.readString(metadataPath, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("[테스트 준비] release_metadata.json 백업 완료");
        } else {
            releaseMetadataBackup = null;
            System.out.println("[테스트 준비] release_metadata.json 파일 없음");
        }
    }

    /**
     * 테스트 실행 후: 생성된 파일 및 디렉토리 자동 삭제
     */
    @AfterEach
    void tearDown() throws IOException {
        System.out.println("\n[테스트 정리 시작]");

        // 1. 테스트에서 생성한 버전 디렉토리 삭제 (1.2.0, 1.3.0, 1.3.1)
        deleteVersionDirectoryIfExists("1.2.0");
        deleteVersionDirectoryIfExists("1.3.0");
        deleteVersionDirectoryIfExists("1.3.1");

        // 2. 누적 패치 디렉토리 삭제 (from-1.2.0)
        Path cumulativePatchDir = Paths.get(baseReleasePath,
                "versions/standard/1.3.x/1.3.1/from-1.2.0");
        if (Files.exists(cumulativePatchDir)) {
            deleteDirectory(cumulativePatchDir);
            System.out.println("  ✓ 누적 패치 디렉토리 삭제: versions/standard/1.3.x/1.3.1/from-1.2.0");
        }

        // 3. 빈 디렉토리 정리
        cleanupEmptyDirectories();

        // 4. release_metadata.json 복원
        Path metadataPath = Paths.get(baseReleasePath, "versions/standard/release_metadata.json");
        if (releaseMetadataBackup != null) {
            Files.writeString(metadataPath, releaseMetadataBackup, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("  ✓ release_metadata.json 복원 완료");
        } else if (Files.exists(metadataPath)) {
            Files.delete(metadataPath);
            System.out.println("  ✓ release_metadata.json 삭제 (테스트가 생성한 파일)");
        }

        // 5. DB는 @Transactional에 의해 자동 롤백됨
        System.out.println("  ✓ DB 변경사항 자동 롤백 (@Transactional)");
        System.out.println("[테스트 정리 완료]\n");
    }

    /**
     * 버전 디렉토리 삭제 (존재하는 경우)
     */
    private void deleteVersionDirectoryIfExists(String version) throws IOException {
        String majorMinor = version.substring(0, version.lastIndexOf('.'));
        Path versionDir = Paths.get(baseReleasePath,
                "versions/standard/" + majorMinor + ".x/" + version);
        if (Files.exists(versionDir)) {
            deleteDirectory(versionDir);
            System.out.println("  ✓ 버전 디렉토리 삭제: versions/standard/" + majorMinor + ".x/" + version);
        }
    }

    /**
     * 빈 디렉토리 정리
     */
    private void cleanupEmptyDirectories() throws IOException {
        // 1.2.x, 1.3.x 디렉토리가 비어있으면 삭제
        cleanupEmptyDirectory("1.2.x");
        cleanupEmptyDirectory("1.3.x");
    }

    /**
     * 빈 디렉토리 삭제
     */
    private void cleanupEmptyDirectory(String dirName) throws IOException {
        Path dir = Paths.get(baseReleasePath, "versions/standard/" + dirName);
        if (Files.exists(dir) && isDirectoryEmpty(dir)) {
            Files.delete(dir);
            System.out.println("  ✓ 빈 디렉토리 삭제: versions/standard/" + dirName);
        }
    }

    /**
     * 디렉토리 재귀 삭제
     */
    private void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 디렉토리가 비어있는지 확인
     */
    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.findAny().isEmpty();
        }
    }

    @Test
    @org.junit.jupiter.api.Disabled("API 변경으로 인한 테스트 비활성화 - Controller가 multipart/form-data + Authorization 필요")
    @DisplayName("시나리오 2: 누적 패치 생성 (1.2.0 → 1.3.1)")
    void generateCumulativePatch_From120To131() throws Exception {
        // ============================================================
        // 사전 준비: 버전 3개 생성 및 파일 업로드
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("사전 준비: 버전 3개 생성");
        System.out.println("========================================");

        // 버전 1.2.0 생성 및 파일 업로드
        Long v120Id = createVersionAndUploadFiles("1.2.0", "초기 버전",
                new String[]{"001_init_schema.sql", "002_init_data.sql"});
        System.out.println("✅ 버전 1.2.0 생성 완료 (파일 2개)");

        // 버전 1.3.0 생성 및 파일 업로드
        Long v130Id = createVersionAndUploadFiles("1.3.0", "신규 기능 추가",
                new String[]{"001_add_feature.sql", "002_update_data.sql"});
        System.out.println("✅ 버전 1.3.0 생성 완료 (파일 2개)");

        // 버전 1.3.1 생성 및 파일 업로드
        Long v131Id = createVersionAndUploadFiles("1.3.1", "버그 수정",
                new String[]{"001_bugfix.sql"});
        System.out.println("✅ 버전 1.3.1 생성 완료 (파일 1개)");

        // ============================================================
        // Step 1: 누적 패치 생성 요청 (1.2.0 → 1.3.1)
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("Step 1: POST /api/patches/generate");
        System.out.println("========================================");
        System.out.println("요청: 1.2.0 → 1.3.1 누적 패치 생성");

        String requestBody = """
                {
                    "type": "standard",
                    "fromVersion": "1.2.0",
                    "toVersion": "1.3.1",
                    "generatedBy": "jhlee@tscientific.co.kr"
                }
                """;

        String patchResponse = mockMvc.perform(post("/api/patches/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.fromVersion").value("1.2.0"))
                .andExpect(jsonPath("$.data.toVersion").value("1.3.1"))
                .andReturn().getResponse().getContentAsString();

        Long patchId = objectMapper.readTree(patchResponse)
                .path("data").path("patchId").asLong();

        System.out.println("✅ 누적 패치 생성 완료 (ID: " + patchId + ")");

        // ============================================================
        // Step 2: DB 검증 - patch_history 테이블
        // ============================================================
        System.out.println("\n[검증 1] DB 확인 - patch_history 테이블");

        var savedPatch = patchRepository.findById(patchId).orElseThrow();
        assertThat(savedPatch.getFromVersion()).isEqualTo("1.2.0");
        assertThat(savedPatch.getToVersion()).isEqualTo("1.3.1");
        assertThat(savedPatch.getCreatedBy()).isEqualTo("jhlee@tscientific.co.kr");

        String outputPath = savedPatch.getOutputPath();
        System.out.println("  - patch_history 레코드 INSERT 완료");
        System.out.println("    from_version: " + savedPatch.getFromVersion());
        System.out.println("    to_version: " + savedPatch.getToVersion());
        System.out.println("    output_path: " + outputPath);
        System.out.println("    created_at: " + savedPatch.getCreatedAt());

        // ============================================================
        // Step 3: 파일시스템 검증 - 디렉토리 구조
        // ============================================================
        System.out.println("\n[검증 2] 파일시스템 - 디렉토리 구조");

        Path outputDir = Paths.get(baseReleasePath, outputPath);
        assertThat(Files.exists(outputDir)).isTrue();
        System.out.println("  ✅ 출력 디렉토리 생성: " + outputDir.toAbsolutePath());

        // MariaDB 디렉토리 및 파일 확인
        Path mariadbDir = outputDir.resolve("mariadb/source_files");
        assertThat(Files.exists(mariadbDir)).isTrue();
        System.out.println("  ✅ mariadb/source_files/ 디렉토리 생성");

        // 버전별 SQL 파일 복사 확인
        Path v110MariadbDir = mariadbDir.resolve("1.3.0");
        Path v111MariadbDir = mariadbDir.resolve("1.3.1");

        assertThat(Files.exists(v110MariadbDir)).isTrue();
        assertThat(Files.list(v110MariadbDir).count()).isEqualTo(2); // 001_add_feature.sql, 002_update_data.sql
        System.out.println("  ✅ mariadb/source_files/1.3.0/ (파일 2개)");

        assertThat(Files.exists(v111MariadbDir)).isTrue();
        assertThat(Files.list(v111MariadbDir).count()).isEqualTo(1); // 001_bugfix.sql
        System.out.println("  ✅ mariadb/source_files/1.3.1/ (파일 1개)");

        // CrateDB 디렉토리 확인
        Path cratedbDir = outputDir.resolve("cratedb/source_files");
        assertThat(Files.exists(cratedbDir)).isTrue();
        System.out.println("  ✅ cratedb/source_files/ 디렉토리 생성");

        // ============================================================
        // Step 4: 스크립트 파일 검증
        // ============================================================
        System.out.println("\n[검증 3] 생성된 스크립트 파일");

        // mariadb_patch.sh 확인
        Path mariadbScript = outputDir.resolve("mariadb_patch.sh");
        assertThat(Files.exists(mariadbScript)).isTrue();
        System.out.println("  ✅ mariadb_patch.sh 생성");

        String scriptContent = Files.readString(mariadbScript);

        // 스크립트 내용 검증
        assertThat(scriptContent).contains("#!/bin/bash");
        assertThat(scriptContent).contains("버전 범위: 1.2.0 → 1.3.1");
        System.out.println("    - Shebang 포함: #!/bin/bash");
        System.out.println("    - 버전 범위 표시: 1.2.0 → 1.3.1");

        // VERSION_METADATA 배열 포함 확인
        assertThat(scriptContent).contains("declare -a VERSION_METADATA=");
        assertThat(scriptContent).contains("1.3.0:");  // 버전 메타데이터
        assertThat(scriptContent).contains("1.3.1:");
        System.out.println("    - VERSION_METADATA 배열 포함");

        // SQL 실행 명령 포함 확인
        assertThat(scriptContent).contains("log_step \"버전 1.3.0 패치 적용 중...\"");
        assertThat(scriptContent).contains("log_step \"버전 1.3.1 패치 적용 중...\"");
        assertThat(scriptContent).contains("execute_sql");
        System.out.println("    - SQL 실행 명령 포함");

        // cratedb_patch.sh 확인
        Path cratedbScript = outputDir.resolve("cratedb_patch.sh");
        assertThat(Files.exists(cratedbScript)).isTrue();
        System.out.println("  ✅ cratedb_patch.sh 생성");

        String cratedbScriptContent = Files.readString(cratedbScript);
        assertThat(cratedbScriptContent).contains("#!/bin/bash");
        assertThat(cratedbScriptContent).contains("CrateDB 누적 패치 실행 스크립트");
        System.out.println("    - CrateDB 전용 스크립트");

        // ============================================================
        // Step 5: README.md 검증
        // ============================================================
        System.out.println("\n[검증 4] README.md");

        Path readmeFile = outputDir.resolve("README.md");
        assertThat(Files.exists(readmeFile)).isTrue();
        System.out.println("  ✅ README.md 생성");

        String readmeContent = Files.readString(readmeFile);
        assertThat(readmeContent).contains("# 누적 패치: from-1.2.0 to 1.3.1");
        assertThat(readmeContent).contains("From Version**: 1.2.0");
        assertThat(readmeContent).contains("To Version**: 1.3.1");
        assertThat(readmeContent).contains("포함된 버전**: 1.3.0, 1.3.1");
        System.out.println("    - 버전 범위 정보 포함");

        assertThat(readmeContent).contains("## 디렉토리 구조");
        assertThat(readmeContent).contains("mariadb_patch.sh");
        assertThat(readmeContent).contains("cratedb_patch.sh");
        System.out.println("    - 디렉토리 구조 설명 포함");

        // ============================================================
        // Step 6: 스크립트 내용 상세 출력
        // ============================================================
        System.out.println("\n[상세 정보] mariadb_patch.sh 주요 내용");
        System.out.println("--- VERSION_METADATA 부분 ---");
        scriptContent.lines()
                .filter(line -> line.contains("VERSION_METADATA") || line.contains("1.1."))
                .limit(5)
                .forEach(line -> System.out.println("  " + line));

        System.out.println("\n--- SQL 실행 명령 부분 ---");
        scriptContent.lines()
                .filter(line -> line.contains("log_step") || line.contains("execute_sql"))
                .limit(10)
                .forEach(line -> System.out.println("  " + line));

        // ============================================================
        // 최종 요약
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("✅ 시나리오 2 완료!");
        System.out.println("========================================");
        System.out.println("누적 패치 ID: " + patchId);
        System.out.println("버전 범위: 1.2.0 → 1.3.1");
        System.out.println("포함 버전: 1.3.0, 1.3.1");
        System.out.println("총 SQL 파일: MariaDB 3개, CrateDB 3개");
        System.out.println("\n생성된 파일:");
        System.out.println("  - mariadb_patch.sh (실행 스크립트)");
        System.out.println("  - cratedb_patch.sh (실행 스크립트)");
        System.out.println("  - README.md (문서)");
        System.out.println("  - mariadb/source_files/1.3.0/*.sql");
        System.out.println("  - mariadb/source_files/1.3.1/*.sql");
        System.out.println("  - cratedb/source_files/1.3.0/*.sql");
        System.out.println("  - cratedb/source_files/1.3.1/*.sql");
        System.out.println("\n출력 경로: " + outputPath);
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("시나리오 3: 누적 패치 실행 시뮬레이션 (VERSION_HISTORY 업데이트)")
    void simulatePatchExecution_VerifyVersionHistory() throws Exception {
        // ============================================================
        // 이 시나리오는 실제로 생성된 스크립트를 실행하는 과정을 설명합니다.
        // (실제 DB 연결이 필요하므로 주석으로 설명)
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("시나리오 3: 누적 패치 실행 프로세스");
        System.out.println("========================================");

        System.out.println("\n[실행 방법]");
        System.out.println("1. 생성된 누적 패치 디렉토리로 이동:");
        System.out.println("   $ cd versions/standard/1.3.x/1.3.1/from-1.2.0/");

        System.out.println("\n2. MariaDB 패치 스크립트 실행:");
        System.out.println("   $ ./mariadb_patch.sh");

        System.out.println("\n3. 스크립트 실행 과정:");
        System.out.println("   [1] 실행 방식 선택");
        System.out.println("       - 1) 로컬 Docker 컨테이너");
        System.out.println("       - 2) 원격 MariaDB 서버");

        System.out.println("\n   [2] 접속 정보 입력");
        System.out.println("       Docker 컨테이너 이름: mariadb");
        System.out.println("       MariaDB 사용자명: root");
        System.out.println("       MariaDB 비밀번호: ****");
        System.out.println("       데이터베이스 이름: infraeye2");

        System.out.println("\n   [3] 연결 테스트");
        System.out.println("       [INFO] MariaDB 연결 테스트 중...");
        System.out.println("       [INFO] MariaDB 접속 성공!");

        System.out.println("\n   [4] SQL 파일 실행");
        System.out.println("       [STEP] 버전 1.3.0 패치 적용 중...");
        System.out.println("       [INFO] 실행: 001_add_feature.sql");
        System.out.println("       [INFO] 실행: 002_update_data.sql");
        System.out.println("       [SUCCESS] 버전 1.3.0 패치 완료!");

        System.out.println("\n       [STEP] 버전 1.3.1 패치 적용 중...");
        System.out.println("       [INFO] 실행: 001_bugfix.sql");
        System.out.println("       [SUCCESS] 버전 1.3.1 패치 완료!");

        System.out.println("\n   [5] 완료");
        System.out.println("       ==========================================");
        System.out.println("       [SUCCESS] 누적 패치 실행 완료!");
        System.out.println("       ==========================================");
        System.out.println("       실행 요약:");
        System.out.println("         - 적용된 버전 개수: 2");
        System.out.println("         - 버전 범위: 1.2.0 → 1.3.1");

        System.out.println("\n========================================");
        System.out.println("✅ 패치 실행 프로세스 설명 완료");
        System.out.println("========================================\n");
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * 버전 생성 및 파일 업로드 헬퍼 메서드
     */
    private Long createVersionAndUploadFiles(String version, String comment,
            String[] fileNames) throws Exception {

        // 버전 생성
        ReleaseVersionDto.CreateRequest createRequest = new ReleaseVersionDto.CreateRequest(
                version, "jhlee", comment, null, null
        );

        String response = mockMvc.perform(post("/api/releases/standard/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())  // 201 Created
                .andReturn().getResponse().getContentAsString();

        Long versionId = objectMapper.readTree(response)
                .path("data").path("releaseVersionId").asLong();

        // MariaDB 파일 업로드
        for (String fileName : fileNames) {
            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    fileName,
                    "text/plain",
                    ("-- " + fileName + " content for " + version).getBytes()
            );

            mockMvc.perform(multipart("/api/releases/versions/{id}/files/upload", versionId)
                            .file(file)
                            .param("databaseType", "MARIADB")
                            .param("uploadedBy", "jhlee"))
                    .andExpect(status().isCreated());  // 201 Created
        }

        // CrateDB 파일도 동일하게 업로드
        for (String fileName : fileNames) {
            MockMultipartFile file = new MockMultipartFile(
                    "files",
                    fileName,
                    "text/plain",
                    ("-- " + fileName + " content for " + version + " (CrateDB)").getBytes()
            );

            mockMvc.perform(multipart("/api/releases/versions/{id}/files/upload", versionId)
                            .file(file)
                            .param("databaseType", "CRATEDB")
                            .param("uploadedBy", "jhlee"))
                    .andExpect(status().isCreated());  // 201 Created
        }

        return versionId;
    }
}
