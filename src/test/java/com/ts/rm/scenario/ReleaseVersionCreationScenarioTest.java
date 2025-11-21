package com.ts.rm.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.config.TestQueryDslConfig;
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
 * 시나리오 1: 표준 릴리즈 버전 생성 및 파일 업로드
 *
 * <p>이 테스트는 실제 릴리즈 버전 생성 과정을 시뮬레이션합니다:
 * <pre>
 * 1. POST /api/release-versions/standard - 버전 1.9.9 생성 (테스트 전용)
 *    → DB: release_version 테이블에 레코드 INSERT
 *    → 파일시스템: releases/standard/1.9.x/1.9.9/patch/mariadb/ 디렉토리 생성
 *    → 파일시스템: releases/standard/1.9.x/1.9.9/patch/cratedb/ 디렉토리 생성
 *    → 파일시스템: releases/standard/patch_note.md 업데이트 (최신 버전이 맨 위에)
 *
 * 2. POST /api/release-files/versions/{versionId}/files/upload - MariaDB SQL 파일 업로드
 *    → DB: release_file 테이블에 파일 메타데이터 INSERT (파일명, 경로, 크기, 체크섬, 실행순서)
 *    → 파일시스템: releases/standard/1.9.x/1.9.9/patch/mariadb/001_create_table.sql 저장
 *
 * 3. POST /api/release-files/versions/{versionId}/files/upload - CrateDB SQL 파일 업로드
 *    → DB: release_file 테이블에 파일 메타데이터 INSERT
 *    → 파일시스템: releases/standard/1.9.x/1.9.9/patch/cratedb/001_create_table.sql 저장
 *
 * 4. GET /api/release-files/version/{versionId} - 업로드된 파일 목록 조회
 *    → DB: release_file 테이블 조회 (실행순서대로 정렬)
 *
 * ⚠️ 테스트 격리:
 * - @Transactional: DB 변경사항은 테스트 종료 후 자동 롤백
 * - @AfterEach: 파일시스템 변경사항은 테스트 종료 후 자동 삭제
 * </pre>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.springframework.context.annotation.Import(TestQueryDslConfig.class)  // ← DB 변경사항 자동 롤백
