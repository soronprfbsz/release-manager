package com.ts.rm.domain.releasefile.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.domain.account.repository.AccountRepository;
import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.domain.releasefile.service.ReleaseFileService;
import com.ts.rm.global.common.exception.GlobalExceptionHandler;
import com.ts.rm.global.config.MessageConfig;
import com.ts.rm.global.jwt.JwtTokenProvider;
import com.ts.rm.global.security.CustomUserDetailsService;
import com.ts.rm.global.security.JwtAuthenticationFilter;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ReleaseFile Controller 통합 테스트
 */
@WebMvcTest(controllers = ReleaseFileController.class,
        excludeAutoConfiguration = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, MessageConfig.class})
@ActiveProfiles("test")
@DisplayName("ReleaseFileController 테스트")
class ReleaseFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReleaseFileService releaseFileService;

    // Security 관련 MockBean 추가
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private ReleaseFileDto.SimpleResponse simpleResponse;
    private ReleaseFileDto.DetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        simpleResponse = new ReleaseFileDto.SimpleResponse(
                1L,
                "1.1.0",
                "MARIADB",
                "001_create_users_table.sql",
                1024L,
                "abc123def456",
                1,
                "Create users table"
        );

        detailResponse = new ReleaseFileDto.DetailResponse(
                1L,
                1L,
                "1.1.0",
                null,
                "MARIADB",
                "001_create_users_table.sql",
                "/release/1.1.0/patch/mariadb/001_create_users_table.sql",
                1024L,
                "abc123def456",
                1,
                "Create users table",
                now,
                now
        );
    }

    @Test
    @DisplayName("버전별 릴리즈 파일 목록 조회 - 성공")
    void getReleaseFilesByVersion_Success() throws Exception {
        // given
        List<ReleaseFileDto.SimpleResponse> responses = List.of(simpleResponse);
        given(releaseFileService.getReleaseFilesByVersion(1L)).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/releases/versions/{versionId}/files", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fileName").value("001_create_users_table.sql"));
    }

    @Test
    @DisplayName("버전+DB타입별 릴리즈 파일 목록 조회 - 성공")
    void getReleaseFilesByVersionAndDbType_Success() throws Exception {
        // given
        List<ReleaseFileDto.SimpleResponse> responses = List.of(simpleResponse);
        given(releaseFileService.getReleaseFilesByVersionAndDbType(1L, "MARIADB")).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/releases/versions/{versionId}/files", 1L)
                        .param("databaseType", "MARIADB"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("릴리즈 파일 상세 조회 - 성공")
    void getReleaseFileById_Success() throws Exception {
        // given
        given(releaseFileService.getReleaseFileById(1L)).willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/api/v1/releases/files/{fileId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.releaseFileId").value(1))
                .andExpect(jsonPath("$.data.fileName").value("001_create_users_table.sql"));
    }

    @Test
    @DisplayName("릴리즈 파일 업로드 - 성공")
    void uploadReleaseFiles_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "001_create_users_table.sql",
                "text/plain",
                "CREATE TABLE users (id INT);".getBytes()
        );

        List<ReleaseFileDto.DetailResponse> responses = List.of(detailResponse);
        given(releaseFileService.uploadReleaseFiles(eq(1L), any(), any())).willReturn(responses);

        // when & then
        mockMvc.perform(multipart("/api/v1/releases/versions/{versionId}/files/upload", 1L)
                        .file(file)
                        .param("databaseType", "MARIADB")
                        .param("uploadedBy", "admin@tscientific"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("릴리즈 파일 다운로드 - 성공")
    void downloadReleaseFile_Success() throws Exception {
        // given
        ByteArrayResource resource = new ByteArrayResource("CREATE TABLE users (id INT);".getBytes());
        given(releaseFileService.downloadReleaseFile(1L)).willReturn(resource);
        given(releaseFileService.getReleaseFileById(1L)).willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/api/v1/releases/files/{fileId}/download", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    @DisplayName("릴리즈 파일 정보 수정 - 성공")
    void updateReleaseFile_Success() throws Exception {
        // given
        ReleaseFileDto.UpdateRequest request = ReleaseFileDto.UpdateRequest.builder()
                .description("Updated description")
                .executionOrder(2)
                .build();

        ReleaseFileDto.DetailResponse updatedResponse = new ReleaseFileDto.DetailResponse(
                1L,
                1L,
                "1.1.0",
                null,
                "MARIADB",
                "001_create_users_table.sql",
                "/release/1.1.0/patch/mariadb/001_create_users_table.sql",
                1024L,
                "abc123def456",
                2,
                "Updated description",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(releaseFileService.updateReleaseFile(eq(1L), any())).willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/v1/releases/files/{fileId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.description").value("Updated description"))
                .andExpect(jsonPath("$.data.executionOrder").value(2));
    }

    @Test
    @DisplayName("릴리즈 파일 삭제 - 성공")
    void deleteReleaseFile_Success() throws Exception {
        // given
        willDoNothing().given(releaseFileService).deleteReleaseFile(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/releases/files/{fileId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}
