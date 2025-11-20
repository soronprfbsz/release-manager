package com.ts.rm.domain.patch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ts.rm.domain.patch.entity.CumulativePatch;
import com.ts.rm.domain.patch.repository.CumulativePatchRepository;
import com.ts.rm.domain.release.entity.ReleaseFile;
import com.ts.rm.domain.release.entity.ReleaseVersion;
import com.ts.rm.domain.release.repository.ReleaseFileRepository;
import com.ts.rm.domain.release.repository.ReleaseVersionRepository;
import com.ts.rm.global.common.exception.BusinessException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CumulativePatch Service 테스트")
class CumulativePatchServiceTest {

    @InjectMocks
    private CumulativePatchService cumulativePatchService;

    @Mock
    private CumulativePatchRepository cumulativePatchRepository;

    @Mock
    private ReleaseVersionRepository releaseVersionRepository;

    @Mock
    private ReleaseFileRepository releaseFileRepository;

    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // 임시 디렉토리 생성
        tempDir = Files.createTempDirectory("release-test");
        ReflectionTestUtils.setField(cumulativePatchService, "releaseBasePath",
                tempDir.toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        // 임시 디렉토리 삭제
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // 하위부터 삭제
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            // 무시
                        }
                    });
        }
    }

    @Test
    @DisplayName("누적 패치 생성 성공 테스트 - From 1.0.0 To 1.1.1")
    void generateCumulativePatch_success() {
        // given
        ReleaseVersion fromVersion = createReleaseVersion(1L, "1.0.0", "STANDARD");
        ReleaseVersion toVersion = createReleaseVersion(3L, "1.1.1", "STANDARD");
        ReleaseVersion v110 = createReleaseVersion(2L, "1.1.0", "STANDARD");

        List<ReleaseVersion> betweenVersions = Arrays.asList(v110, toVersion);

        // Mock: 버전 조회
        given(releaseVersionRepository.findById(1L)).willReturn(Optional.of(fromVersion));
        given(releaseVersionRepository.findById(3L)).willReturn(Optional.of(toVersion));
        given(releaseVersionRepository.findVersionsBetween("STANDARD", "1.0.0", "1.1.1"))
                .willReturn(betweenVersions);

        // Mock: 각 버전의 파일 목록 조회
        List<ReleaseFile> files110 = Arrays.asList(
                createReleaseFile(1L, v110, "mariadb", "1.patch_mariadb_ddl.sql", 1),
                createReleaseFile(2L, v110, "mariadb", "2.patch_mariadb_view.sql", 2)
        );
        List<ReleaseFile> files111 = Arrays.asList(
                createReleaseFile(3L, toVersion, "mariadb", "1.patch_mariadb_ddl.sql", 1)
        );

        given(releaseFileRepository.findAllByReleaseVersionIdOrderByExecutionOrderAsc(2L))
                .willReturn(files110);
        given(releaseFileRepository.findAllByReleaseVersionIdOrderByExecutionOrderAsc(3L))
                .willReturn(files111);

        // Mock: 누적 패치 이력 저장
        CumulativePatch savedPatch = CumulativePatch.builder()
                .cumulativePatchId(1L)
                .releaseType("STANDARD")
                .fromVersion("1.0.0")
                .toVersion("1.1.1")
                .patchName("from-1.0.0")
                .outputPath("releases/standard/1.1.x/1.1.1/from-1.0.0")
                .generatedBy("admin@tscientific.co.kr")
                .status("SUCCESS")
                .build();

        given(cumulativePatchRepository.save(any(CumulativePatch.class)))
                .willReturn(savedPatch);

        // when
        CumulativePatch result = cumulativePatchService.generateCumulativePatch(
                1L, 3L, "admin@tscientific.co.kr"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFromVersion()).isEqualTo("1.0.0");
        assertThat(result.getToVersion()).isEqualTo("1.1.1");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");

        verify(releaseVersionRepository).findVersionsBetween("STANDARD", "1.0.0", "1.1.1");
        verify(cumulativePatchRepository).save(any(CumulativePatch.class));
    }

    @Test
    @DisplayName("누적 패치 생성 실패 - From 버전이 To 버전보다 큰 경우")
    void generateCumulativePatch_fail_invalidVersionRange() {
        // given
        ReleaseVersion fromVersion = createReleaseVersion(3L, "1.1.1", "STANDARD");
        ReleaseVersion toVersion = createReleaseVersion(1L, "1.0.0", "STANDARD");

        given(releaseVersionRepository.findById(3L)).willReturn(Optional.of(fromVersion));
        given(releaseVersionRepository.findById(1L)).willReturn(Optional.of(toVersion));

        // when & then
        assertThatThrownBy(() ->
                cumulativePatchService.generateCumulativePatch(3L, 1L, "admin@tscientific.co.kr")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("From 버전은 To 버전보다 작아야 합니다");
    }

    @Test
    @DisplayName("누적 패치 생성 실패 - From 버전이 존재하지 않는 경우")
    void generateCumulativePatch_fail_fromVersionNotFound() {
        // given
        given(releaseVersionRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                cumulativePatchService.generateCumulativePatch(999L, 3L, "admin@tscientific.co.kr")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("버전을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("누적 패치 생성 실패 - 중간 버전이 없는 경우")
    void generateCumulativePatch_fail_noVersionsBetween() {
        // given
        ReleaseVersion fromVersion = createReleaseVersion(1L, "1.0.0", "STANDARD");
        ReleaseVersion toVersion = createReleaseVersion(2L, "1.0.1", "STANDARD");

        given(releaseVersionRepository.findById(1L)).willReturn(Optional.of(fromVersion));
        given(releaseVersionRepository.findById(2L)).willReturn(Optional.of(toVersion));
        given(releaseVersionRepository.findVersionsBetween("STANDARD", "1.0.0", "1.0.1"))
                .willReturn(Arrays.asList());

        // when & then
        assertThatThrownBy(() ->
                cumulativePatchService.generateCumulativePatch(1L, 2L, "admin@tscientific.co.kr")
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("패치할 버전이 없습니다");
    }

    // ========== Helper Methods ==========

    private ReleaseVersion createReleaseVersion(Long id, String version, String releaseType) {
        String[] parts = version.split("\\.");
        return ReleaseVersion.builder()
                .releaseVersionId(id)
                .version(version)
                .releaseType(releaseType)
                .majorVersion(Integer.parseInt(parts[0]))
                .minorVersion(Integer.parseInt(parts[1]))
                .patchVersion(Integer.parseInt(parts[2]))
                .majorMinor(parts[0] + "." + parts[1])
                .createdBy("system")
                .build();
    }

    private ReleaseFile createReleaseFile(Long id, ReleaseVersion version,
                                          String dbType, String fileName, int order) {
        return ReleaseFile.builder()
                .releaseFileId(id)
                .releaseVersion(version)
                .databaseType(dbType)
                .fileName(fileName)
                .filePath("releases/standard/" + version.getMajorMinor() + ".x/"
                        + version.getVersion() + "/patch/" + dbType + "/" + fileName)
                .executionOrder(order)
                .build();
    }
}
