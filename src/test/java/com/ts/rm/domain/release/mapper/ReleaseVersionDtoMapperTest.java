package com.ts.rm.domain.release.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.release.dto.ReleaseVersionDto;
import com.ts.rm.domain.release.entity.ReleaseVersion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ReleaseVersion Mapper 단위 테스트
 */
@SpringBootTest(classes = {ReleaseVersionDtoMapperImpl.class, ReleaseFileDtoMapperImpl.class})
@DisplayName("ReleaseVersionDtoMapper 테스트")
class ReleaseVersionDtoMapperTest {

    @Autowired
    private ReleaseVersionDtoMapper mapper;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .customerId(1L)
                .customerCode("company_a")
                .customerName("A회사")
                .description("테스트 고객사")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Entity → SimpleResponse 변환 성공")
    void toSimpleResponse_Success() {
        // given
        ReleaseVersion version = ReleaseVersion.builder()
                .releaseVersionId(1L)
                .releaseType("STANDARD")
                .version("1.1.0")
                .majorVersion(1)
                .minorVersion(1)
                .patchVersion(0)
                .majorMinor("1.1.x")
                .createdBy("jhlee@tscientific")
                .comment("새로운 기능")
                .isInstall(false)
                .releaseFiles(new ArrayList<>())
                .build();

        // when
        ReleaseVersionDto.SimpleResponse response = mapper.toSimpleResponse(version);

        // then
        assertThat(response).isNotNull();
        assertThat(response.releaseVersionId()).isEqualTo(1L);
        assertThat(response.releaseType()).isEqualTo("STANDARD");
        assertThat(response.version()).isEqualTo("1.1.0");
        assertThat(response.majorMinor()).isEqualTo("1.1.x");
        assertThat(response.createdBy()).isEqualTo("jhlee@tscientific");
        assertThat(response.comment()).isEqualTo("새로운 기능");
        assertThat(response.isInstall()).isFalse();
        assertThat(response.patchFileCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Entity → DetailResponse 변환 성공")
    void toDetailResponse_Success() {
        // given
        ReleaseVersion version = ReleaseVersion.builder()
                .releaseVersionId(1L)
                .releaseType("STANDARD")
                .version("1.1.0")
                .majorVersion(1)
                .minorVersion(1)
                .patchVersion(0)
                .majorMinor("1.1.x")
                .createdBy("jhlee@tscientific")
                .comment("새로운 기능")
                .isInstall(false)
                .releaseFiles(new ArrayList<>())
                .build();

        // when
        ReleaseVersionDto.DetailResponse response = mapper.toDetailResponse(version);

        // then
        assertThat(response).isNotNull();
        assertThat(response.releaseVersionId()).isEqualTo(1L);
        assertThat(response.releaseType()).isEqualTo("STANDARD");
        assertThat(response.version()).isEqualTo("1.1.0");
        assertThat(response.majorVersion()).isEqualTo(1);
        assertThat(response.minorVersion()).isEqualTo(1);
        assertThat(response.patchVersion()).isEqualTo(0);
        assertThat(response.majorMinor()).isEqualTo("1.1.x");
        assertThat(response.releaseFiles()).isEmpty();
    }

    @Test
    @DisplayName("Entity List → SimpleResponse List 변환 성공")
    void toSimpleResponseList_Success() {
        // given
        List<ReleaseVersion> versions = List.of(
                ReleaseVersion.builder()
                        .releaseVersionId(1L)
                        .releaseType("STANDARD")
                        .version("1.1.0")
                        .majorVersion(1)
                        .minorVersion(1)
                        .patchVersion(0)
                        .majorMinor("1.1.x")
                        .createdBy("jhlee@tscientific")
                        .comment("버전 1")
                        .isInstall(false)
                        .releaseFiles(new ArrayList<>())
                        .build(),
                ReleaseVersion.builder()
                        .releaseVersionId(2L)
                        .releaseType("STANDARD")
                        .version("1.2.0")
                        .majorVersion(1)
                        .minorVersion(2)
                        .patchVersion(0)
                        .majorMinor("1.2.x")
                        .createdBy("jhlee@tscientific")
                        .comment("버전 2")
                        .isInstall(true)
                        .releaseFiles(new ArrayList<>())
                        .build()
        );

        // when
        List<ReleaseVersionDto.SimpleResponse> responses = mapper.toSimpleResponseList(versions);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).version()).isEqualTo("1.1.0");
        assertThat(responses.get(1).version()).isEqualTo("1.2.0");
    }

    @Test
    @DisplayName("Entity → SimpleResponse 변환 - 커스텀 릴리즈")
    void toSimpleResponse_Custom_Success() {
        // given
        ReleaseVersion version = ReleaseVersion.builder()
                .releaseVersionId(1L)
                .releaseType("CUSTOM")
                .customer(testCustomer)
                .version("1.0.0")
                .majorVersion(1)
                .minorVersion(0)
                .patchVersion(0)
                .majorMinor("1.0.x")
                .createdBy("admin@tscientific")
                .comment("커스텀 버전")
                .customVersion("1.0.0-company_a")
                .isInstall(false)
                .releaseFiles(new ArrayList<>())
                .build();

        // when
        ReleaseVersionDto.SimpleResponse response = mapper.toSimpleResponse(version);

        // then
        assertThat(response.releaseType()).isEqualTo("CUSTOM");
        assertThat(response.customerCode()).isEqualTo("company_a");
    }
}
