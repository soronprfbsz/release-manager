package com.ts.rm.domain.release.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ts.rm.domain.release.dto.ReleaseVersionDto;
import com.ts.rm.domain.release.service.ReleaseVersionService;
import com.ts.rm.global.common.exception.GlobalExceptionHandler;
import com.ts.rm.global.config.MessageConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ReleaseVersion Controller 통합 테스트
 */
@WebMvcTest(ReleaseVersionController.class)
@Import({GlobalExceptionHandler.class, MessageConfig.class})
@ActiveProfiles("test")
@DisplayName("ReleaseVersionController 테스트")
class ReleaseVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReleaseVersionService releaseVersionService;

    private ReleaseVersionDto.SimpleResponse simpleResponse;
    private ReleaseVersionDto.DetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        simpleResponse = new ReleaseVersionDto.SimpleResponse(
                1L,
                "standard",
                null,
                "1.1.0",
                "1.1.x",
                "jhlee@tscientific",
                "새로운 기능 추가",
                false,
                now,
                3
        );

        detailResponse = new ReleaseVersionDto.DetailResponse(
                1L,
                "standard",
                null,
                "1.1.0",
                1,
                1,
                0,
                "1.1.x",
                "jhlee@tscientific",
                "새로운 기능 추가",
                null,
                false,
                now,
                now,
                List.of()
        );
    }

    @Test
    @DisplayName("표준 릴리즈 버전 생성 - 성공")
    void createStandardVersion_Success() throws Exception {
        // given
        ReleaseVersionDto.CreateRequest request = ReleaseVersionDto.CreateRequest.builder()
                .version("1.1.0")
                .createdBy("jhlee@tscientific")
                .comment("새로운 기능 추가")
                .isInstall(false)
                .build();

        given(releaseVersionService.createStandardVersion(any())).willReturn(detailResponse);

        // when & then
        mockMvc.perform(post("/api/v1/releases/standard/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.version").value("1.1.0"))
                .andExpect(jsonPath("$.data.releaseType").value("standard"));
    }

    @Test
    @DisplayName("커스텀 릴리즈 버전 생성 - 성공")
    void createCustomVersion_Success() throws Exception {
        // given
        ReleaseVersionDto.CreateRequest request = ReleaseVersionDto.CreateRequest.builder()
                .version("1.0.0")
                .createdBy("admin@tscientific")
                .comment("고객사 맞춤 기능")
                .customerId(1L)
                .customVersion("1.0.0-company_a")
                .isInstall(false)
                .build();

        ReleaseVersionDto.DetailResponse customResponse = new ReleaseVersionDto.DetailResponse(
                2L,
                "custom",
                "company_a",
                "1.0.0",
                1,
                0,
                0,
                "1.0.x",
                "admin@tscientific",
                "고객사 맞춤 기능",
                "1.0.0-company_a",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );

        given(releaseVersionService.createCustomVersion(any())).willReturn(customResponse);

        // when & then
        mockMvc.perform(post("/api/v1/releases/custom/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.releaseType").value("custom"))
                .andExpect(jsonPath("$.data.customerCode").value("company_a"));
    }

    @Test
    @DisplayName("릴리즈 버전 조회 (ID) - 성공")
    void getVersionById_Success() throws Exception {
        // given
        given(releaseVersionService.getVersionById(1L)).willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/api/v1/releases/versions/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.releaseVersionId").value(1))
                .andExpect(jsonPath("$.data.version").value("1.1.0"));
    }

    @Test
    @DisplayName("타입별 버전 목록 조회 - 성공")
    void getVersionsByType_Success() throws Exception {
        // given
        List<ReleaseVersionDto.SimpleResponse> responses = List.of(simpleResponse);
        given(releaseVersionService.getVersionsByType("standard")).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/releases/standard/versions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].version").value("1.1.0"));
    }

    @Test
    @DisplayName("고객사별 커스텀 버전 목록 조회 - 성공")
    void getVersionsByCustomer_Success() throws Exception {
        // given
        List<ReleaseVersionDto.SimpleResponse> responses = List.of(simpleResponse);
        given(releaseVersionService.getVersionsByCustomer(1L)).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/releases/custom/versions")
                        .param("customerId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Major.Minor 버전 목록 조회 - 성공")
    void getVersionsByMajorMinor_Success() throws Exception {
        // given
        List<ReleaseVersionDto.SimpleResponse> responses = List.of(simpleResponse);
        given(releaseVersionService.getVersionsByMajorMinor("standard", "1.1.x")).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/releases/standard/versions/1.1.x"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].majorMinor").value("1.1.x"));
    }

    @Test
    @DisplayName("릴리즈 버전 정보 수정 - 성공")
    void updateVersion_Success() throws Exception {
        // given
        ReleaseVersionDto.UpdateRequest request = ReleaseVersionDto.UpdateRequest.builder()
                .comment("수정된 코멘트")
                .isInstall(true)
                .build();

        ReleaseVersionDto.DetailResponse updatedResponse = new ReleaseVersionDto.DetailResponse(
                1L,
                "standard",
                null,
                "1.1.0",
                1,
                1,
                0,
                "1.1.x",
                "jhlee@tscientific",
                "수정된 코멘트",
                null,
                true,
                detailResponse.createdAt(),
                LocalDateTime.now(),
                List.of()
        );

        given(releaseVersionService.updateVersion(eq(1L), any())).willReturn(updatedResponse);

        // when & then
        mockMvc.perform(put("/api/v1/releases/versions/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.comment").value("수정된 코멘트"))
                .andExpect(jsonPath("$.data.isInstall").value(true));
    }

    @Test
    @DisplayName("릴리즈 버전 삭제 - 성공")
    void deleteVersion_Success() throws Exception {
        // given
        willDoNothing().given(releaseVersionService).deleteVersion(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/releases/versions/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("버전 생성 - Validation 실패 (version 형식 오류)")
    void createVersion_ValidationFail_InvalidVersionFormat() throws Exception {
        // given
        ReleaseVersionDto.CreateRequest request = ReleaseVersionDto.CreateRequest.builder()
                .version("invalid-version")
                .createdBy("jhlee@tscientific")
                .comment("테스트")
                .isInstall(false)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/releases/standard/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("버전 생성 - Validation 실패 (createdBy 누락)")
    void createVersion_ValidationFail_MissingCreatedBy() throws Exception {
        // given
        ReleaseVersionDto.CreateRequest request = ReleaseVersionDto.CreateRequest.builder()
                .version("1.1.0")
                .comment("테스트")
                .isInstall(false)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/releases/standard/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
