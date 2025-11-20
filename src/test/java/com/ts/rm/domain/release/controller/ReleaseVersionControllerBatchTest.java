package com.ts.rm.domain.release.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.config.TestQueryDslConfig;
import com.ts.rm.domain.release.dto.ReleaseVersionDto;
import com.ts.rm.domain.release.repository.ReleaseFileRepository;
import com.ts.rm.domain.release.repository.ReleaseVersionRepository;
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
 * ReleaseVersionController 일괄 생성 API 테스트
 *
 * <p>릴리즈 버전 생성 + SQL 파일 업로드를 하나의 API로 처리하는 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.springframework.context.annotation.Import(TestQueryDslConfig.class)
public class ReleaseVersionControllerBatchTest {

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

    private String patchNoteBackup;

    @BeforeEach
    void setUp() throws IOException {
        // patch_note.md 백업
        Path patchNotePath = Paths.get(baseReleasePath, "releases/standard/patch_note.md");
        if (Files.exists(patchNotePath)) {
            patchNoteBackup = Files.readString(patchNotePath, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("\n[테스트 준비] patch_note.md 백업 완료");
        } else {
            patchNoteBackup = null;
            System.out.println("\n[테스트 준비] patch_note.md 파일 없음");
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        System.out.println("\n[테스트 정리 시작]");

        // 테스트에서 생성한 버전 디렉토리 삭제
        deleteVersionDirectoryIfExists("99.9.8");

        // patch_note.md 복원
        Path patchNotePath = Paths.get(baseReleasePath, "releases/standard/patch_note.md");
        if (patchNoteBackup != null) {
            Files.writeString(patchNotePath, patchNoteBackup, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("  ✓ patch_note.md 복원 완료");
        } else if (Files.exists(patchNotePath)) {
            Files.delete(patchNotePath);
            System.out.println("  ✓ patch_note.md 삭제");
        }

        System.out.println("  ✓ DB 변경사항 자동 롤백 (@Transactional)");
        System.out.println("[테스트 정리 완료]\n");
    }

    @Test
    @DisplayName("일괄 생성: 버전 생성 + MariaDB/CrateDB 파일 업로드 한 번에")
    void createStandardVersionWithFiles_Success() throws Exception {
        // given
        String version = "99.9.8";
        String createdBy = "jhlee@tscientific.co.kr";
        String comment = "일괄 생성 테스트";

        // 요청 데이터 (JSON)
        ReleaseVersionDto.CreateRequest request = new ReleaseVersionDto.CreateRequest(
                version,
                createdBy,
                comment,
                null,
                null,
                true
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        // MariaDB SQL 파일들
        MockMultipartFile mariadbFile1 = new MockMultipartFile(
                "mariadbFiles",
                "001_create_table.sql",
                "text/plain",
                "CREATE TABLE test (id BIGINT);".getBytes()
        );

        MockMultipartFile mariadbFile2 = new MockMultipartFile(
                "mariadbFiles",
                "002_insert_data.sql",
                "text/plain",
                "INSERT INTO test VALUES (1);".getBytes()
        );

        // CrateDB SQL 파일
        MockMultipartFile cratedbFile = new MockMultipartFile(
                "cratedbFiles",
                "001_create_table.sql",
                "text/plain",
                "CREATE TABLE test (id LONG);".getBytes()
        );

        // when & then
        String response = mockMvc.perform(multipart("/api/v1/releases/standard/versions/batch")
                        .file(requestPart)
                        .file(mariadbFile1)
                        .file(mariadbFile2)
                        .file(cratedbFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.version").value(version))
                .andExpect(jsonPath("$.data.majorMinor").value("99.9.x"))
                .andExpect(jsonPath("$.data.releaseType").value("STANDARD"))
                .andReturn().getResponse().getContentAsString();

        Long versionId = objectMapper.readTree(response)
                .path("data").path("releaseVersionId").asLong();

        System.out.println("✅ 생성된 버전 ID: " + versionId);

        // DB 검증: release_version
        var savedVersion = releaseVersionRepository.findById(versionId).orElseThrow();
        assertThat(savedVersion.getVersion()).isEqualTo(version);
        assertThat(savedVersion.getMajorMinor()).isEqualTo("99.9.x");

        // DB 검증: release_file (MariaDB 2개 + CrateDB 1개 = 총 3개)
        var mariadbFiles = releaseFileRepository.findByVersionAndDatabaseType(versionId, "MARIADB");
        assertThat(mariadbFiles).hasSize(2);
        assertThat(mariadbFiles.get(0).getExecutionOrder()).isEqualTo(1);
        assertThat(mariadbFiles.get(1).getExecutionOrder()).isEqualTo(2);

        var cratedbFiles = releaseFileRepository.findByVersionAndDatabaseType(versionId, "CRATEDB");
        assertThat(cratedbFiles).hasSize(1);
        assertThat(cratedbFiles.get(0).getExecutionOrder()).isEqualTo(1);

        // 파일시스템 검증: 디렉토리 생성
        Path mariadbDir = Paths.get(baseReleasePath, "releases/standard/99.9.x/99.9.8/patch/mariadb");
        Path cratedbDir = Paths.get(baseReleasePath, "releases/standard/99.9.x/99.9.8/patch/cratedb");

        assertThat(Files.exists(mariadbDir)).isTrue();
        assertThat(Files.exists(cratedbDir)).isTrue();

        // 파일시스템 검증: 실제 파일 저장
        Path mariadbSql1 = mariadbDir.resolve("001_create_table.sql");
        Path mariadbSql2 = mariadbDir.resolve("002_insert_data.sql");
        Path cratedbSql = cratedbDir.resolve("001_create_table.sql");

        assertThat(Files.exists(mariadbSql1)).isTrue();
        assertThat(Files.exists(mariadbSql2)).isTrue();
        assertThat(Files.exists(cratedbSql)).isTrue();

        // 파일시스템 검증: patch_note.md 업데이트
        Path patchNotePath = Paths.get(baseReleasePath, "releases/standard/patch_note.md");
        String patchNoteContent = Files.readString(patchNotePath);
        assertThat(patchNoteContent).contains("VERSION: " + version);
        assertThat(patchNoteContent).contains("COMMENT: " + comment);

        System.out.println("✅ 일괄 생성 테스트 완료!");
        System.out.println("  - 버전 생성: " + version);
        System.out.println("  - MariaDB 파일: 2개");
        System.out.println("  - CrateDB 파일: 1개");
    }

    @Test
    @DisplayName("일괄 생성: MariaDB 파일만 업로드")
    void createStandardVersionWithFiles_MariaDBOnly() throws Exception {
        // given
        String version = "99.9.8";

        ReleaseVersionDto.CreateRequest request = new ReleaseVersionDto.CreateRequest(
                version, "jhlee", "MariaDB만", null, null, true
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile mariadbFile = new MockMultipartFile(
                "mariadbFiles", "001_schema.sql", "text/plain",
                "CREATE TABLE test;".getBytes()
        );

        // when & then
        String response = mockMvc.perform(multipart("/api/v1/releases/standard/versions/batch")
                        .file(requestPart)
                        .file(mariadbFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn().getResponse().getContentAsString();

        Long versionId = objectMapper.readTree(response)
                .path("data").path("releaseVersionId").asLong();

        // 검증
        var mariadbFiles = releaseFileRepository.findByVersionAndDatabaseType(versionId, "MARIADB");
        var cratedbFiles = releaseFileRepository.findByVersionAndDatabaseType(versionId, "CRATEDB");

        assertThat(mariadbFiles).hasSize(1);
        assertThat(cratedbFiles).isEmpty();

        System.out.println("✅ MariaDB만 업로드 테스트 완료!");
    }

    @Test
    @DisplayName("일괄 생성: 파일 없이 버전만 생성")
    void createStandardVersionWithFiles_NoFiles() throws Exception {
        // given
        String version = "99.9.8";

        ReleaseVersionDto.CreateRequest request = new ReleaseVersionDto.CreateRequest(
                version, "jhlee", "버전만 생성", null, null, true
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        // when & then
        String response = mockMvc.perform(multipart("/api/v1/releases/standard/versions/batch")
                        .file(requestPart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn().getResponse().getContentAsString();

        Long versionId = objectMapper.readTree(response)
                .path("data").path("releaseVersionId").asLong();

        // 검증: 파일이 없어야 함
        var allFiles = releaseFileRepository.findAllByReleaseVersionIdOrderByExecutionOrderAsc(versionId);
        assertThat(allFiles).isEmpty();

        System.out.println("✅ 버전만 생성 테스트 완료!");
    }

    @Test
    @DisplayName("일괄 생성 실패: 중복 버전")
    void createStandardVersionWithFiles_DuplicateVersion() throws Exception {
        // given: 먼저 버전 생성
        String version = "99.9.8";
        ReleaseVersionDto.CreateRequest request = new ReleaseVersionDto.CreateRequest(
                version, "jhlee", "첫 번째", null, null, true
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/v1/releases/standard/versions/batch")
                .file(requestPart))
                .andExpect(status().isCreated());

        // when & then: 같은 버전으로 다시 생성 시도
        mockMvc.perform(multipart("/api/v1/releases/standard/versions/batch")
                        .file(requestPart))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("fail"));

        System.out.println("✅ 중복 버전 방지 테스트 완료!");
    }

    // Helper methods
    private void deleteVersionDirectoryIfExists(String version) throws IOException {
        // 안전장치: 테스트 경로가 아니면 삭제하지 않음
        if (!baseReleasePath.contains("test-release")) {
            System.out.println("  ⚠️  경고: 프로덕션 경로 감지, 삭제 건너뜀 - " + baseReleasePath);
            return;
        }

        // 안전장치: 99.x.x 버전이 아니면 삭제하지 않음
        if (!version.startsWith("99.")) {
            System.out.println("  ⚠️  경고: 테스트 버전(99.x.x)이 아님, 삭제 건너뜀 - " + version);
            return;
        }

        String[] parts = version.split("\\.");
        String majorMinor = parts[0] + "." + parts[1] + ".x";

        Path versionDir = Paths.get(baseReleasePath,
                "releases/standard/" + majorMinor + "/" + version);

        if (Files.exists(versionDir)) {
            deleteDirectory(versionDir);
            System.out.println("  ✓ 버전 디렉토리 삭제: " + majorMinor + "/" + version);
        }

        // 빈 디렉토리 정리
        Path majorMinorDir = Paths.get(baseReleasePath, "releases/standard/" + majorMinor);
        if (Files.exists(majorMinorDir) && isDirectoryEmpty(majorMinorDir)) {
            Files.delete(majorMinorDir);
            System.out.println("  ✓ 빈 디렉토리 삭제: " + majorMinor);
        }
    }

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

    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.findAny().isEmpty();
        }
    }
}