public class ReleaseVersionCreationScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private ReleaseFileRepository releaseFileRepository;

    @Value("${app.release.base-path:src/main/resources/release}")
    private String baseReleasePath;

    private Path testBaseDir;
    private String patchNoteBackup;  // patch_note.md 백업
    private Path versionDir;  // 테스트에서 생성한 버전 디렉토리

    /**
     * 테스트 실행 전: 백업 및 임시 디렉토리 생성
     */
    @BeforeEach
    void setUp() throws IOException {
        // 1. patch_note.md 백업
        Path patchNotePath = Paths.get(baseReleasePath, "releases/standard/patch_note.md");
        if (Files.exists(patchNotePath)) {
            patchNoteBackup = Files.readString(patchNotePath, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("\n[테스트 준비] patch_note.md 백업 완료");
        } else {
            patchNoteBackup = null;
            System.out.println("\n[테스트 준비] patch_note.md 파일 없음 (새로 생성될 예정)");
        }

        // 2. 임시 테스트 디렉토리 생성
        testBaseDir = Paths.get(baseReleasePath, "test-" + System.currentTimeMillis());
        Files.createDirectories(testBaseDir);
        System.out.println("[테스트 시작] 임시 디렉토리: " + testBaseDir.toAbsolutePath());
    }

    /**
     * 테스트 실행 후: 생성된 파일 및 디렉토리 자동 삭제
     */
    @AfterEach
    void tearDown() throws IOException {
        System.out.println("\n[테스트 정리 시작]");

        // 1. 임시 테스트 디렉토리 삭제
        if (testBaseDir != null && Files.exists(testBaseDir)) {
            deleteDirectory(testBaseDir);
            System.out.println("  ✓ 임시 디렉토리 삭제: " + testBaseDir.getFileName());
        }

        // 2. 테스트에서 생성한 버전 디렉토리들 삭제 (1.9.x/*)
        deleteVersionDirectoryIfExists("1.9.0");
        deleteVersionDirectoryIfExists("1.9.1");
        deleteVersionDirectoryIfExists("1.9.9");

        // 3. 빈 디렉토리 정리 (1.9.x가 비어있으면 삭제)
        Path majorMinorDir = Paths.get(baseReleasePath, "releases/standard/1.9.x");
        if (Files.exists(majorMinorDir) && isDirectoryEmpty(majorMinorDir)) {
            Files.delete(majorMinorDir);
            System.out.println("  ✓ 빈 디렉토리 삭제: releases/standard/1.9.x");
        }

        // 4. patch_note.md 복원
        Path patchNotePath = Paths.get(baseReleasePath, "releases/standard/patch_note.md");
        if (patchNoteBackup != null) {
            // 백업 내용으로 복원
            Files.writeString(patchNotePath, patchNoteBackup, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("  ✓ patch_note.md 복원 완료");
        } else if (Files.exists(patchNotePath)) {
            // 백업이 없었다면 테스트가 생성한 파일이므로 삭제
            Files.delete(patchNotePath);
            System.out.println("  ✓ patch_note.md 삭제 (테스트가 생성한 파일)");
        }

        // 5. DB는 @Transactional에 의해 자동 롤백됨
        System.out.println("  ✓ DB 변경사항 자동 롤백 (@Transactional)");
        System.out.println("[테스트 정리 완료]\n");
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

    /**
     * 지정된 버전의 디렉토리가 존재하면 삭제
     */
    private void deleteVersionDirectoryIfExists(String version) throws IOException {
        // 버전으로 majorMinor 추출 (예: "1.9.0" -> "1.9.x")
        String[] parts = version.split("\\.");
        String majorMinor = parts[0] + "." + parts[1] + ".x";

        Path versionDir = Paths.get(baseReleasePath,
                "releases/standard/" + majorMinor + "/" + version);

        if (Files.exists(versionDir)) {
            deleteDirectory(versionDir);
            System.out.println("  ✓ 버전 디렉토리 삭제: releases/standard/" +
                    majorMinor + "/" + version);
        }
    }

    @Test
    @DisplayName("시나리오 1: 표준 릴리즈 버전 1.9.9 생성 → SQL 파일 업로드 → 조회")
    void createStandardReleaseVersion_UploadFiles_AndVerify() throws Exception {
        // ============================================================
        // Step 1: 표준 릴리즈 버전 1.9.9 생성 (테스트 전용 버전)
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("Step 1: POST /api/release-versions/standard");
        System.out.println("========================================");

        // 요청 데이터 준비 (파라미터 순서 수정)
        ReleaseVersionDto.CreateRequest createRequest = new ReleaseVersionDto.CreateRequest(
                "1.9.9",                    // version (테스트 전용 버전)
                "jhlee@tscientific.co.kr",  // createdBy
                "테스트 릴리즈 버전",          // comment
                null,                       // customerId (표준 버전이므로 null)
                null,                       // customVersion (표준 버전이므로 null)
                true                        // isInstall
        );

        // API 호출: 버전 생성
        String createResponse = mockMvc.perform(post("/api/v1/releases/standard/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.version").value("1.9.9"))
                .andExpect(jsonPath("$.data.releaseType").value("STANDARD"))
                .andExpect(jsonPath("$.data.majorVersion").value(1))
                .andExpect(jsonPath("$.data.minorVersion").value(9))
                .andExpect(jsonPath("$.data.patchVersion").value(9))
                .andExpect(jsonPath("$.data.majorMinor").value("1.9.x"))
                .andReturn().getResponse().getContentAsString();

        // 응답에서 versionId 추출
        Long versionId = objectMapper.readTree(createResponse)
                .path("data").path("releaseVersionId").asLong();

        System.out.println("✅ 생성된 버전 ID: " + versionId);

        // ============================================================
        // Step 1 검증: DB 및 파일시스템 확인
        // ============================================================
        System.out.println("\n[검증 1] DB 확인");

        // DB 검증: release_version 테이블에 레코드 존재 확인
        var savedVersion = releaseVersionRepository.findById(versionId).orElseThrow();
        assertThat(savedVersion.getVersion()).isEqualTo("1.9.9");
        assertThat(savedVersion.getReleaseType()).isEqualTo("STANDARD");
        assertThat(savedVersion.getMajorMinor()).isEqualTo("1.9.x");

        System.out.println("  - release_version 테이블에 레코드 INSERT 완료");
        System.out.println("    version: " + savedVersion.getVersion());
        System.out.println("    release_type: " + savedVersion.getReleaseType());
        System.out.println("    major_minor: " + savedVersion.getMajorMinor());

        System.out.println("\n[검증 2] 파일시스템 확인");

        // 파일시스템 검증: 디렉토리 생성 확인
        Path mariadbDir = Paths.get(baseReleasePath,
                "releases/standard/1.9.x/1.9.9/patch/mariadb");
        Path cratedbDir = Paths.get(baseReleasePath,
                "releases/standard/1.9.x/1.9.9/patch/cratedb");

        assertThat(Files.exists(mariadbDir)).isTrue();
        assertThat(Files.isDirectory(mariadbDir)).isTrue();
        System.out.println("  ✅ MariaDB 디렉토리 생성: " + mariadbDir.toAbsolutePath());

        assertThat(Files.exists(cratedbDir)).isTrue();
        assertThat(Files.isDirectory(cratedbDir)).isTrue();
        System.out.println("  ✅ CrateDB 디렉토리 생성: " + cratedbDir.toAbsolutePath());

        // patch_note.md 생성 확인
        Path patchNoteFile = Paths.get(baseReleasePath, "releases/standard/patch_note.md");
        assertThat(Files.exists(patchNoteFile)).isTrue();

        String patchNoteContent = Files.readString(patchNoteFile);
        assertThat(patchNoteContent).contains("VERSION: 1.9.9");
        assertThat(patchNoteContent).contains("CREATED_BY: jhlee@tscientific.co.kr");
        assertThat(patchNoteContent).contains("COMMENT: 테스트 릴리즈 버전");

        System.out.println("  ✅ patch_note.md 업데이트 완료: " + patchNoteFile.toAbsolutePath());
        System.out.println("\n--- patch_note.md 내용 (일부) ---");
        System.out.println(patchNoteContent.lines().limit(10).reduce((a, b) -> a + "\n" + b)
                .orElse(""));

        // ============================================================
        // Step 2: MariaDB SQL 파일 업로드
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("Step 2: POST /api/release-files/versions/{versionId}/files/upload (MariaDB)");
        System.out.println("========================================");

        // 가상의 SQL 파일 생성
        String mariadbSqlContent = """
                -- MariaDB 테이블 생성 스크립트
                CREATE TABLE IF NOT EXISTS sample_table (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

                INSERT INTO sample_table (name) VALUES ('테스트 데이터');
                """;

        MockMultipartFile mariadbFile = new MockMultipartFile(
                "files",                        // parameter name
                "001_create_table.sql",         // original filename
                "text/plain",                   // content type
                mariadbSqlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8)    // content
        );

        // API 호출: MariaDB 파일 업로드
        mockMvc.perform(multipart("/api/v1/releases/versions/{versionId}/files/upload", versionId)
                        .file(mariadbFile)
                        .param("databaseType", "MARIADB")
                        .param("uploadedBy", "jhlee@tscientific.co.kr"))
                .andDo(print())
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].fileName").value("001_create_table.sql"))
                .andExpect(jsonPath("$.data[0].databaseTypeName").value("MARIADB"))
                .andExpect(jsonPath("$.data[0].executionOrder").value(1)); // 첫 번째 파일이므로 순서 1

        System.out.println("✅ MariaDB SQL 파일 업로드 완료");

        // ============================================================
        // Step 2 검증: DB 및 파일시스템 확인
        // ============================================================
        System.out.println("\n[검증 3] MariaDB 파일 업로드 확인");

        // DB 검증: release_file 테이블 확인
        var mariadbFiles = releaseFileRepository.findByVersionAndDatabaseType(versionId,
                "MARIADB");
        assertThat(mariadbFiles).hasSize(1);
        assertThat(mariadbFiles.get(0).getFileName()).isEqualTo("001_create_table.sql");
        assertThat(mariadbFiles.get(0).getExecutionOrder()).isEqualTo(1);
        assertThat(mariadbFiles.get(0).getChecksum()).isNotNull(); // MD5 체크섬 존재

        System.out.println("  - release_file 테이블에 레코드 INSERT 완료");
        System.out.println("    file_name: " + mariadbFiles.get(0).getFileName());
        System.out.println("    file_path: " + mariadbFiles.get(0).getFilePath());
        System.out.println("    file_size: " + mariadbFiles.get(0).getFileSize() + " bytes");
        System.out.println("    checksum: " + mariadbFiles.get(0).getChecksum());
        System.out.println("    execution_order: " + mariadbFiles.get(0).getExecutionOrder());

        // 파일시스템 검증: 실제 파일 저장 확인
        Path savedMariadbFile = Paths.get(baseReleasePath, mariadbFiles.get(0).getFilePath());
        assertThat(Files.exists(savedMariadbFile)).isTrue();

        String savedContent = Files.readString(savedMariadbFile, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(savedContent).isEqualTo(mariadbSqlContent);

        System.out.println("  ✅ 실제 파일 저장: " + savedMariadbFile.toAbsolutePath());

        // ============================================================
        // Step 3: CrateDB SQL 파일 업로드
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("Step 3: POST /api/release-files/versions/{versionId}/files/upload (CrateDB)");
        System.out.println("========================================");

        String cratedbSqlContent = """
                -- CrateDB 테이블 생성 스크립트
                CREATE TABLE IF NOT EXISTS sample_table (
                    id LONG PRIMARY KEY,
                    name STRING,
                    created_at TIMESTAMP
                );

                INSERT INTO sample_table (id, name, created_at)
                VALUES (1, '테스트 데이터', CURRENT_TIMESTAMP);
                """;

        MockMultipartFile cratedbFile = new MockMultipartFile(
                "files",
                "001_create_table.sql",
                "text/plain",
                cratedbSqlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/releases/versions/{versionId}/files/upload", versionId)
                        .file(cratedbFile)
                        .param("databaseType", "CRATEDB")
                        .param("uploadedBy", "jhlee@tscientific.co.kr"))
                .andDo(print())
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].fileName").value("001_create_table.sql"))
                .andExpect(jsonPath("$.data[0].databaseTypeName").value("CRATEDB"))
                .andExpect(jsonPath("$.data[0].executionOrder").value(1));

        System.out.println("✅ CrateDB SQL 파일 업로드 완료");

        // ============================================================
        // Step 4: 업로드된 파일 목록 조회
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("Step 4: GET /api/release-files/versions/{versionId}/files");
        System.out.println("========================================");

        mockMvc.perform(get("/api/v1/releases/versions/{versionId}/files", versionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2)); // MariaDB 1개 + CrateDB 1개

        System.out.println("✅ 전체 파일 목록 조회 완료 (2개)");

        // ============================================================
        // 최종 요약
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("✅ 시나리오 1 완료!");
        System.out.println("========================================");
        System.out.println("생성된 버전: 1.9.9 (ID: " + versionId + ")");
        System.out.println("업로드된 파일: 2개 (MariaDB 1개, CrateDB 1개)");
        System.out.println("\n디렉토리 구조:");
        System.out.println("releases/standard/");
        System.out.println("├── patch_note.md");
        System.out.println("└── 1.9.x/");
        System.out.println("    └── 1.9.9/");
        System.out.println("        └── patch/");
        System.out.println("            ├── mariadb/");
        System.out.println("            │   └── 001_create_table.sql");
        System.out.println("            └── cratedb/");
        System.out.println("                └── 001_create_table.sql");
        System.out.println("\n⚠️ 테스트 종료 후:");
        System.out.println("  - DB 변경사항: 자동 롤백 (@Transactional)");
        System.out.println("  - 파일: 테스트 후 자동 삭제 (@AfterEach)");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("시나리오 2: 여러 버전 생성 및 다중 파일 업로드 (실행순서 자동 부여)")
    void createMultipleVersions_UploadMultipleFiles_VerifyExecutionOrder() throws Exception {
        // ============================================================
        // Step 1: 버전 1.9.0 생성 (테스트 전용)
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("버전 1.9.0 생성 (테스트 전용)");
        System.out.println("========================================");

        ReleaseVersionDto.CreateRequest v190Request = new ReleaseVersionDto.CreateRequest(
                "1.9.0", "jhlee", "테스트용 초기 버전", null, null, true
        );

        String v190Response = mockMvc.perform(post("/api/v1/releases/standard/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v190Request)))
                .andExpect(status().isCreated())  // 201 Created
                .andReturn().getResponse().getContentAsString();

        Long v190Id = objectMapper.readTree(v190Response)
                .path("data").path("releaseVersionId").asLong();

        // ============================================================
        // Step 2: 1.9.0에 3개의 SQL 파일 업로드 (실행순서 자동 부여)
        // ============================================================
        System.out.println("\n1.9.0에 3개 파일 업로드 → 실행순서 1, 2, 3 자동 부여");

        MockMultipartFile file1 = new MockMultipartFile("files", "001_schema.sql",
                "text/plain", "CREATE TABLE users;".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "002_data.sql",
                "text/plain", "INSERT INTO users VALUES (1, 'test');".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("files", "003_index.sql",
                "text/plain", "CREATE INDEX idx_users ON users(id);".getBytes());

        mockMvc.perform(multipart("/api/v1/releases/versions/{versionId}/files/upload", v190Id)
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .param("databaseType", "MARIADB")
                        .param("uploadedBy", "jhlee"))
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(jsonPath("$.data[0].executionOrder").value(1))
                .andExpect(jsonPath("$.data[1].executionOrder").value(2))
                .andExpect(jsonPath("$.data[2].executionOrder").value(3));

        System.out.println("  ✅ 001_schema.sql → execution_order: 1");
        System.out.println("  ✅ 002_data.sql   → execution_order: 2");
        System.out.println("  ✅ 003_index.sql  → execution_order: 3");

        // ============================================================
        // Step 3: 추가 파일 업로드 → 기존 최대값(3) 다음부터 시작
        // ============================================================
        System.out.println("\n추가로 1개 파일 더 업로드 → 실행순서 4부터 시작");

        MockMultipartFile file4 = new MockMultipartFile("files", "004_alter.sql",
                "text/plain", "ALTER TABLE users ADD COLUMN email VARCHAR(100);".getBytes());

        mockMvc.perform(multipart("/api/v1/releases/versions/{versionId}/files/upload", v190Id)
                        .file(file4)
                        .param("databaseType", "MARIADB")
                        .param("uploadedBy", "jhlee"))
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(jsonPath("$.data[0].executionOrder").value(4)); // 기존 최대값 3 + 1

        System.out.println("  ✅ 004_alter.sql  → execution_order: 4");

        // ============================================================
        // Step 4: 버전 1.9.1 생성 및 파일 업로드 → 새 버전이므로 다시 1부터
        // ============================================================
        System.out.println("\n========================================");
        System.out.println("버전 1.9.1 생성 및 파일 업로드 (테스트 전용)");
        System.out.println("========================================");

        ReleaseVersionDto.CreateRequest v191Request = new ReleaseVersionDto.CreateRequest(
                "1.9.1", "jhlee", "테스트용 버그 수정", null, null, true
        );

        String v191Response = mockMvc.perform(post("/api/v1/releases/standard/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v191Request)))
                .andExpect(status().isCreated())  // 201 Created
                .andReturn().getResponse().getContentAsString();

        Long v191Id = objectMapper.readTree(v191Response)
                .path("data").path("releaseVersionId").asLong();

        MockMultipartFile v191File = new MockMultipartFile("files", "001_bugfix.sql",
                "text/plain", "UPDATE users SET status = 'active';".getBytes());

        mockMvc.perform(multipart("/api/v1/releases/versions/{versionId}/files/upload", v191Id)
                        .file(v191File)
                        .param("databaseType", "MARIADB")
                        .param("uploadedBy", "jhlee"))
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(jsonPath("$.data[0].executionOrder").value(1)); // 새 버전이므로 1부터

        System.out.println("  ✅ 001_bugfix.sql → execution_order: 1 (새 버전이므로 1부터 시작)");

        System.out.println("\n========================================");
        System.out.println("✅ 시나리오 2 완료!");
        System.out.println("========================================");
        System.out.println("버전 1.9.0: 파일 4개 (실행순서 1~4)");
        System.out.println("버전 1.9.1: 파일 1개 (실행순서 1)");
        System.out.println("\n⚠️ 테스트 종료 후 모든 데이터 자동 정리됨");
        System.out.println("========================================\n");
    }
}
