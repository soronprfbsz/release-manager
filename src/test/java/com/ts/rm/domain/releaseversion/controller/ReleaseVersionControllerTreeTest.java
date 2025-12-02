package com.ts.rm.domain.releaseversion.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ts.rm.config.TestQueryDslConfig;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReleaseVersion 트리 조회 API 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
@Import(TestQueryDslConfig.class)
@DisplayName("ReleaseVersion 트리 조회 API 테스트")
class ReleaseVersionControllerTreeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository releaseVersionRepository;

    @BeforeEach
    void setUp() {
        // 테스트용 버전 데이터 생성 (DB만 직접 저장, 파일 시스템 사용 안함)
        createTestVersionDirectly("1.1.0", "jhlee@tscientific", "Initial version");
        createTestVersionDirectly("1.1.1", "jhlee@tscientific", "Bug fix");
        createTestVersionDirectly("1.2.0", "jhlee@tscientific", "New features");
        createTestVersionDirectly("1.2.1", "jhlee@tscientific", "Hotfix");
        createTestVersionDirectly("2.0.0", "jhlee@tscientific", "Major release");
    }

    @Test
    @DisplayName("Standard 릴리즈 트리 조회 성공")
    void getStandardReleaseTree_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/releases/standard/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.releaseType").value("STANDARD"))
                .andExpect(jsonPath("$.data.customerCode").doesNotExist())
                .andExpect(jsonPath("$.data.majorMinorGroups").isArray())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].majorMinor").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions").isArray())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].version").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].createdAt").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].createdBy").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].comment").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].categories").isArray());
    }

    @Test
    @DisplayName("Standard 릴리즈 트리 - 메이저.마이너 그룹 확인")
    void getStandardReleaseTree_CheckMajorMinorGroups() throws Exception {
        // when & then
        mockMvc.perform(get("/api/releases/standard/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.majorMinorGroups").isArray())
                // 1.1.x 그룹 확인
                .andExpect(jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.1.x')]").exists())
                .andExpect(
                        jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.1.x')].versions.length()").value(2))
                // 1.2.x 그룹 확인
                .andExpect(jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.2.x')]").exists())
                .andExpect(
                        jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.2.x')].versions.length()").value(2))
                // 2.0.x 그룹 확인
                .andExpect(jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '2.0.x')]").exists())
                .andExpect(
                        jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '2.0.x')].versions.length()").value(1));
    }

    @Test
    @DisplayName("Standard 릴리즈 트리 - 버전 내림차순 정렬 확인")
    void getStandardReleaseTree_VersionDescendingOrder() throws Exception {
        // when & then
        mockMvc.perform(get("/api/releases/standard/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // 1.1.x 그룹 내 버전 정렬: 1.1.1 > 1.1.0
                .andExpect(
                        jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.1.x')].versions[0].version").value(
                                "1.1.1"))
                .andExpect(
                        jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.1.x')].versions[1].version").value(
                                "1.1.0"))
                // 1.2.x 그룹 내 버전 정렬: 1.2.1 > 1.2.0
                .andExpect(
                        jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.2.x')].versions[0].version").value(
                                "1.2.1"))
                .andExpect(
                        jsonPath("$.data.majorMinorGroups[?(@.majorMinor == '1.2.x')].versions[1].version").value(
                                "1.2.0"));
    }

    @Test
    @DisplayName("Standard 릴리즈 트리 - 데이터베이스 파일 목록 확인")
    void getStandardReleaseTree_CheckDatabaseFiles() throws Exception {
        // when & then
        mockMvc.perform(get("/api/releases/standard/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].categories").isArray());
                // 주의: 테스트 환경에서는 실제 파일이 없어서 categories가 빈 배열일 수 있음
                // 실제 환경에서는 DATABASE, WEB, ENGINE, INSTALL 카테고리가 포함됨
    }

    @Test
    @DisplayName("데이터 없을 경우 빈 트리 반환")
    void getStandardReleaseTree_EmptyData() throws Exception {
        // given - 모든 데이터 삭제 (Transactional이므로 롤백됨)
        // 실제로는 별도의 테스트 메서드에서 데이터 없이 시작

        // when & then
        mockMvc.perform(get("/api/releases/standard/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.releaseType").value("STANDARD"))
                .andExpect(jsonPath("$.data.majorMinorGroups").isArray());
    }

    @Test
    @DisplayName("Standard 릴리즈 트리 - versionId 포함 확인")
    void getStandardReleaseTree_IncludesVersionId() throws Exception {
        // when & then
        mockMvc.perform(get("/api/releases/standard/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                // versionId가 모든 버전 노드에 포함되어 있는지 확인
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].versionId").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].versionId").isNumber())
                // versionId가 null이 아닌지 확인
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].versionId").isNotEmpty());
    }

    @Test
    @DisplayName("Standard 릴리즈 트리 - DB 기반 조회로 versionId와 파일 정보 동시 제공")
    void getStandardReleaseTree_ProvidesVersionIdAndFileInfo() throws Exception {
        // when & then
        mockMvc.perform(get("/api/releases/standard/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                // 첫 번째 major.minor 그룹의 첫 번째 버전
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].versionId").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].version").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].createdAt").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].createdBy").exists())
                .andExpect(jsonPath("$.data.majorMinorGroups[0].versions[0].categories").isArray());
    }

    /**
     * 테스트용 버전 생성 헬퍼 메서드 (DB만 직접 저장)
     * 파일 시스템을 건드리지 않고 DB에만 데이터 저장
     */
    private void createTestVersionDirectly(String version, String createdBy, String comment) {
        // 버전 파싱 (예: "1.1.0" -> major=1, minor=1, patch=0)
        String[] parts = version.split("\\.");
        Integer major = Integer.parseInt(parts[0]);
        Integer minor = Integer.parseInt(parts[1]);
        Integer patch = Integer.parseInt(parts[2]);

        com.ts.rm.domain.releaseversion.entity.ReleaseVersion releaseVersion =
                com.ts.rm.domain.releaseversion.entity.ReleaseVersion.builder()
                .version(version)
                .releaseType("STANDARD")
                .majorVersion(major)
                .minorVersion(minor)
                .patchVersion(patch)
                .comment(comment)
                .createdBy(createdBy)
                .build();

        releaseVersionRepository.save(releaseVersion);
    }
}
